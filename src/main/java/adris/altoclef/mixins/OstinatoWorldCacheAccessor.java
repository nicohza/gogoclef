package adris.altoclef.mixins;

import org.spongepowered.asm.mixin.Mixin;
//#if MC >= 260000
//$$ import baritone.cache.WorldData;
//$$ import baritone.cache.WorldProvider;
//$$ import org.spongepowered.asm.mixin.gen.Accessor;
//$$ import java.nio.file.Path;
//$$ import java.util.Map;
//$$
//$$ @Mixin(value = WorldProvider.class, remap = false)
//#else
import net.minecraft.client.MinecraftClient;
@Mixin(MinecraftClient.class)
//#endif
public interface OstinatoWorldCacheAccessor {
    //#if MC >= 260000
    //$$ @Accessor("worldCache")
    //$$ static Map<Path, WorldData> altoClefWorldCache() {
    //$$     throw new AssertionError("Mixin accessor was not transformed");
    //$$ }
    //#endif
}
