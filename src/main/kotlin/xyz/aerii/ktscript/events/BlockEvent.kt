package xyz.aerii.ktscript.events

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import xyz.aerii.ktscript.events.core.Event

data class BlockEvent(
    val old: BlockState,
    val new: BlockState,
    val pos: BlockPos
) : Event()