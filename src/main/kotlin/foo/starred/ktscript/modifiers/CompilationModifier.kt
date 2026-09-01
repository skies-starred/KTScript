package foo.starred.ktscript.modifiers

import foo.starred.ktscript.KTScript
import foo.starred.ktscript.KTScriptHost
import foo.starred.ktscript.modifiers.PackageModifier.modify
import foo.starred.ktscript.modifiers.PackageModifier.source
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.version.VersionPredicate
import net.minecraft.SharedConstants
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath

object CompilationModifier {
    private val defaults = listOf(
        "foo.starred.ktscript.annotations.Mod",
        "foo.starred.ktscript.annotations.Lib",
        "foo.starred.ktscript.annotations.Dependencies",
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

    val configuration = ScriptCompilationConfiguration {
        compilerOptions("-Xskip-prerelease-check", "-jvm-target", "25")
        defaultImports(defaults)

        refineConfiguration {
            beforeCompiling { context ->
                val file = context.script.locationId?.let { File(it) } ?: return@beforeCompiling context.compilationConfiguration.asSuccess()
                if (file.name != "main.kts" && file.name != "library.kts") return@beforeCompiling context.compilationConfiguration.asSuccess()

                val base = file.parentFile ?: KTScript.dir
                val sources = base.walkTopDown().filter { it.isFile && it.extension == "kts" && it != file && !it.name.startsWith(".") }.map { source(it) }.toList()
                val jars = KTScript.dir.walkTopDown().filter { it.isDirectory && !it.name.startsWith(".") && it != base }.mapNotNull { b -> b.resolve("library.kts").takeIf { it.isFile } }.mapNotNull { c -> PackageModifier.key(c).let { (k, t) -> KTScriptHost.cache.resolve("$k-$t.jar").takeIf { it.isFile } } }.toList()
                val subs = base.walkTopDown().filter { it.isDirectory }.map { modify(it.resolve("dummy.kts")) + ".*" }.toList()

                ScriptCompilationConfiguration(context.compilationConfiguration) {
                    if (sources.isNotEmpty()) importScripts(sources)
                    if (jars.isNotEmpty()) jvm { updateClasspath(jars) }
                    defaultImports(defaults + subs)
                }.asSuccess()
            }
        }

        jvm {
            dependenciesFromCurrentContext(wholeClasspath = true)
        }
    }

    fun validate(file: File, mods: Array<String>, minecraft: Array<String>): Boolean {
        if (minecraft.isNotEmpty()) {
            val version = Version.parse(SharedConstants.getCurrentVersion().name())
            if (!minecraft.any { VersionPredicate.parse(it).test(version) }) {
                KTScript.LOGGER.warn("Skipping '${file.name}': requires Minecraft ${minecraft.joinToString(" or ")} (current: ${version.friendlyString})")
                return false
            }
        }

        val missing = mods.filterNot { FabricLoader.getInstance().isModLoaded(it) }
        if (missing.isNotEmpty()) {
            KTScript.LOGGER.warn("Skipping '${file.name}': missing mod dependencies: ${missing.joinToString(", ")}")
            return false
        }

        return true
    }
}
