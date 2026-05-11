package xyz.aerii.ktscript.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.aerii.ktscript.events.GuiEvent;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void ktscript$render$pre(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        new GuiEvent.Render.Pre(guiGraphics).post();
    }

    //~ if >= 26.1 'render' -> 'extractRenderState'
    //~ if >= 26.1 'renderSleepOverlay' -> 'extractSleepOverlay'
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSleepOverlay(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    private void ktscript$render$main(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        new GuiEvent.Render.Main(guiGraphics).post();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void ktscript$render$post(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        new GuiEvent.Render.Post(guiGraphics).post();
    }

    //~ if >= 26.1 'renderSlot' -> 'extractSlot'
    @Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true)
    private void ktscript$renderSlot$pre(GuiGraphics guiGraphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack stack, int seed, CallbackInfo ci) {
        if (new GuiEvent.Slots.Render.Hotbar.Pre(guiGraphics, stack, x, y).post()) ci.cancel();
    }

    //~ if >= 26.1 'renderSlot' -> 'extractSlot'
    @Inject(method = "renderSlot", at = @At("TAIL"), cancellable = true)
    private void ktscript$renderSlot$post(GuiGraphics guiGraphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack stack, int seed, CallbackInfo ci) {
        if (new GuiEvent.Slots.Render.Hotbar.Post(guiGraphics, stack, x, y).post()) ci.cancel();
    }
}