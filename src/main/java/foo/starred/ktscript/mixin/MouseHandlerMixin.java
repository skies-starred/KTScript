package foo.starred.ktscript.mixin;

import foo.starred.ktscript.events.InputEvent;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Shadow
    private double xpos;

    @Shadow
    private double ypos;

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void ktscript$onButton(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        if (action == 1) {
            if (new InputEvent.Mouse.Press(rawButtonInfo).post()) ci.cancel();
        } else if (action == 0) {
            new InputEvent.Mouse.Release(rawButtonInfo).post();
        }
    }

    @Inject(method = "onMove", at = @At("HEAD"), cancellable = true)
    private void ktscript$onMove(long handle, double xpos, double ypos, CallbackInfo ci) {
        if (!(new InputEvent.Mouse.Move(xpos, ypos).post())) return;

        ci.cancel();
        this.xpos = xpos;
        this.ypos = ypos;
    }
}
