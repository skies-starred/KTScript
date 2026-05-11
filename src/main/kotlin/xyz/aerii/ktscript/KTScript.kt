package xyz.aerii.ktscript

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import xyz.aerii.ktscript.events.core.Node
import xyz.aerii.ktscript.events.impl.EventDispatcher
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

object KTScript : ClientModInitializer {
    @JvmField
    val dir = File(FabricLoader.getInstance().configDir.toFile(), "KTScript/scripts")

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(KTScript::class.java)

    @JvmStatic
    val nodes: MutableList<Node<*>> = mutableListOf()

    override fun onInitializeClient() {
        LOGGER.info("KTScript initialised.")
        EventDispatcher.init()
        load()

        ClientCommandRegistrationCallback.EVENT.register { d, _ ->
            literal("ktscript").then(
                literal("reload")
                    .executes {
                        load()
                        1
                    }
            ).apply {
                d.register(this)
            }
        }
    }

    private fun load() {
        dir.mkdirs()

        for (n in nodes) n.unregister()
        nodes.clear()

        val s = dir.listFiles { f -> f.extension == "kts" } ?: emptyArray()
        val validJars = s.map { "${it.name}-${it.lastModified()}.jar" }.toSet()
        KTScriptHost.cacheDir.listFiles { f -> f.extension == "jar" && f.name !in validJars }?.forEach { it.delete() }

        if (s.isEmpty()) return LOGGER.info("KTScript found no scripts to load.")

        val ss = s.sortedBy { it.name }
        val t = ss.size

        thread(name = "KTScript", isDaemon = true) {
            val i = AtomicInteger()
            val threads = ss.map { thread(isDaemon = true) { if (KTScriptHost.eval(it)) i.incrementAndGet() } }

            threads.forEach { it.join() }
            LOGGER.info("KTScript loaded ${i.get()}/$t scripts.")
        }
    }
}