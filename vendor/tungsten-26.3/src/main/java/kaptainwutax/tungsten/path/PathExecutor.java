package kaptainwutax.tungsten.path;

import kaptainwutax.tungsten.Debug;
import kaptainwutax.tungsten.TungstenMod;
import kaptainwutax.tungsten.TungstenModRenderContainer;
import kaptainwutax.tungsten.helpers.render.RenderHelper;
import kaptainwutax.tungsten.path.blockSpaceSearchAssist.BlockNode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Options;
// import net.minecraft.server.network.ServerPlayerEntity; // server-side disabled
import kaptainwutax.tungsten.agent.TungstenPlayerInput;

import java.util.List;
import java.util.ArrayList;

public class PathExecutor {

    private List<Node> path;
    private int tick = 0;
    protected boolean allowedFlying = false;
    public volatile boolean stop = false;
    public Runnable cb = null;
    public long startTime;
    public List<BlockNode> blockPath = null;
    private boolean isClient;

    public PathExecutor(boolean isClient) {
    	this.isClient = isClient;
    	try {
    		this.startTime = System.currentTimeMillis();
			if (isClient)
	        	this.allowedFlying = TungstenMod.mc.player.getAbilities().mayfly;
		} catch (Exception e) {
			this.allowedFlying = true;
		}
	}

	public synchronized void setPath(List<Node> path) {
		this.cb = null;
		this.startTime = System.currentTimeMillis();
		if (isClient)
			this.allowedFlying = TungstenMod.mc.player.getAbilities().mayfly;
	    stop = false;
        this.path = path == null ? null : new ArrayList<>(path);
    	this.tick = 0;
    	RenderHelper.renderPathCurrentlyExecuted();
	}
	
	public synchronized void addToPath(Node n) {
		addPath(List.of(n));
	}
	
	public synchronized void addPath(List<Node> path) {
		if (stop) {
			setPath(path);
			return;
		}
		if (this.path == null) {
			setPath(path);
			return;
		}
		this.path.addAll(path);
    	RenderHelper.renderPathCurrentlyExecuted();
	}
	
	public synchronized List<Node> getPath() {
		return this.path == null ? null : List.copyOf(this.path);
	}
	
    /** A detached path and its tick, captured under the same executor lock. */
    public record Snapshot(List<Node> path, int tick) {
        public boolean isRunning() {
            return path != null && tick <= path.size();
        }
    }

    public synchronized Snapshot getSnapshot() {
        return new Snapshot(getPath(), tick);
    }

	public synchronized Node getCurrentNode() {
		if (this.path == null || this.path.isEmpty()) return null;
		if (this.tick >= this.path.size()) return this.path.get(this.path.size()-1);
		return this.path.get(this.tick);
	}
	

	public synchronized int getCurrentTick() {
		return this.tick;
	}


	public synchronized boolean isRunning() {
        return this.path != null && this.tick <= this.path.size();
    }


    // Server-side tick disabled: requires ServerPlayerEntity.setPlayerInput() (MC 1.21.4+ only)
    // public void tick(ServerPlayerEntity player) { ... }
    
    public synchronized void tick(LocalPlayer player, Options options) {
        if (this.path == null) return;
    	player.getAbilities().mayfly = false;
    	if(TungstenMod.pauseKeyBinding.isDown() || stop) {
    		this.tick = this.path.size();
    		// player.input.playerInput = ... // MC 1.21: Input has no playerInput field
		    options.keyUp.setDown(false);
		    options.keyDown.setDown(false);
		    options.keyLeft.setDown(false);
		    options.keyRight.setDown(false);
		    options.keyJump.setDown(false);
		    options.keyShift.setDown(false);
		    options.keySprint.setDown(false);
		    player.getAbilities().mayfly = allowedFlying;
		    this.path = null;
		    stop = false;
		    TungstenModRenderContainer.RUNNING_PATH_RENDERER.clear();
		    TungstenModRenderContainer.BLOCK_PATH_RENDERER.clear();
    		return;
    	}
    	if(this.tick == this.path.size()) {
    		long endTime = System.currentTimeMillis();
    		long elapsedTime = endTime - startTime;
    		long minutes = (elapsedTime / 1000) / 60;
            long seconds = (elapsedTime / 1000) % 60;
            long milliseconds = elapsedTime % 1000;
            
            Debug.logMessage("Time taken to execute: " + minutes + " minutes, " + seconds + " seconds, " + milliseconds + " milliseconds");
    		
		    options.keyUp.setDown(false);
		    options.keyDown.setDown(false);
		    options.keyLeft.setDown(false);
		    options.keyRight.setDown(false);
		    options.keyJump.setDown(false);
		    options.keyShift.setDown(false);
		    options.keySprint.setDown(false);
		    player.getAbilities().mayfly = allowedFlying;
		    this.path = null;
		    stop = false;
		    TungstenModRenderContainer.RUNNING_PATH_RENDERER.clear();
		    TungstenModRenderContainer.BLOCK_PATH_RENDERER.clear();
			player.setDeltaMovement(0, 0, 0);
		    if (cb != null) {
		    	cb.run();
		    	cb = null;
		    }
	    } else {
		    Node node = this.path.get(this.tick);

		    if(node.input != null) {
			    player.setYRot(node.input.yaw);
			    player.setXRot(node.input.pitch);
			    // player.stopGliding() removed in MC 1.21
	    		options.keyUp.setDown(node.input.forward);
			    options.keyDown.setDown(node.input.back);
			    options.keyLeft.setDown(node.input.left);
			    options.keyRight.setDown(node.input.right);
			    options.keyJump.setDown(node.input.jump);
			    options.keyShift.setDown(node.input.sneak);
			    options.keySprint.setDown(node.input.sprint);
		    }
//		    if(this.tick != 0 && options != null) {
//			    this.path.get(this.tick - 1).agent.compare(player, optionsToPlayerInput(options), true);
//		    }
		    int idx = TungstenModRenderContainer.RUNNING_PATH_RENDERER.size()-1;
		    if (!TungstenModRenderContainer.RUNNING_PATH_RENDERER.isEmpty() && this.tick != 0) {
		    	try {
			    	TungstenModRenderContainer.RUNNING_PATH_RENDERER.remove(TungstenModRenderContainer.RUNNING_PATH_RENDERER.toArray()[idx]);
			    	if (TungstenMod.renderPositonBoxes && TungstenModRenderContainer.RUNNING_PATH_RENDERER.size() > 1) {
			    		TungstenModRenderContainer.RUNNING_PATH_RENDERER.remove(TungstenModRenderContainer.RUNNING_PATH_RENDERER.toArray()[idx-1]);
			    	}
				} catch (Exception e) {
					// TODO: handle exception
				}
		    }
	    }
	    this.tick++;
    }
    
    
    public static TungstenPlayerInput optionsToPlayerInput(Options options) {
    	return new TungstenPlayerInput(options.keyUp.isDown(), options.keyDown.isDown(), options.keyLeft.isDown(), options.keyRight.isDown(), options.keyJump.isDown(), options.keyShift.isDown(), options.keySprint.isDown());
    }

}
