package foo.starred.ktscript

import foo.starred.kommand.IKommand
import foo.starred.kommand.scopes.KommandCommandScope
import foo.starred.ktscript.api.hooks.impl.HookManager
import foo.starred.ktscript.api.lifecycle.managed.impl.KTScriptLifeCycle
import foo.starred.ktscript.api.lifecycle.singletons.impl.KTScriptSingletons
import foo.starred.ktscript.events.core.Node
import foo.starred.ktscript.events.impl.EventDispatcher
import foo.starred.ktscript.modifiers.LibraryModifier
import foo.starred.ktscript.modifiers.ModModifier
import foo.starred.ktscript.modifiers.PackageModifier
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

object KTScript : ClientModInitializer, IKommand<FabricClientCommandSource> {
    override val loader: KommandCommandScope<FabricClientCommandSource> = KommandCommandScope()

    @JvmField
    val dir = File(FabricLoader.getInstance().configDir.toFile(), "KTScript/scripts")

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(KTScript::class.java)

    @JvmStatic
    val nodes: MutableList<Node<*>> = mutableListOf()

    override fun onInitializeClient() {
        LOGGER.info("KTScript initialising...")

        EventDispatcher.init()
        load()

        command("ktscript") {
            "reload" {
                load()
            }
        }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            loader.register(dispatcher)
        }

        LOGGER.info("KTScript initialised.")
    }

    private fun load() {
        dir.mkdirs()
        unregister()
        register()
    }

    private fun register() {
        val folders = dir.walkTopDown().filter { it.isDirectory && !it.name.startsWith(".") }.toList()
        if (folders.isEmpty()) return LOGGER.info("KTScript found no script modules or libraries")

        val libraries = folders.mapNotNull { it.resolve("library.kts").takeIf { f -> f.isFile } }
        val mods = folders.mapNotNull { it.resolve("main.kts").takeIf { f -> f.isFile } }
        if (libraries.isEmpty() && mods.isEmpty()) return LOGGER.info("KTScript found no modules with main.kts or library.kts")

        val all = (libraries + mods).map { PackageModifier.module(it) }.toSet()
        KTScriptSingletons.remove(all)

        val sorted = mods.sortedBy { it.name }
        val size = sorted.size

        thread(name = "KTScript", isDaemon = true) {
            val jars = (libraries + mods).map { PackageModifier.key(it).let { (key, time) -> "$key-$time.jar" } }.toSet()
            KTScriptHost.cache.walkTopDown().filter { f -> f.isFile && f.extension == "jar" && f.name !in jars }.forEach { it.delete() }
            LibraryModifier.libraries = emptyArray()

            for (library in libraries) LibraryModifier.compile(library)
            LibraryModifier.reload(libraries)
            for (library in libraries) LibraryModifier.load(library)

            val i = AtomicInteger()
            val threads = sorted.map { thread(isDaemon = true) { if (ModModifier.eval(it)) i.incrementAndGet() } }

            for (t in threads) t.join()
            LOGGER.info("KTScript loaded ${i.get()}/$size mods (${libraries.size} libraries)")
        }
    }

    private fun unregister() {
        KTScriptLifeCycle.cleanup()
        HookManager.hooks.clear()

        for (n in nodes) {
            n.unregister()
        }

        nodes.clear()
    }
}
