package kaptainwutax.tungsten;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kaptainwutax.tungsten.commandsystem.CommandExecutor;
import kaptainwutax.tungsten.path.PathExecutor;
import kaptainwutax.tungsten.path.PathFinder;
import kaptainwutax.tungsten.render.Renderer;
import kaptainwutax.tungsten.world.VoxelWorld;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelReader;

public class TungstenMod implements ClientModInitializer {

	public static final String MOD_ID = "tungsten";
//    public static final ModMetadata MOD_META;
    public static final String NAME;

    public static Minecraft mc = null;
    public static Player player = null;
    public static LevelReader world = null;
	public static Vec3 TARGET = new Vec3(0.5D, 10.0D, 0.5D);
	public static clickModeEnum clickMode = clickModeEnum.OFF;
	public static final Logger LOG;
	public static VoxelWorld WORLD;
	public static KeyMapping pauseKeyBinding;
	public static KeyMapping runKeyBinding;
	public static KeyMapping runBlockSearchKeyBinding;
	public static KeyMapping createGoalKeyBinding;
    private static CommandExecutor _commandExecutor;
    public static boolean renderPositonBoxes = true;
	
	
	static {
		// MOD_META = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata();
        NAME = "Tungsten";
        // DEV_BUILD = MOD_META.getCustomValue(TungstenMod.MOD_ID + ":devbuild").getAsString();
        LOG = LoggerFactory.getLogger(NAME);
		
	}

	@Override
	public void onInitializeClient() {
		TungstenConfig.load();
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("tungsten", "controls"));
		TungstenModDataContainer.EXECUTOR = new PathExecutor(true);
		pauseKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
	            "key.tungsten.pause", // The translation key of the keybinding's name
	            InputConstants.Type.KEYBOARD, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
	            InputConstants.KEY_P, // The keycode of the key
	            category // The translation key of the keybinding's category.
        ));
		runKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
	            "key.tungsten.run", // The translation key of the keybinding's name
	            InputConstants.Type.KEYBOARD, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
	            InputConstants.KEY_G, // The keycode of the key
	            category // The translation key of the keybinding's category.
        ));
		runBlockSearchKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
	            "key.tungsten.run_block_search", // The translation key of the keybinding's name
	            InputConstants.Type.KEYBOARD, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
	            InputConstants.KEY_J, // The keycode of the key
	            category // The translation key of the keybinding's category.
        ));
		createGoalKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
	            "key.tungsten.create_goal", // The translation key of the keybinding's name
	            InputConstants.Type.KEYBOARD, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
	            InputConstants.KEY_H, // The keycode of the key
	            category // The translation key of the keybinding's category.
        ));
        _commandExecutor = new CommandExecutor(this);

        // Global minecraft client accessor
        mc = Minecraft.getInstance();
        TungstenModDataContainer.player = mc.player;
        TungstenModDataContainer.world = mc.level;
        TungstenModDataContainer.gameRenderer = mc.gameRenderer;

        initializeCommands();

    	ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable toRun = new Runnable() {
            public void run() {
	        	if (!TungstenModRenderContainer.ERROR.isEmpty()) {
	        		TungstenModRenderContainer.ERROR.clear();
	        	}
            }
        };
        scheduler.scheduleAtFixedRate(toRun, 1, 15, TimeUnit.SECONDS);
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            kaptainwutax.tungsten.task.FollowPlayerTask.stop();
            TungstenModDataContainer.EXECUTOR.stop = true;
            TungstenModDataContainer.PATHFINDER.shutdown();
            Thread blockSearch = kaptainwutax.tungsten.path.blockSpaceSearchAssist.BlockSpacePathFinder.thread;
            if (blockSearch != null) blockSearch.interrupt();
            scheduler.shutdownNow();
        });
    
        ClientTickEvents.START_CLIENT_TICK.register((a) -> {
        	
        	boolean isRunning = TungstenModDataContainer.PATHFINDER.active.get() || TungstenModDataContainer.EXECUTOR.isRunning();
        	if (!isRunning) {
	        	if (!TungstenModRenderContainer.BLOCK_PATH_RENDERER.isEmpty()) {
	        		TungstenModRenderContainer.BLOCK_PATH_RENDERER.clear();
	        	}
	        	if (!TungstenModRenderContainer.RUNNING_PATH_RENDERER.isEmpty()) {
	        		TungstenModRenderContainer.RUNNING_PATH_RENDERER.clear();
	        	}
	        	if (!TungstenModRenderContainer.RENDERERS.isEmpty()) {
	        		TungstenModRenderContainer.RENDERERS.clear();
	        	}
	        	if (!TungstenModRenderContainer.TEST.isEmpty()) {
	        		TungstenModRenderContainer.TEST.clear();
	        	}
        	}
        	if (clickMode != clickModeEnum.OFF && mc.options.keyUse.isDown() && !isRunning) {
        		
        		 Camera camera = mc.gameRenderer.mainCamera();
                 Vec3 cameraPos = camera.position();

                 // Calculate the direction the camera is looking based on its pitch and yaw, and extend this direction 210 units away from the camera position
                 // 210 is used here as the maximum distance of 200 blocks
                 // This is done to be able to set target while in freecam
                 Vec3 direction = Vec3.directionFromRotation(camera.xRot(), camera.yRot()).scale(210);
                 Vec3 targetPos = cameraPos.add(direction);
                 
                 ClipContext context = new ClipContext(
                         cameraPos,   // start position of the ray
                         targetPos,   // end position of the ray
                         ClipContext.Block.OUTLINE,
                         ClipContext.Fluid.NONE,
                         mc.player
                 );
                 
                 HitResult hitResult = mc.level.clip(context);
                 
                 if (hitResult.getType() == HitResult.Type.BLOCK) {
                     BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
	                 if (mc.level.getBlockState(pos).useWithoutItem(mc.level, mc.player, (BlockHitResult) hitResult) != InteractionResult.PASS) return;
	
		                 BlockState state = mc.level.getBlockState(pos);
		
		                 VoxelShape shape = state.getCollisionShape(mc.level, pos);
//		                 if (shape.isEmpty()) shape = state.getOutlineShape(mc.world, pos);
		
		                 double height = shape.isEmpty() ? 0 : shape.max(Direction.Axis.Y);
		
		                 Vec3 newPos = new Vec3(pos.getX() + 0.5, pos.getY() + height, pos.getZ() + 0.5);
		         		TungstenMod.TARGET = newPos;
		         		

		        		if (clickMode == clickModeEnum.GOTO && !TungstenModDataContainer.PATHFINDER.active.get()) {
		        			TungstenModDataContainer.PATHFINDER.find(TungstenMod.mc.level, TARGET, TungstenMod.mc.player);
		        		}
	        		}
        		}
        		
        		
        });
	}
	
	 public static String getCommandPrefix() {
		 return ";";
	 }
	 
	// List all command sources here.
    private void initializeCommands() {
        try {
            // This creates the commands. If you want any more commands feel free to initialize new command lists.
            new TungstenCommands(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	 
	 /**
     * Executes commands
     */
    public static CommandExecutor getCommandExecutor() {
        return _commandExecutor;
    }
    
    public enum clickModeEnum {
    	OFF,
    	PLACE_GOAL,
    	GOTO
    }

}
