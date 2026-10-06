package kaptainwutax.tungsten.task;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrailTrackerTest {
    @SuppressWarnings("unchecked")
    private List<Object> points(TrailTracker tracker) throws Exception {
        Field field = TrailTracker.class.getDeclaredField("trail");
        field.setAccessible(true);
        return (List<Object>) field.get(tracker);
    }

    private TrailTracker seededTrail() throws Exception {
        TrailTracker tracker = new TrailTracker("test");
        Class<?> point = Class.forName(TrailTracker.class.getName() + "$TrailPoint");
        Constructor<?> constructor = point.getDeclaredConstructor(Vec3.class);
        constructor.setAccessible(true);
        // Recorded ground waypoints, without requiring a running client world.
        for (int i = 0; i < 3; i++) {
            points(tracker).add(constructor.newInstance(new Vec3(8 + i, 6, 0)));
        }
        return tracker;
    }

    @Test
    void nearbyTargetOnAnotherFloorKeepsItsTrail() throws Exception {
        TrailTracker tracker = seededTrail();
        Vec3 target = new Vec3(8, 6, 0);
        assertTrue(tracker.update(Vec3.ZERO, target));
        for (int tick = 0; tick < 60; tick++) {
            assertTrue(tracker.update(Vec3.ZERO, target), "Trail toggled off at tick " + tick);
        }
    }

    @Test
    void nearbyTargetOnSameLevelLeavesTrailMode() throws Exception {
        TrailTracker tracker = seededTrail();
        assertTrue(tracker.update(Vec3.ZERO, new Vec3(8, 6, 0)));
        assertFalse(tracker.update(Vec3.ZERO, new Vec3(8, 0, 0)));
    }

    @Test
    void exhaustedTrailLeavesTrailMode() throws Exception {
        TrailTracker tracker = seededTrail();
        assertTrue(tracker.update(Vec3.ZERO, new Vec3(25, 0, 0)));
        points(tracker).clear();
        assertFalse(tracker.update(Vec3.ZERO, new Vec3(8, 6, 0)));
    }
}
