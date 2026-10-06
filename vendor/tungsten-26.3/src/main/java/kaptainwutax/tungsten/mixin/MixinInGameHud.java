package kaptainwutax.tungsten.mixin;

import kaptainwutax.tungsten.util.WindMouseRotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into InGameHud.render() which is called every rendered frame (~60 FPS).
 * Applies WindMouse rotation steps at render frequency so player turning
 * looks smooth rather than snapping every game tick (50 ms).
 *
 * Pattern mirrors altoclef's ClientUIMixin.
 */
@Mixin(Hud.class)
public class MixinInGameHud {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onRenderFrame(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            WindMouseRotation.INSTANCE.applyRenderStep(mc.player);
        }
    }
}
