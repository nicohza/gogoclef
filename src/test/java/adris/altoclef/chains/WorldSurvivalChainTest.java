package adris.altoclef.chains;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldSurvivalChainTest {
    @Test
    void briefSubmersionDoesNotInterruptTravel() {
        assertFalse(WorldSurvivalChain.needsAirRecovery(true, true, 299, 300, false));
        assertFalse(WorldSurvivalChain.needsAirRecovery(true, true, 151, 300, false));
        assertTrue(WorldSurvivalChain.needsAirRecovery(true, true, 150, 300, false));
        assertTrue(WorldSurvivalChain.needsAirRecovery(true, true, -10, 300, false));
    }

    @Test
    void recoveryContinuesUntilFullAirAndThenAllowsTravel() {
        boolean recovering = false;
        for (int air : new int[]{150, 90, 180, 299}) {
            recovering = WorldSurvivalChain.needsAirRecovery(true, true, air, 300, recovering);
            assertTrue(recovering);
        }
        recovering = WorldSurvivalChain.needsAirRecovery(true, true, 300, 300, recovering);
        assertFalse(recovering);
        assertFalse(WorldSurvivalChain.needsAirRecovery(true, true, 299, 300, recovering));
    }

    @Test
    void leavingWaterOrDisablingProtectionEndsRecovery() {
        assertFalse(WorldSurvivalChain.needsAirRecovery(true, false, 100, 300, true));
        assertFalse(WorldSurvivalChain.needsAirRecovery(false, true, 100, 300, true));
    }

    @Test
    void thresholdTracksMaximumAir() {
        assertFalse(WorldSurvivalChain.needsAirRecovery(true, true, 301, 600, false));
        assertTrue(WorldSurvivalChain.needsAirRecovery(true, true, 300, 600, false));
        assertTrue(WorldSurvivalChain.needsAirRecovery(true, true, 599, 600, true));
        assertFalse(WorldSurvivalChain.needsAirRecovery(true, true, 600, 600, true));
    }
}
