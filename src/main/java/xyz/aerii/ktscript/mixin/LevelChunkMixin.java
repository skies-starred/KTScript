package xyz.aerii.ktscript.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.aerii.ktscript.events.BlockEvent;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {
    @Final
    @Shadow
    Level level;

    @Inject(method = "setBlockState", at = @At("HEAD"))
    private void ktscript$setBlockState(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
        BlockState old = this.level.getBlockState(pos);
        if (old != state) new BlockEvent(old, state, pos).post();
    }
}