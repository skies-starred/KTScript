package xyz.aerii.ktscript.events

import net.minecraft.world.entity.Entity
import xyz.aerii.ktscript.events.core.Event

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