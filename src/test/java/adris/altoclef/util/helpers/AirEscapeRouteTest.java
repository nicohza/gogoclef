package adris.altoclef.util.helpers;

import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class AirEscapeRouteTest {
    private static AirEscapeRoute.Cell c(int x, int y, int z) {
        return new AirEscapeRoute.Cell(x, y, z);
    }

    @Test void prefersDirectAscentInOpenWater() {
        assertEquals(List.of(c(0, 1, 0), c(0, 2, 0)),
                AirEscapeRoute.find((a, b) -> true, p -> p.y() == 2));
    }

    @Test void swimsAroundCeilingToOffsetOpening() {
        List<AirEscapeRoute.Cell> route = AirEscapeRoute.find(
                (a, b) -> b.y() >= 0 && (b.y() == 0 || b.x() >= 3), p -> p.y() == 2);
        assertEquals(c(3, 2, 0), route.get(route.size() - 1));
        assertTrue(route.contains(c(3, 0, 0)));
        assertFalse(route.contains(c(0, 1, 0)));
    }

    @Test void canDescendAroundAnOverhangBeforeRising() {
        Set<AirEscapeRoute.Cell> tunnel = Set.of(c(0, -1, 0), c(1, -1, 0), c(2, -1, 0),
                c(2, 0, 0), c(2, 1, 0));
        List<AirEscapeRoute.Cell> route = AirEscapeRoute.find((a, b) -> tunnel.contains(b),
                p -> p.equals(c(2, 1, 0)));
        assertEquals(c(0, -1, 0), route.get(0));
        assertEquals(5, route.size());
    }

    @Test void ignoresAirBehindImpassableWalls() {
        assertTrue(AirEscapeRoute.find((a, b) -> false, p -> p.y() > 0).isEmpty());
    }

    @Test void changedObstacleInvalidatesOldRouteAndAllowsAnother() {
        Set<AirEscapeRoute.Cell> blocked = new HashSet<>();
        var before = AirEscapeRoute.find((a, b) -> !blocked.contains(b), p -> p.y() == 2);
        blocked.add(c(0, 1, 0));
        var after = AirEscapeRoute.find((a, b) -> !blocked.contains(b), p -> p.y() == 2);
        assertNotEquals(before, after);
        assertFalse(after.contains(c(0, 1, 0)));
    }

    @Test void sealedLargeWorldHasBoundedSearchCost() {
        AtomicInteger checks = new AtomicInteger();
        assertTrue(AirEscapeRoute.find((a, b) -> true,
                p -> { checks.incrementAndGet(); return false; }).isEmpty());
        assertTrue(checks.get() <= 4096);
    }
}
