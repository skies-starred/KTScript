package xyz.aerii.ktscript.events

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import xyz.aerii.ktscript.events.core.Event

sealed class WorldRenderEvent {
    data class Last(val context: WorldRenderContext) : Event()
}