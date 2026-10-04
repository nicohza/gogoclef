package adris.altoclef.release;

import baritone.api.BaritoneAPI;
import net.fabricmc.api.ModInitializer;

/** Startup preset for the separately packaged Minecraft 26.3 kinematic release. */
public final class KinematicRelease implements ModInitializer {
    @Override
    public void onInitialize() {
        // AltoClef reads this property when its title-screen initialization runs.
        // Retain an explicit launcher override, including -Dtenorclef.kinematic=false.
        if (System.getProperty("tenorclef.kinematic") == null) {
            System.setProperty("tenorclef.kinematic", "true");
        }
        // A previous Tungsten installation may have persisted a different backend.
        BaritoneAPI.getSettings().movementBackend.value = "baritone";
        System.out.println("[TenorClef Kinematic Release] movementBackend=baritone, kinematic="
                + System.getProperty("tenorclef.kinematic"));
    }
}
