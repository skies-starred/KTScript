package xyz.aerii.ktscript.utils

import net.minecraft.client.Minecraft
import xyz.aerii.ktscript.handlers.Client

inline fun mainThread(
    crossinline block: Minecraft.() -> Unit
) {
    Client.self.execute { Client.self.block() }
}