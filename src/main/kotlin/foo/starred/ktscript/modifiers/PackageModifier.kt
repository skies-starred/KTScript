package foo.starred.ktscript.modifiers

import foo.starred.ktscript.KTScript
import java.io.File
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.host.toScriptSource

object PackageModifier {
    fun modify(file: File): String {
        val top = file.relativeToOrNull(KTScript.dir)?.path?.substringBefore(File.separatorChar)?.substringBefore('/')?.let { KTScript.dir.resolve(it) } ?: file.parentFile ?: return "ktscript.scripts"
        val root = listOf(top.resolve("main.kts"), top.resolve("library.kts")).firstOrNull(File::isFile)
        val base = root?.useLines { it.map(String::trim).firstOrNull { line -> line.startsWith("package ") }?.removePrefix("package ")?.trimEnd(';', ' ') } ?: "ktscript.scripts.${top.name}"

        val parent = file.parentFile ?: return base
        if (parent == top) return base

        val subpackage = parent.relativeTo(top).path.replace(File.separatorChar, '.').replace('/', '.')
        return "$base.$subpackage"
    }

    fun source(file: File): SourceCode {
        val text = file.readText()
        if (text.lineSequence().any { it.trim().startsWith("package ") }) return file.toScriptSource()

        var i0 = 0
        var i1 = 0
        var i2 = 0

        for (line in text.lineSequence()) {
            val line1 = line.trim()
            if (line1.isEmpty() || line1.startsWith("//") || line1.startsWith("/*")) {
                i1 += line.length + 1
                continue
            }

            if (!line1.startsWith("@file:") && i0 == 0) {
                break
            }

            i0 += line.count { it == '(' } - line.count { it == ')' }
            if (i0 <= 0 && !line1.endsWith(",")) {
                i2 = i1 + line.length
                break
            }

            i1 += line.length + 1
        }

        val pack = modify(file)
        val content = if (i2 == 0 || i2 >= text.length) "package $pack\n\n$text" else text.substring(0, i2) + "\n\npackage $pack\n\n" + text.substring(i2).trimStart()
        return StringScriptSource(content, name = file.name, locationId = file.absolutePath)
    }

    fun module(file: File): String {
        val top = file.relativeToOrNull(KTScript.dir)?.path?.substringBefore(File.separatorChar)?.substringBefore('/')?.let { KTScript.dir.resolve(it) }
        return if (top?.isDirectory == true) top.name else file.nameWithoutExtension
    }

    fun key(file: File): Pair<String, Long> {
        val top = file.relativeToOrNull(KTScript.dir)?.path?.substringBefore(File.separatorChar)?.substringBefore('/')?.let { KTScript.dir.resolve(it) }
        if (top?.isDirectory != true) return file.nameWithoutExtension to file.lastModified()

        val time = top.walkTopDown().filter { it.isFile && it.extension == "kts" }.maxOfOrNull { it.lastModified() } ?: file.lastModified()
        return top.name to time
    }
}
