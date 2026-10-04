package kaptainwutax.tungsten.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import kaptainwutax.tungsten.TungstenMod;
import kaptainwutax.tungsten.TungstenModRenderContainer;
import kaptainwutax.tungsten.render.Color;
import kaptainwutax.tungsten.render.Cuboid;
import kaptainwutax.tungsten.render.Renderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class MixinDebugRenderer {
    @Inject(method = "emitGizmos", at = @At("RETURN"))
    private void tungsten$emitGizmos(Frustum frustum, double cameraX, double cameraY,
                                   double cameraZ, float partialTick, CallbackInfo ci) {
        new Cuboid(TungstenMod.TARGET.subtract(0.5, 0, 0.5), new Vec3(1, 2, 1), Color.GREEN).render();
        renderCollection(TungstenModRenderContainer.RUNNING_PATH_RENDERER, frustum);
        renderCollection(TungstenModRenderContainer.BLOCK_PATH_RENDERER, frustum);
        renderCollection(TungstenModRenderContainer.RENDERERS, frustum);
        renderCollection(TungstenModRenderContainer.TEST, frustum);
        renderCollection(TungstenModRenderContainer.ERROR, frustum);
    }

    private static void renderCollection(Collection<Renderer> renderers, Frustum frustum) {
        List<Renderer> snapshot;
        synchronized (renderers) {
            snapshot = new ArrayList<>(renderers);
        }
        Collections.reverse(snapshot);
        int count = 0;
        for (Renderer renderer : snapshot) {
            if (count >= 500) break;
            if (renderer == null || renderer.getPos() == null) continue;
            if (frustum != null && !frustum.isVisible(new AABB(renderer.getPos()).inflate(3))) continue;
            renderer.render();
            count++;
        }
    }
}
