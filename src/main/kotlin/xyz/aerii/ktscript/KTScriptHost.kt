package xyz.aerii.ktscript

import net.fabricmc.loader.api.FabricLoader
import java.io.File
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.compilerOptions
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.compilationCache
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.CompiledScriptJarsCache

object KTScriptHost {
    private val host = BasicJvmScriptingHost(ScriptingHostConfiguration {
        jvm {
            compilationCache(CompiledScriptJarsCache { source, _ ->
                val name = source.name ?: source.locationId
                File(cacheDir, "$name-${sourceFile(source.name)?.lastModified() ?: 0}.jar")
            })
        }
    })

    private fun sourceFile(name: String?) = name?.let { KTScript.dir.listFiles { f -> f.name == it }?.firstOrNull() }

    private val compilation = ScriptCompilationConfiguration {
        compilerOptions("-Xskip-prerelease-check", "-jvm-target", "21")
        jvm { dependenciesFromCurrentContext(wholeClasspath = true) }
    }

    private val evaluation = ScriptEvaluationConfiguration {}

    @JvmStatic
    val cacheDir = File(FabricLoader.getInstance().configDir.toFile(), "KTScript/compiled").apply { mkdirs() }

    @JvmStatic
    fun eval(file: File): Boolean {
        val result = host.eval(file.toScriptSource(), compilation, evaluation)

        if (result is ResultWithDiagnostics.Failure) {
            val errors = result.reports.filter { it.severity == ScriptDiagnostic.Severity.ERROR }

            KTScript.LOGGER.error("Script error in ${file.name}: ${errors.joinToString("\n") { it.message }}")
            return false
        }

        when (val rv = (result as ResultWithDiagnostics.Success).value.returnValue) {
            is ResultValue.Value -> rv.scriptInstance
            is ResultValue.Unit -> rv.scriptInstance
            is ResultValue.Error -> rv.scriptInstance
            else -> null
        }?.let { instance ->
            instance::class.nestedClasses.mapNotNull { it.objectInstance }
        }

        return true
    }
}