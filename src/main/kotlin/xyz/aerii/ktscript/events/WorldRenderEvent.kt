package xyz.aerii.ktscript.events

//~ if >= 26.1 'world.WorldRenderContext' -> 'level.LevelRenderContext'
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import xyz.aerii.ktscript.events.core.Event

sealed class WorldRenderEvent {
    //~ if >= 26.1 'WorldRenderContext' -> 'LevelRenderContext'
    data class Last(val context: WorldRenderContext) : Event()
}