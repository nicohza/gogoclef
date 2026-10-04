package kaptainwutax.tungsten.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;

import kaptainwutax.tungsten.Debug;
import kaptainwutax.tungsten.TungstenMod;
import kaptainwutax.tungsten.TungstenModDataContainer;
import kaptainwutax.tungsten.agent.Agent;
import kaptainwutax.tungsten.agent.TungstenPlayerInput;
import kaptainwutax.tungsten.task.FollowEntityTask;
import kaptainwutax.tungsten.task.FollowPlayerTask;
import kaptainwutax.tungsten.path.blockSpaceSearchAssist.BlockSpacePathFinder;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

@Mixin(LocalPlayer.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayer {

	// MC 1.21: AbstractClientPlayerEntity constructor takes (ClientWorld, GameProfile) only
	// PlayerPublicKey was removed in MC 1.20.5
	public MixinClientPlayerEntity(ClientLevel world, GameProfile profile) {
		super(world, profile);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	public void start(CallbackInfo ci) {
        kaptainwutax.tungsten.world.EntityCollisionSnapshot.refresh(Minecraft.getInstance().level);
		FollowEntityTask.tick(this.level(), (LocalPlayer)(Object)this);
		FollowPlayerTask.tick(this.level(), (LocalPlayer)(Object)this);

		if(TungstenModDataContainer.EXECUTOR.isRunning()) {
			TungstenModDataContainer.EXECUTOR.tick((LocalPlayer)(Object)this, Minecraft.getInstance().options);
		}

		if(!this.getAbilities().flying) {
			Agent.INSTANCE = Agent.of((LocalPlayer)(Object)this, Minecraft.getInstance().options);
			Agent.INSTANCE.tick(this.level());
		}

		if(TungstenMod.runKeyBinding.isDown() && !TungstenModDataContainer.PATHFINDER.active.get() && !TungstenModDataContainer.EXECUTOR.isRunning()) {
			TungstenModDataContainer.PATHFINDER.find(this.level(), TungstenMod.TARGET, TungstenMod.mc.player);
		}
		if(TungstenMod.runBlockSearchKeyBinding.isDown() && !TungstenModDataContainer.PATHFINDER.active.get()) {
			BlockSpacePathFinder.find(level(), TungstenMod.TARGET, TungstenMod.mc.player);
		}
		if (TungstenMod.pauseKeyBinding.isDown()) {
			try {

	        	if((TungstenModDataContainer.PATHFINDER.active.get() || TungstenModDataContainer.EXECUTOR.isRunning())) {
	        		TungstenModDataContainer.PATHFINDER.stop.set(true);
	        		TungstenModDataContainer.EXECUTOR.stop = true;
					Debug.logMessage("Stopped!");
	    		} else {
					Debug.logMessage("Nothing to stop.");
	    		}


			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		if (TungstenMod.pauseKeyBinding.isDown()) {
			TungstenModDataContainer.PATHFINDER.stop.set(true);
		}
		if (TungstenMod.createGoalKeyBinding.isDown()) {
			BlockPos cameraBlockPos = TungstenMod.mc.gameRenderer.mainCamera().blockPosition();
			TungstenMod.TARGET = new Vec3(cameraBlockPos.getX() + 0.5, cameraBlockPos.getY() - 1, cameraBlockPos.getZ() + 0.5);
		}
	}

	@Inject(method = "tick", at = @At(value = "RETURN"))
	public void end(CallbackInfo ci) {
		// MC 1.21: Input has no playerInput field; build TungstenPlayerInput from input fields
		LocalPlayer self = (LocalPlayer)(Object)this;
		TungstenPlayerInput currentInput = new TungstenPlayerInput(
			self.input.keyPresses.forward(),
			self.input.keyPresses.backward(),
			self.input.keyPresses.left(),
			self.input.keyPresses.right(),
			self.input.keyPresses.jump(),
			self.input.keyPresses.shift(),
			self.isSprinting()
		);
		var snapshot = TungstenModDataContainer.EXECUTOR.getSnapshot();
		if (snapshot.isRunning() && snapshot.tick() > 0) {
			snapshot.path().get(snapshot.tick() - 1).agent.compare(self, currentInput, true);
		} else if(!this.getAbilities().flying && Agent.INSTANCE != null) {
			Agent.INSTANCE.compare(self, currentInput, false);
		}
	}

	@Inject(method="getViewYRot", at=@At("RETURN"), cancellable = true)
	public void getYaw(float tickDelta, CallbackInfoReturnable<Float> ci) {
		if(TungstenModDataContainer.EXECUTOR.isRunning()) {
			ci.setReturnValue(super.getViewYRot(tickDelta));
		}
	}

}
