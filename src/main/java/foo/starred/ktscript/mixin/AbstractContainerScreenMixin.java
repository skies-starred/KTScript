package foo.starred.ktscript.mixin;

import foo.starred.ktscript.events.GuiEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Unique
    @Nullable
    private Slot ktscript$previousHoveredSlot = null;

    @Inject(method = "extractSlot", at = @At("HEAD"), cancellable = true)
    private void ktscript$onRenderSlot$pre(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (new GuiEvent.Slots.Render.Pre(graphics, slot).post()) ci.cancel();
    }

    @Inject(method = "extractSlot", at = @At("RETURN"))
    private void ktscript$onRenderSlot$post(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        new GuiEvent.Slots.Render.Post(graphics, slot).post();
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void ktscript$slotClick(Slot slot, int slotId, int buttonNum, ContainerInput containerInput, CallbackInfo ci) {
        if (new GuiEvent.Slots.Click(slot, slotId, buttonNum, containerInput).post()) ci.cancel();
    }

    @Inject(method = "extractContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;getHoveredSlot(DD)Lnet/minecraft/world/inventory/Slot;"))
    private void ktscript$renderContents$0(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        ktscript$previousHoveredSlot = hoveredSlot;
    }

    @Inject(method = "extractContents", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hoveredSlot:Lnet/minecraft/world/inventory/Slot;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void ktscript$renderContents$1(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (hoveredSlot == ktscript$previousHoveredSlot) return;

        if (hoveredSlot != null) new GuiEvent.Slots.Hover(hoveredSlot).post();
        ktscript$previousHoveredSlot = hoveredSlot;
    }
}
