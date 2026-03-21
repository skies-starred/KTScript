package xyz.aerii.ktscript.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
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

    @Inject(method = "render", at = @At("TAIL"))
    private void ktscript$render$post(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        new GuiEvent.Render.Post(guiGraphics).post();
    }
}