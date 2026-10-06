package kaptainwutax.tungsten.mixin;

import kaptainwutax.tungsten.TungstenModDataContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public abstract class MixinWorldChunk {
    @Shadow public abstract Level getLevel();

    @Inject(method = "replaceWithPacketData", at = @At("RETURN"))
    private void tungsten$refreshWorld(CallbackInfo ci) {
        TungstenModDataContainer.world = this.getLevel();
    }
}
