package foo.starred.ktscript.modifiers

import foo.starred.ktscript.KTScript
import foo.starred.ktscript.KTScriptHost
import foo.starred.ktscript.annotations.Mod
import foo.starred.ktscript.modifiers.PackageModifier.key
import foo.starred.ktscript.modifiers.PackageModifier.source
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm

object ModModifier {
    fun eval(file: File): Boolean {
        KTScriptHost.current.set(file)
        try {
            val source = source(file)
            val receivers = LibraryModifier.libraries.map { KotlinType(it::class) }

            val config = ScriptCompilationConfiguration(CompilationModifier.configuration) {
                if (receivers.isNotEmpty()) implicitReceivers(*receivers.toTypedArray())
            }

            val script = when (val result = KTScriptHost.host.runInCoroutineContext { KTScriptHost.host.compiler(source, config) }) {
                is ResultWithDiagnostics.Failure -> return KTScriptHost.error("compilation", file, result.reports)
                is ResultWithDiagnostics.Success -> result.value
            }

            val evaluation = ScriptEvaluationConfiguration {
                jvm {
                    baseClassLoader(LibraryModifier.loader)
                }

                if (LibraryModifier.libraries.isNotEmpty()) implicitReceivers(*LibraryModifier.libraries)
            }

            val klass = when (val result = KTScriptHost.host.runInCoroutineContext { script.getClass(evaluation) }) {
                is ResultWithDiagnostics.Failure -> return KTScriptHost.error("class load", file, result.reports)
                is ResultWithDiagnostics.Success -> result.value
            }

            if (klass.java.getAnnotation(Mod::class.java)?.autoInit != false) {
                autoInit(file, klass.java)
            }

            val eval = when (val result = KTScriptHost.host.runInCoroutineContext { KTScriptHost.host.evaluator(script, evaluation) }) {
                is ResultWithDiagnostics.Failure -> return KTScriptHost.error("evaluation", file, result.reports)
                is ResultWithDiagnostics.Success -> result.value
            }

            if (eval.returnValue is ResultValue.Error) {
                val error = (eval.returnValue as ResultValue.Error).error
                val root = generateSequence(error) { it.cause }.last()
                val trace = root.stackTrace.take(8).joinToString("\n   at ")

                KTScript.LOGGER.error("Script evaluation error in ${file.name}:\n - ${root.javaClass.name}: ${root.message}\n   at $trace")
                return false
            }

            return true
        } catch (t: Throwable) {
            KTScript.LOGGER.error("Script evaluation error in ${file.name}", t)
            return false
        } finally {
            KTScriptHost.current.remove()
        }
    }

    private fun autoInit(file: File, classes: Class<*>) {
        val (k, t) = key(file)
        val jar = KTScriptHost.cache.resolve("$k-$t.jar")
        if (!jar.isFile) return

        JarFile(jar).use { zip ->
            zip.entries().asSequence()
                .filter { it.name.endsWith(".class") && !it.name.endsWith("_kts.class") }
                .forEach { entry -> init(classes.classLoader, entry) }
        }
    }

    private fun init(loader: ClassLoader, entry: JarEntry) {
        runCatching {
            loader.loadClass(entry.name.removeSuffix(".class").replace('/', '.')).getDeclaredField("INSTANCE").get(null)
        }
    }
}
