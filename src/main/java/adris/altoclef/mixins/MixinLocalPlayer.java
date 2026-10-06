package adris.altoclef.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayerEntity {

    public MixinLocalPlayer(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    //#if MC >= 260000
    // LocalPlayer no longer overrides pitch in 26.3. Only yaw needs the superclass view rotation.
    //$$ @Inject(method = "getViewYRot(F)F", at = @At("RETURN"), cancellable = true)
    //$$ private void altoClefViewYaw(float tickDelta, CallbackInfoReturnable<Float> cir) {
    //$$     cir.setReturnValue(super.getViewYRot(tickDelta));
    //$$ }
    //#else
    @Inject(method = "getPitch", at = @At("RETURN"), cancellable = true, require = 0)
    public void getPitch(float tickDelta, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(super.getPitch(tickDelta));
    }

    @Inject(method = "getYaw", at = @At("RETURN"), cancellable = true, require = 0)
    public void getYaw(float tickDelta, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(super.getYaw(tickDelta));
    }
    //#endif
}