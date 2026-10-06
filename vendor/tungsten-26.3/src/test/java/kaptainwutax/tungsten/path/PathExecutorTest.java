package kaptainwutax.tungsten.path;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class PathExecutorTest {
    private Node node() {
        return new Node(null, (kaptainwutax.tungsten.agent.Agent) null, null, 0);
    }

    @Test
    void ownsItsListAndKeepsSnapshotsStableAcrossReplacement() {
        PathExecutor executor = new PathExecutor(false);
        Node first = node();
        var input = new ArrayList<>(List.of(first));
        executor.setPath(input);
        input.clear();
        var snapshot = executor.getSnapshot();
        assertSame(first, executor.getCurrentNode());
        assertThrows(UnsupportedOperationException.class, () -> executor.getPath().clear());
        executor.addToPath(node());
        executor.setPath(List.of(node()));
        assertEquals(List.of(first), snapshot.path());
        assertEquals(0, snapshot.tick());
        assertTrue(snapshot.isRunning());
    }

    @Test
    void emptyClearedAndRestartedPathsAreSafe() {
        PathExecutor executor = new PathExecutor(false);
        executor.setPath(List.of());
        assertNull(executor.getCurrentNode());
        executor.setPath(null);
        assertFalse(executor.getSnapshot().isRunning());
        assertDoesNotThrow(() -> executor.tick(null, null));
        Node next = node();
        executor.addToPath(next);
        assertSame(next, executor.getCurrentNode());
        assertEquals(0, executor.getCurrentTick());
    }

    @Test
    void concurrentReplacementAppendAndSnapshotStayConsistent() throws Exception {
        PathExecutor executor = new PathExecutor(false);
        Node first = node();
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var writer = pool.submit(() -> {
                start.await();
                for (int i = 0; i < 1000; i++) {
                    executor.setPath(List.of(first));
                    executor.addPath(List.of(node()));
                    executor.setPath(null);
                }
                return null;
            });
            var reader = pool.submit(() -> {
                start.await();
                for (int i = 0; i < 1000; i++) {
                    var snapshot = executor.getSnapshot();
                    assertEquals(0, snapshot.tick());
                    if (snapshot.isRunning()) {
                        assertSame(first, snapshot.path().getFirst());
                        assertTrue(snapshot.path().size() <= 2);
                    } else {
                        assertNull(snapshot.path());
                    }
                }
                return null;
            });
            start.countDown();
            writer.get(10, TimeUnit.SECONDS);
            reader.get(10, TimeUnit.SECONDS);
        }
    }
}
