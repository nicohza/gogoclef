package kaptainwutax.tungsten.world;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/** Client-thread snapshots keep physics searches off Minecraft's mutable entity index. */
public final class EntityCollisionSnapshot {
    private record Snapshot(LevelReader world, List<AABB> boxes) {}
    private static volatile Snapshot current = new Snapshot(null, List.of());

    private EntityCollisionSnapshot() {}

    /** Called on the client thread before follow/executor ticks. */
    public static void refresh(ClientLevel world) {
        List<AABB> boxes = new ArrayList<>();
        for (var entity : world.entitiesForRendering()) {
            // Match EntityGetter.getEntityCollisions(null, box).
            if (EntitySelector.CAN_BE_COLLIDED_WITH.test(entity)) {
                boxes.add(entity.getBoundingBox());
            }
        }
        current = new Snapshot(world, List.copyOf(boxes));
    }

    public static List<VoxelShape> collisions(LevelReader world, AABB box) {
        Snapshot snapshot = current;
        if (snapshot.world != world || box.getSize() < 1.0E-7) return List.of();
        AABB query = box.inflate(1.0E-7);
        List<VoxelShape> result = new ArrayList<>();
        for (AABB obstacle : snapshot.boxes) {
            if (obstacle.intersects(query)) result.add(Shapes.create(obstacle));
        }
        return result;
    }
}
