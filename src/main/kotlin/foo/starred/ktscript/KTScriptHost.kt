package foo.starred.ktscript

import foo.starred.ktscript.modifiers.PackageModifier.key
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.compilationCache
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.CompiledScriptJarsCache

object KTScriptHost {
    val cache = File(FabricLoader.getInstance().configDir.toFile(), "KTScript/compiled").apply { mkdirs() }
    val current = ThreadLocal<File>()

    val host = BasicJvmScriptingHost(ScriptingHostConfiguration {
        jvm {
            compilationCache(CompiledScriptJarsCache { source, _ ->
                val (k, t) = source.locationId?.let { File(it) }?.let { key(it) } ?: Pair(source.name?.substringBeforeLast('.') ?: "script", 0L)
                File(cache, "$k-$t.jar")
            })
        }
    })

    fun error(text: String, file: File, reports: List<ScriptDiagnostic>): Boolean {
        val errors = reports.filter { it.severity == ScriptDiagnostic.Severity.ERROR || it.severity == ScriptDiagnostic.Severity.FATAL }.ifEmpty { reports }
        val lines = errors.map { diag ->
            "${diag.message}${diag.location?.let { " (line ${it.start.line}, col ${it.start.col})" } ?: ""}${diag.exception?.format() ?: ""}"
        }

        KTScript.LOGGER.error("Script $text error in ${file.name}:\n - ${lines.joinToString("\n - ")}")
        return false
    }

    private fun Throwable.format(): String {
        val root = generateSequence(this) { it.cause }.last()
        return "\n   ${root.javaClass.name}: ${root.message}\n   at ${root.stackTrace.take(8).joinToString("\n   at ")}"
    }
}
