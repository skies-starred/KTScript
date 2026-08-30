package foo.starred.ktscript.mixin;

import foo.starred.ktscript.events.InputEvent;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void ktscript$keyPress(long handle, int action, KeyEvent event, CallbackInfo ci) {
        if (action == 1) {
            if (new InputEvent.Keyboard.Press(event).post()) ci.cancel();
        } else if (action == 0) {
            new InputEvent.Keyboard.Release(event).post();
        }
    }
}
