package kaptainwutax.tungsten.world;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.dimension.DimensionType;

public class VoxelWorld implements LevelReader {

    public final LevelReader parent;
    private Long2ObjectMap<VoxelChunk> chunks = new Long2ObjectOpenHashMap<>();

    public VoxelWorld(LevelReader parent) {
        this.parent = parent;
        this.chunks.defaultReturnValue(VoxelChunk.EMPTY);
    }

    @Override
    public boolean hasChunk(int chunkX, int chunkZ) {
        return true;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY() - this.getMinY();
        int z = pos.getZ();
        long id = 0L;
        return this.chunks.get(id).getBlockState(x, y, z);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY() - this.getMinY();
        int z = pos.getZ();
        long id = 0L;
        return this.chunks.get(id).getFluidState(x, y, z);
    }

    public void setBlockAndFluidState(BlockPos pos, BlockState block, FluidState fluid) {
        int x = pos.getX();
        int y = pos.getY() - this.getMinY();
        int z = pos.getZ();
        long id = 0L;
        VoxelChunk chunk = this.chunks.computeIfAbsent(id, i -> new VoxelChunk());
        chunk.setBlockState(x, y, z, block);
        chunk.setFluidState(x, y, z, fluid);
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB box) {
        return this.parent.getEntityCollisions(entity, box);
    }

    //========================================================================================================//

    @Nullable
    @Override
    public ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
        return this.parent.getChunk(chunkX, chunkZ, leastStatus, create);
    }

    @Override
    public int getHeight(Heightmap.Types heightmap, int x, int z) {
        return this.parent.getHeight(heightmap, x, z);
    }

    @Override
    public int getSkyDarken() {
        return this.parent.getSkyDarken();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return this.parent.getBiomeManager();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int biomeX, int biomeY, int biomeZ) {
        return this.parent.getUncachedNoiseBiome(biomeX, biomeY, biomeZ);
    }

    @Override
    public boolean isClientSide() {
        return true;
    }

    @Override
    public int getSeaLevel() {
        return this.parent.getSeaLevel();
    }

    @Override
    public DimensionType dimensionType() {
        return this.parent.dimensionType();
    }

    @Override
    public net.minecraft.world.attribute.EnvironmentAttributeReader environmentAttributes() {
        return this.parent.environmentAttributes();
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.parent.getLightEngine();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.parent.getWorldBorder();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return this.parent.getBlockEntity(pos);
    }

	@Override
	public RegistryAccess registryAccess() {
		return this.parent.registryAccess();
	}

	@Override
	public FeatureFlagSet enabledFeatures() {
		return this.parent.enabledFeatures();
	}

}
