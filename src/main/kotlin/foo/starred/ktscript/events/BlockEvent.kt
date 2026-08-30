package foo.starred.ktscript.events

import foo.starred.ktscript.events.core.Event
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

data class BlockEvent(
    val old: BlockState,
    val new: BlockState,
    val pos: BlockPos
) : Event()
