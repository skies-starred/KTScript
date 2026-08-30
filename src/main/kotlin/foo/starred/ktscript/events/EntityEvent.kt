package foo.starred.ktscript.events

import foo.starred.ktscript.events.core.Event
import net.minecraft.world.entity.Entity

sealed class EntityEvent {
    data class Load(
        val entity: Entity
    ) : Event()

    data class Unload(
        val entity: Entity
    ) : Event()

    data class Death(
        val entity: Entity
    ) : Event()
}
