package xyz.aerii.ktscript.handlers

import net.minecraft.network.chat.Component

object Typo {
    private val STRIP_COLOR_REGEX = Regex("(?i)§.")

    @JvmStatic
    fun String.stripped(): String {
        return STRIP_COLOR_REGEX.replace(this, "")
    }

    @JvmStatic
    fun Component.stripped(): String {
        return STRIP_COLOR_REGEX.replace(this.string, "")
    }
}