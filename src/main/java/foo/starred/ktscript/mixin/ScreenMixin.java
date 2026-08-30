package foo.starred.ktscript.mixin;

import foo.starred.ktscript.events.GuiEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("HEAD"), cancellable = true)
    private void ktscript$renderWithTooltipAndSubtitles$pre(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        Screen self = self();
        if ((self instanceof AbstractContainerScreen<?>)) if (new GuiEvent.Render.Container.Pre(graphics).post()) ci.cancel();
        if (new GuiEvent.Render.Screen.Pre(graphics).post()) ci.cancel();
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("TAIL"))
    private void ktscript$renderWithTooltipAndSubtitles$post(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        new GuiEvent.Render.Screen.Post(graphics).post();
    }

    @Unique
    private Screen self() {
        return (Screen) (Object) this;
    }
}
