package adris.altoclef.movement;

import baritone.api.IBaritone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

class MovementEngineAdapterTest {
    public static class Factory {
        public static Object engine(IBaritone baritone) {
            return new Object();
        }
    }

    @AfterEach
    void reset() {
        MovementEngineAdapter.reset();
    }

    private void installFactory() throws Exception {
        Field present = MovementEngineAdapter.class.getDeclaredField("classesPresent");
        present.setAccessible(true);
        present.set(null, true);
        Field factory = MovementEngineAdapter.class.getDeclaredField("factoryMethod");
        factory.setAccessible(true);
        factory.set(null, Factory.class.getMethod("engine", IBaritone.class));
    }

    private Object engine(IBaritone baritone) throws Exception {
        Method method = MovementEngineAdapter.class.getDeclaredMethod("engineFor", IBaritone.class);
        method.setAccessible(true);
        return method.invoke(null, baritone);
    }

    private IBaritone baritone() {
        return (IBaritone) Proxy.newProxyInstance(IBaritone.class.getClassLoader(),
                new Class<?>[]{IBaritone.class}, (proxy, method, args) -> null);
    }

    @Test
    void dispatchAndStatusKeepTheSameEngineUntilBaritoneChanges() throws Exception {
        installFactory();
        IBaritone first = baritone();
        Object initial = engine(first);
        assertSame(initial, engine(first));
        IBaritone second = baritone();
        Object replacement = engine(second);
        assertNotSame(initial, replacement);
        assertSame(replacement, engine(second));
    }

    @Test
    void resetDropsThePreviousEngine() throws Exception {
        installFactory();
        IBaritone baritone = baritone();
        Object initial = engine(baritone);
        MovementEngineAdapter.reset();
        installFactory();
        assertNotSame(initial, engine(baritone));
    }
}
