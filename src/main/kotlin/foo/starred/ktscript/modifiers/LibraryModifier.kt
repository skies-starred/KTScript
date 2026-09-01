package foo.starred.ktscript.modifiers

import foo.starred.ktscript.KTScript
import foo.starred.ktscript.KTScriptHost
import foo.starred.ktscript.annotations.Dependencies
import foo.starred.ktscript.modifiers.PackageModifier.source
import java.io.File
import java.net.URLClassLoader
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm

object LibraryModifier {
    var libraries = emptyArray<Any>()
    var loader = URLClassLoader(emptyArray(), KTScriptHost::class.java.classLoader)

    fun load(file: File): Boolean {
        KTScriptHost.current.set(file)
        try {
            val source = source(file)
            val config = ScriptEvaluationConfiguration {
                jvm {
                    baseClassLoader(loader)
                }
            }

            val script = when (val result = KTScriptHost.host.runInCoroutineContext { KTScriptHost.host.compiler(source, CompilationModifier.configuration) }) {
                is ResultWithDiagnostics.Failure -> return KTScriptHost.error("compilation", file, result.reports)
                is ResultWithDiagnostics.Success -> result.value
            }

            val klass = when (val result = KTScriptHost.host.runInCoroutineContext { script.getClass(config) }) {
                is ResultWithDiagnostics.Failure -> return KTScriptHost.error("class load", file, result.reports)
                is ResultWithDiagnostics.Success -> result.value
            }

            val dependencies = klass.java.getAnnotation(Dependencies::class.java)
            if (dependencies != null && !CompilationModifier.validate(file, dependencies.mods, dependencies.minecraft)) return false

            val eval = when (val result = KTScriptHost.host.runInCoroutineContext { KTScriptHost.host.evaluator(script, config) }) {
                is ResultWithDiagnostics.Failure -> return KTScriptHost.error("evaluation", file, result.reports)
                is ResultWithDiagnostics.Success -> result.value
            }

            if (eval.returnValue is ResultValue.Error) {
                val error = (eval.returnValue as ResultValue.Error).error
                val root = generateSequence(error) { it.cause }.last()
                val trace = root.stackTrace.take(8).joinToString("\n   at ")

                KTScript.LOGGER.error("Script library error in ${file.name}:\n - ${root.javaClass.name}: ${root.message}\n   at $trace")
                return false
            }

            val instance = (eval.returnValue as? ResultValue.Value)?.scriptInstance ?: (eval.returnValue as? ResultValue.Value)?.value ?: (eval.returnValue as? ResultValue.Unit)?.scriptInstance ?: (eval.returnValue as? ResultValue.Unit)?.let { klass.java.getDeclaredConstructor().newInstance() }
            if (instance != null) libraries += instance
            return true
        } catch (t: Throwable) {
            KTScript.LOGGER.error("Script library error in ${file.name}", t)
            return false
        } finally {
            KTScriptHost.current.remove()
        }
    }
    fun reload(libraries1: List<File>) {
        runCatching { loader.close() }
        val urls = libraries1.mapNotNull { file -> PackageModifier.key(file).let { (k, t) -> KTScriptHost.cache.resolve("$k-$t.jar").takeIf { it.isFile }?.toURI()?.toURL() } }.toTypedArray()
        loader = URLClassLoader(urls, KTScriptHost::class.java.classLoader)
        libraries = emptyArray()
    }

    fun compile(file: File): Boolean {
        return when (val result = KTScriptHost.host.runInCoroutineContext { KTScriptHost.host.compiler(source(file), CompilationModifier.configuration) }) {
            is ResultWithDiagnostics.Failure -> KTScriptHost.error("compilation", file, result.reports)
            is ResultWithDiagnostics.Success -> true
        }
    }
}
