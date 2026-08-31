package foo.starred.ktscript

import foo.starred.kommand.IKommand
import foo.starred.kommand.scopes.KommandCommandScope
import foo.starred.ktscript.api.hooks.impl.HookManager
import foo.starred.ktscript.api.lifecycle.managed.impl.KTScriptLifeCycle
import foo.starred.ktscript.api.lifecycle.singletons.impl.KTScriptSingletons
import foo.starred.ktscript.events.core.Node
import foo.starred.ktscript.events.impl.EventDispatcher
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
        val s = dir.walkTopDown().filter { it.isFile && it.extension == "kts" && !it.name.startsWith(".") }.toList()
        if (s.isEmpty()) return LOGGER.info("KTScript found no scripts to load.")

        KTScriptSingletons.remove(s.map { it.name }.toSet())

        val ss = s.sortedBy { it.name }
        val t = ss.size

        thread(name = "KTScript", isDaemon = true) {
            val jars = s.map { "${it.relativeToOrNull(dir)?.path?.replace(File.separatorChar, '_') ?: it.name}-${it.lastModified()}.jar" }.toSet()
            KTScriptHost.cache.listFiles { f -> f.extension == "jar" && f.name !in jars }?.forEach { it.delete() }

            val i = AtomicInteger()
            val threads = ss.map { thread(isDaemon = true) { if (KTScriptHost.eval(it)) i.incrementAndGet() } }

            for (t in threads) t.join()
            LOGGER.info("KTScript loaded ${i.get()}/$t scripts.")
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
