package adris.altoclef.util.helpers;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/** Bounded search through traversable swim steps, independent of Minecraft. */
public final class AirEscapeRoute {
    public record Cell(int x, int y, int z) {
        Cell offset(int dx, int dy, int dz) { return new Cell(x + dx, y + dy, z + dz); }
    }

    private static final int[][] STEPS = {{0, 1, 0}, {1, 0, 0}, {-1, 0, 0},
            {0, 0, 1}, {0, 0, -1}, {0, -1, 0}};
    private AirEscapeRoute() {}

    /** Returned waypoints exclude the start; an empty route means no reachable air was found. */
    public static List<Cell> find(BiPredicate<Cell, Cell> canMove, Predicate<Cell> breathable) {
        Cell start = new Cell(0, 0, 0);
        ArrayDeque<Cell> queue = new ArrayDeque<>();
        Map<Cell, Cell> parent = new HashMap<>();
        queue.add(start);
        parent.put(start, null);
        int examined = 0;
        while (!queue.isEmpty() && examined++ < 4096) {
            Cell here = queue.removeFirst();
            if (breathable.test(here)) {
                List<Cell> path = new ArrayList<>();
                for (Cell p = here; !p.equals(start); p = parent.get(p)) path.add(p);
                Collections.reverse(path);
                return path;
            }
            for (int[] step : STEPS) {
                Cell next = here.offset(step[0], step[1], step[2]);
                if (Math.abs(next.x()) > 16 || Math.abs(next.z()) > 16
                        || next.y() < -8 || next.y() > 32 || parent.containsKey(next)) continue;
                if (canMove.test(here, next)) {
                    parent.put(next, here);
                    queue.addLast(next);
                }
            }
        }
        return List.of();
    }
}
