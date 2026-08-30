package foo.starred.ktscript.mixin;

import foo.starred.ktscript.events.GuiEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void ktscript$render$pre(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        new GuiEvent.Render.Pre(graphics).post();
    }

    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractSleepOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void ktscript$render$main(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        new GuiEvent.Render.Main(graphics).post();
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void ktscript$render$post(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        new GuiEvent.Render.Post(graphics).post();
    }

    @Inject(method = "extractSlot", at = @At("HEAD"), cancellable = true)
    private void ktscript$renderSlot$pre(GuiGraphicsExtractor graphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack itemStack, int seed, CallbackInfo ci) {
        if (new GuiEvent.Slots.Render.Hotbar.Pre(graphics, itemStack, x, y).post()) ci.cancel();
    }

    @Inject(method = "extractSlot", at = @At("TAIL"), cancellable = true)
    private void ktscript$renderSlot$post(GuiGraphicsExtractor graphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack itemStack, int seed, CallbackInfo ci) {
        if (new GuiEvent.Slots.Render.Hotbar.Post(graphics, itemStack, x, y).post()) ci.cancel();
    }
}
