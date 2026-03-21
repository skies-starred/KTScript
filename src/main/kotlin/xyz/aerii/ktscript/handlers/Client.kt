package xyz.aerii.ktscript.handlers

import net.minecraft.client.Minecraft

object Client {
    @JvmField
    val self: Minecraft = Minecraft.getInstance()
}