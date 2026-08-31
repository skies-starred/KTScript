package foo.starred.ktscript

import net.fabricmc.loader.api.FabricLoader
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.compilationCache
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.CompiledScriptJarsCache

object KTScriptHost {
    private val import = Regex("""@file:Import\s*\(([\s\S]*?)\)""")
    private val string = Regex(""""([^"\\]*(?:\\.[^"\\]*)*)"""")

    private val host = BasicJvmScriptingHost(ScriptingHostConfiguration {
        jvm {
            compilationCache(CompiledScriptJarsCache { source, _ ->
                val file = source.locationId?.let { File(it) }?.takeIf { it.exists() }
                val key = file?.relativeToOrNull(KTScript.dir)?.path?.replace(File.separatorChar, '_') ?: (source.name ?: "script")
                val time = file?.lastModified() ?: 0L
                File(cache, "$key-$time.jar")
            })
        }
    })

    private val compilation = ScriptCompilationConfiguration {
        compilerOptions("-Xskip-prerelease-check", "-jvm-target", "25")
        defaultImports(
            "foo.starred.ktscript.annotations.Import",
            "foo.starred.ktscript.events.*",
            "foo.starred.ktscript.events.core.*",
            "foo.starred.ktscript.api.lifecycle.managed.extensions.*",
            "foo.starred.ktscript.api.lifecycle.singletons.*",
            "foo.starred.ktscript.api.lifecycle.singletons.extensions.*",
            "foo.starred.ktscript.api.hooks.data.*",
            "foo.starred.ktscript.api.hooks.context.*",
            "foo.starred.ktscript.api.hooks.extensions.*",
            "foo.starred.ktscript.api.network.*"
        )

        refineConfiguration {
            beforeCompiling { context ->
                val file = context.script.locationId?.let { File(it) } ?: return@beforeCompiling context.compilationConfiguration.asSuccess()
                val base = file.parentFile ?: KTScript.dir

                val imports = import.findAll(runCatching { file.readText() }.getOrDefault("")).flatMap { a -> string.findAll(a.groupValues[1]).map { b -> b.groupValues[1] } }.toList()
                val sources = imports.map { path ->
                    val file = get(base, path)
                    if (file?.isFile != true) return@beforeCompiling makeFailureResult("Cannot find imported script: '$path' (looked in ${base.path} and ${KTScript.dir.path})")

                    file.toScriptSource()
                }

                ScriptCompilationConfiguration(context.compilationConfiguration) { importScripts(sources) }.asSuccess()
            }
        }

        jvm {
            dependenciesFromCurrentContext(wholeClasspath = true)
        }
    }

    private val evaluation = ScriptEvaluationConfiguration {}

    @JvmStatic
    val cache = File(FabricLoader.getInstance().configDir.toFile(), "KTScript/compiled").apply { mkdirs() }

    val current = ThreadLocal<File>()

    @JvmStatic
    fun eval(file: File): Boolean {
        current.set(file)
        try {
            val source = file.toScriptSource()

            val script = when (val result = host.runInCoroutineContext { host.compiler(source, compilation) }) {
                is ResultWithDiagnostics.Failure -> return error("compilation", file, result.reports)
                is ResultWithDiagnostics.Success -> result.value
            }

            when (val result = host.runInCoroutineContext { script.getClass(evaluation) }) {
                is ResultWithDiagnostics.Failure -> return error("class load", file, result.reports)
                is ResultWithDiagnostics.Success -> {}
            }

            return when (val result = host.runInCoroutineContext { host.evaluator(script, evaluation) }) {
                is ResultWithDiagnostics.Failure -> error("evaluation", file, result.reports)
                is ResultWithDiagnostics.Success -> true
            }
        } finally {
            current.remove()
        }
    }

    private fun error(text: String, file: File, reports: List<ScriptDiagnostic>): Boolean {
        val errors = reports.filter { it.severity == ScriptDiagnostic.Severity.ERROR }
        KTScript.LOGGER.error("Script $text error in ${file.name}:\n - ${errors.joinToString("\n - ") { it.message }}")
        return false
    }

    private fun get(base: File, path: String): File? {
        val list = listOf(base.resolve(path), base.resolve("$path.kts"), KTScript.dir.resolve(path), KTScript.dir.resolve("$path.kts"))
        return list.firstOrNull { it.isFile && !it.name.startsWith(".") } ?: KTScript.dir.walkTopDown().firstOrNull { it.isFile && !it.name.startsWith(".") && (it.name == path || it.name == "$path.kts" || it.path.endsWith(path) || it.path.endsWith("$path.kts")) }
    }
}
