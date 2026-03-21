package xyz.aerii.ktscript.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.aerii.ktscript.events.GuiEvent;

@Mixin(value = GuiGraphics.class, priority = Integer.MAX_VALUE)
public class GuiGraphicsMixin {
    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V", at = @At("HEAD"))
    private void ktscript$renderItem(LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, CallbackInfo ci) {
        new GuiEvent.Items.Render.Pre(self(), stack, x, y).post();
    }

    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
    private void ktscript$renderItemDecorations(Font font, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        new GuiEvent.Items.Render.Post(self(), stack, x, y).post();
    }

    @Unique
    private GuiGraphics self() {
        return (GuiGraphics) (Object) this;
    }
}