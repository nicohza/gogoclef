package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.AirEscapeRoute;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import baritone.api.utils.input.Input;

/** Interrupt travel and swim upward until the player can breathe again. */
public final class SurfaceForAirTask extends Task {
    private final ArrayDeque<Vec3d> route = new ArrayDeque<>();
    private int searchCooldown;
    private int stalledTicks;
    private double highestY;

    @Override
    protected void onStart() {
        AltoClef mod = AltoClef.getInstance();
        route.clear();
        searchCooldown = 0;
        stalledTicks = 0;
        highestY = mod.getPlayer().getY();
        mod.getMovement().cancel();
        mod.getClientBaritone().getPathingBehavior().forceCancel();
        mod.getClientBaritone().getInputOverrideHandler().clearAllKeys();
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();
        Vec3d pos = mod.getPlayer().getPos();
        if (pos.y > highestY + 0.1) {
            highestY = pos.y;
            stalledTicks = 0;
        } else {
            stalledTicks++;
        }
        if (searchCooldown > 0) searchCooldown--;
        // Once breathing, stay at the surface while air replenishes.
        if (!mod.getPlayer().isSubmergedInWater()) route.clear();
        boolean blockedAbove = !mod.getWorld().isSpaceEmpty(mod.getPlayer(),
                mod.getPlayer().getBoundingBox().offset(0, 0.4, 0));
        if (route.isEmpty() && mod.getPlayer().isSubmergedInWater()
                && (blockedAbove || stalledTicks >= 20) && searchCooldown == 0) {
            findRoute(mod);
            searchCooldown = 10;
        }
        while (!route.isEmpty() && pos.squaredDistanceTo(route.peekFirst()) < 0.16) {
            route.removeFirst();
        }
        if (!route.isEmpty()) {
            Vec3d target = route.peekFirst();
            // Recheck the swept player volume: blocks/doors may change after planning.
            if (!clearStep(mod, pos, target)) {
                route.clear();
                searchCooldown = 0;
            } else {
                double dx = target.x - pos.x, dz = target.z - pos.z;
                double dy = target.y - pos.y;
                boolean forward = dx * dx + dz * dz > 0.04;
                if (forward) mod.getInputControls().forceLook(
                        (float) Math.toDegrees(Math.atan2(-dx, dz)), 0);
                input(mod, Input.MOVE_FORWARD, forward);
                input(mod, Input.SNEAK, dy < -0.3);
                input(mod, Input.JUMP, dy >= -0.1);
                setDebugState("Following open route to air");
                return null;
            }
        }
        input(mod, Input.MOVE_FORWARD, false);
        input(mod, Input.SNEAK, false);
        input(mod, Input.JUMP, true);
        setDebugState("Swimming upward");
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef mod = AltoClef.getInstance();
        route.clear();
        input(mod, Input.JUMP, false);
        input(mod, Input.MOVE_FORWARD, false);
        input(mod, Input.SNEAK, false);
    }

    private void findRoute(AltoClef mod) {
        Vec3d origin = mod.getPlayer().getPos();
        for (AirEscapeRoute.Cell cell : AirEscapeRoute.find(
                (from, to) -> clearStep(mod, at(origin, from), at(origin, to)),
                cell -> mod.getWorld().getFluidState(new BlockPos(
                        (int) Math.floor(origin.x + cell.x()),
                        (int) Math.floor(origin.y + cell.y() + mod.getPlayer().getStandingEyeHeight()),
                        (int) Math.floor(origin.z + cell.z()))).isEmpty())) {
            route.addLast(at(origin, cell));
        }
    }

    private static Vec3d at(Vec3d origin, AirEscapeRoute.Cell cell) {
        return origin.add(cell.x(), cell.y(), cell.z());
    }

    private static boolean clearStep(AltoClef mod, Vec3d from, Vec3d to) {
        Vec3d pos = mod.getPlayer().getPos();
        Box body = mod.getPlayer().getBoundingBox();
        Box swept = body.offset(from.subtract(pos)).union(body.offset(to.subtract(pos)));
        // Unloaded terrain is not an escape route; do not swim through lava either.
        for (int x = (int) Math.floor(swept.minX); x <= (int) Math.floor(swept.maxX); x++) {
            for (int y = (int) Math.floor(swept.minY); y <= (int) Math.floor(swept.maxY); y++) {
                for (int z = (int) Math.floor(swept.minZ); z <= (int) Math.floor(swept.maxZ); z++) {
                    BlockPos block = new BlockPos(x, y, z);
                    if (!mod.getChunkTracker().isChunkLoaded(block)
                            || mod.getWorld().getBlockState(block).getBlock() == net.minecraft.block.Blocks.LAVA) {
                        return false;
                    }
                }
            }
        }
        return mod.getWorld().isSpaceEmpty(mod.getPlayer(), swept);
    }

    private static void input(AltoClef mod, Input input, boolean held) {
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(input, held);
        if (held) mod.getInputControls().hold(input);
        else mod.getInputControls().release(input);
    }

    @Override
    public boolean isFinished() {
        AltoClef mod = AltoClef.getInstance();
        return !mod.getPlayer().isTouchingWater()
                || mod.getPlayer().getAir() >= mod.getPlayer().getMaxAir();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof SurfaceForAirTask;
    }

    @Override
    protected String toDebugString() {
        return "Surface for air";
    }
}
