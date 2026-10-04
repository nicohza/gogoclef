package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import baritone.api.utils.input.Input;

/** Interrupt travel and swim upward until the player can breathe again. */
public final class SurfaceForAirTask extends Task {
    @Override
    protected void onStart() {
        AltoClef mod = AltoClef.getInstance();
        mod.getMovement().cancel();
        mod.getClientBaritone().getPathingBehavior().forceCancel();
        mod.getClientBaritone().getInputOverrideHandler().clearAllKeys();
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.JUMP, true);
        mod.getInputControls().hold(Input.JUMP);
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef mod = AltoClef.getInstance();
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.JUMP, false);
        mod.getInputControls().release(Input.JUMP);
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
