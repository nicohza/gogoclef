package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.knowledge.KnowledgeFact;
import adris.altoclef.knowledge.KnowledgeFacts;
import adris.altoclef.knowledge.WorldKnowledge;
import adris.altoclef.tasksystem.FailureReason;
import adris.altoclef.tasksystem.RecoveryDecision;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.control.MovementController;
import adris.altoclef.util.baritone.GoalFollowEntity;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import baritone.api.utils.input.Input;
//#if MC < 260000
import net.minecraft.block.*;
//#else
//$$ import net.minecraft.world.level.block.Block;
//$$ import net.minecraft.world.level.block.DoorBlock;
//$$ import net.minecraft.world.level.block.FenceBlock;
//$$ import net.minecraft.world.level.block.FenceGateBlock;
//$$ import net.minecraft.world.level.block.FlowerBlock;
//#endif
import adris.altoclef.multiversion.versionedfields.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class GetToEntityTask extends Task implements ITaskRequiresGrounded {
    private final MovementProgressChecker stuckCheck = new MovementProgressChecker();
    private final MovementProgressChecker _progress = new MovementProgressChecker();
    private final TimeoutWanderTask _wanderTask = new TimeoutWanderTask(5);
    private final Entity _entity;
    private final double _closeEnoughDistance;
    Block[] annoyingBlocks = new Block[]{
            Blocks.VINE,
            Blocks.NETHER_SPROUTS,
            Blocks.CAVE_VINES,
            Blocks.CAVE_VINES_PLANT,
            Blocks.TWISTING_VINES,
            Blocks.TWISTING_VINES_PLANT,
            Blocks.WEEPING_VINES_PLANT,
            Blocks.LADDER,
            Blocks.BIG_DRIPLEAF,
            Blocks.BIG_DRIPLEAF_STEM,
            Blocks.SMALL_DRIPLEAF,
            Blocks.TALL_GRASS,
            Blocks.SHORT_GRASS,
            Blocks.SWEET_BERRY_BUSH
    };
    private Task _unstuckTask = null;
    //#if MC >= 260300
    private boolean _useGoalFallback;
    private long _followStartedMs;
    //#endif

    public GetToEntityTask(Entity entity, double closeEnoughDistance) {
        _entity = entity;
        _closeEnoughDistance = closeEnoughDistance;
    }

    public GetToEntityTask(Entity entity) {
        this(entity, 1);
    }

    private static BlockPos[] generateSides(BlockPos pos) {
        return new BlockPos[]{
                pos.add(1,0,0),
                pos.add(-1,0,0),
                pos.add(0,0,1),
                pos.add(0,0,-1),
                pos.add(1,0,-1),
                pos.add(1,0,1),
                pos.add(-1,0,-1),
                pos.add(-1,0,1)
        };
    }

    private boolean isAnnoying(AltoClef mod, BlockPos pos) {
        if (annoyingBlocks != null) {
            for (Block AnnoyingBlocks : annoyingBlocks) {
                return mod.getWorld().getBlockState(pos).getBlock() == AnnoyingBlocks ||
                        mod.getWorld().getBlockState(pos).getBlock() instanceof DoorBlock ||
                        mod.getWorld().getBlockState(pos).getBlock() instanceof FenceBlock ||
                        mod.getWorld().getBlockState(pos).getBlock() instanceof FenceGateBlock ||
                        mod.getWorld().getBlockState(pos).getBlock() instanceof FlowerBlock;
            }
        }
        return false;
    }

    // This happens all the time in mineshafts and swamps/jungles
    private BlockPos stuckInBlock(AltoClef mod) {
        BlockPos p = mod.getPlayer().getBlockPos();
        if (isAnnoying(mod, p)) return p;
        if (isAnnoying(mod, p.up())) return p.up();
        BlockPos[] toCheck = generateSides(p);
        for (BlockPos check : toCheck) {
            if (isAnnoying(mod, check)) {
                return check;
            }
        }
        BlockPos[] toCheckHigh = generateSides(p.up());
        for (BlockPos check : toCheckHigh) {
            if (isAnnoying(mod, check)) {
                return check;
            }
        }
        return null;
    }

    private Task getFenceUnstuckTask() {
        return new SafeRandomShimmyTask();
    }

    @Override
    protected void onStart() {
        AltoClef.getInstance().getMovement().cancel();
        _progress.reset();
        stuckCheck.reset();
        _wanderTask.resetWander();
        //#if MC >= 260300
        // Keep the deadline across combat interruptions of this same approach.
        if (_followStartedMs == 0) _followStartedMs = System.currentTimeMillis();
        //#endif
    }

    //#if MC >= 260300
    private void useGoalFallback(AltoClef mod) {
        _useGoalFallback = true;
        mod.getMovement().cancel();
        _progress.reset();
        stuckCheck.reset();
        adris.altoclef.Debug.logMessage("Entity approach: Tungsten stalled; falling back to Ostinato follow goal");
    }
    //#endif

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // Phase 4/5: entity validity via KnowledgeFact (stale/low confidence → TARGET_UNAVAILABLE)
        WorldKnowledge knowledge = mod.getWorldKnowledge();
        KnowledgeFact<Boolean> aliveFact = knowledge.entityAliveFact(_entity);
        long now = knowledge.currentTick();
        // Fresh SENSOR dead, unknown, or decayed MEMORY below threshold → unavailable.
        // maxAge 40 ticks (~2s), half-life 20, min confidence 0.35
        boolean targetReliable = _entity != null
                && KnowledgeFacts.isReliable(aliveFact, now, 40L, 20L, 0.35)
                && Boolean.TRUE.equals(aliveFact.getValue());
        if (!targetReliable) {
            // Phase 6: TARGET_UNAVAILABLE → ABORT (no easy retarget without planner)
            failWithRecovery(FailureReason.TARGET_UNAVAILABLE,
                    "Follow target entity unavailable (stale/low confidence)");
            setDebugState("Target entity unavailable");
            return null;
        }

        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            _progress.reset();
        }
        if (WorldHelper.isInNetherPortal()) {
            if (!mod.getClientBaritone().getPathingBehavior().isPathing()) {
                setDebugState("Getting out from nether portal");
                mod.getInputControls().hold(Input.SNEAK);
                mod.getInputControls().hold(Input.MOVE_FORWARD);
                return null;
            } else {
                mod.getInputControls().release(Input.SNEAK);
                mod.getInputControls().release(Input.MOVE_BACK);
                mod.getInputControls().release(Input.MOVE_FORWARD);
            }
        } else {
            if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
                mod.getInputControls().release(Input.SNEAK);
                mod.getInputControls().release(Input.MOVE_BACK);
                mod.getInputControls().release(Input.MOVE_FORWARD);
            }
        }
        if (_unstuckTask != null && _unstuckTask.isActive() && !_unstuckTask.isFinished() && stuckInBlock(mod) != null) {
            setDebugState("Getting unstuck from block.");
            stuckCheck.reset();
            // Stop other tasks, we are JUST shimmying
            mod.getClientBaritone().getCustomGoalProcess().onLostControl();
            mod.getClientBaritone().getExploreProcess().onLostControl();
            return _unstuckTask;
        }
        if (!_progress.check(mod) || !stuckCheck.check(mod)) {
            BlockPos blockStuck = stuckInBlock(mod);
            if (blockStuck != null) {
                _unstuckTask = getFenceUnstuckTask();
                return _unstuckTask;
            }
            stuckCheck.reset();
        }
        if (_wanderTask.isActive() && !_wanderTask.isFinished()) {
            _progress.reset();
            setDebugState("Failed to get to target, wandering for a bit.");
            return _wanderTask;
        }

        // Phase 2/3: MovementController (MovementEngineAdapter) follow; fall back to CustomGoalProcess.
        MovementController movement = mod.getMovement();
        //#if MC >= 260300
        if (!_useGoalFallback && System.currentTimeMillis() - _followStartedMs >= 15_000
                && !mod.getPlayer().closerThan(_entity, _closeEnoughDistance)
                && "TUNGSTEN".equals(adris.altoclef.movement.MovementEngineAdapter.requestedBackend())) {
            useGoalFallback(mod);
        }
        //#endif
        if (!movement.isPathingOrActive()) {
            //#if MC >= 260300
            if (_useGoalFallback) {
                // This dynamic goal stays with Ostinato and permits mining/placing
                // when the target is separated from us by blocks.
                mod.getClientBaritone().getCustomGoalProcess().setGoalAndPath(new GoalFollowEntity(_entity, _closeEnoughDistance));
            } else
            //#endif
            if (!movement.followEntity(_entity, _closeEnoughDistance)) {
                movement.ensureGoalAndPath(new GoalFollowEntity(_entity, _closeEnoughDistance));
            }
        }

        if (mod.getPlayer().isInRange(_entity, _closeEnoughDistance)) {
            _progress.reset();
        }

        if (!_progress.check(mod)) {
            //#if MC >= 260300
            if (!_useGoalFallback && "TUNGSTEN".equals(adris.altoclef.movement.MovementEngineAdapter.requestedBackend())) {
                useGoalFallback(mod);
                return null;
            }
            //#endif
            // Phase 6: TIMEOUT → RETRY/ALTERNATE_PATH then ABORT
            RecoveryDecision d = failWithRecovery(FailureReason.TIMEOUT,
                    "Failed to make progress toward entity");
            if (d.isTerminal()) {
                setDebugState("Entity approach retries exhausted");
                return null;
            }
            return _wanderTask;
        }

        setDebugState("Going to entity");
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef.getInstance().getMovement().cancel();
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GetToEntityTask task) {
            return task._entity.equals(_entity) && Math.abs(task._closeEnoughDistance - _closeEnoughDistance) < 0.1;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Approach entity " + _entity.getType().getTranslationKey();
    }
}
