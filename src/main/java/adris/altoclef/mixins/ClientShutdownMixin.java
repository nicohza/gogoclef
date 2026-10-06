package adris.altoclef.mixins;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
//#if MC >= 260000
//$$ import baritone.Baritone;
//$$ import baritone.api.BaritoneAPI;
//$$ import baritone.cache.WorldData;
//$$ import org.slf4j.LoggerFactory;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import java.util.ArrayList;
//$$ import java.util.concurrent.ExecutorService;
//$$ import java.util.concurrent.TimeUnit;
//#endif

@Mixin(MinecraftClient.class)
public final class ClientShutdownMixin {
    //#if MC >= 260000
    // Ostinato's cache packer and periodic saver run indefinitely on non-daemon
    // workers. Minecraft 26.3 no longer exits while those workers remain alive.
    // Stop them before Minecraft shuts down its own executors, then flush every
    // visited world's cache (the current world can already be null at the title).
    //$$ @Inject(method = "close()V", at = @At("HEAD"))
    //$$ private void altoClefStopOstinato(CallbackInfo ci) {
    //$$     var logger = LoggerFactory.getLogger("TenorClef");
    //$$     var cache = OstinatoWorldCacheAccessor.altoClefWorldCache();
    //$$     ArrayList<WorldData> worlds;
    //$$     synchronized (cache) {
    //$$         worlds = new ArrayList<>(cache.values());
    //$$     }
    //$$     var executor = (ExecutorService) Baritone.getExecutor();
    //$$     for (var baritone : BaritoneAPI.getProvider().getAllBaritones()) {
    //$$         baritone.getPathingBehavior().forceCancel();
    //$$     }
    //$$     executor.shutdownNow();
    //$$     try {
    //$$         if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
    //$$             logger.warn("Ostinato workers did not stop within five seconds");
    //$$         }
    //$$     } catch (InterruptedException e) {
    //$$         Thread.currentThread().interrupt();
    //$$     }
    // Pruning asks world providers to detect world changes, which can submit new
    // asynchronous saves. Only flush data after shutdown; RAM reclamation is moot.
    //$$     boolean pruneRegions = Baritone.settings().pruneRegionsFromRAM.value;
    //$$     Baritone.settings().pruneRegionsFromRAM.value = false;
    //$$     try {
    //$$         for (WorldData world : worlds) {
    //$$             try {
    //$$                 world.getCachedWorld().save();
    //$$             } catch (RuntimeException e) {
    //$$                 logger.error("Failed to save an Ostinato world cache during shutdown", e);
    //$$             }
    //$$         }
    //$$     } finally {
    //$$         Baritone.settings().pruneRegionsFromRAM.value = pruneRegions;
    //$$     }
    //$$ }
    //#endif
}
