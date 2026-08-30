package foo.starred.ktscript.events

import foo.starred.ktscript.events.core.Event
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext

sealed class WorldRenderEvent {
    data class Last(val context: LevelRenderContext) : Event()
}
