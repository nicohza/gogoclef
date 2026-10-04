package kaptainwutax.tungsten.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import kaptainwutax.tungsten.TungstenMod;
import kaptainwutax.tungsten.TungstenModDataContainer;
import kaptainwutax.tungsten.world.VoxelWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.multiplayer.ClientLevel;

@Mixin(Minecraft.class)
public class MixinMinecraftClient {

	@Shadow @Nullable public ClientLevel level;
	@Shadow @Nullable public GameRenderer gameRenderer;

	@Inject(at = @At("HEAD"), method = "tick")
	private void tick(CallbackInfo info) {
		if (gameRenderer != TungstenModDataContainer.gameRenderer) {
	        TungstenModDataContainer.gameRenderer = this.gameRenderer;
		}
		if (Minecraft.getInstance().player != TungstenModDataContainer.player) {
	        TungstenModDataContainer.player = Minecraft.getInstance().player;
		}
		if(this.level == null) {
			TungstenMod.WORLD = null;
		} else if(TungstenMod.WORLD == null) {
			TungstenMod.WORLD = new VoxelWorld(this.level);
		} else if(TungstenMod.WORLD.parent != this.level) {
			TungstenMod.WORLD = new VoxelWorld(this.level);
		}
	}
	
}
