package net.caffeinemc.mods.lithium.mixin.world.combined_heightmap_update;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.lithium.common.world.chunk.heightmap.CombinedHeightmapUpdate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess {

    public LevelChunkMixin(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, PalettedContainerFactory containerFactory, long inhabitedTime, LevelChunkSection @Nullable [] sections, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, containerFactory, inhabitedTime, sections, blendingData);
    }

    @Redirect(
            method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private <K, V> V skipGetHeightmap(Map<K, V> heightmaps, K heightmapType) {
        if (heightmapType == Heightmap.Types.MOTION_BLOCKING || heightmapType == Heightmap.Types.MOTION_BLOCKING_NO_LEAVES || heightmapType == Heightmap.Types.OCEAN_FLOOR || heightmapType == Heightmap.Types.WORLD_SURFACE) {
            return null;
        }
        return heightmaps.get(heightmapType);
    }

    @WrapOperation(
            method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/Heightmap;update(IIILnet/minecraft/world/level/block/state/BlockState;)Z")
    )
    private boolean skipHeightmapUpdate(Heightmap instance, int localX, int localY, int localZ, BlockState state, Operation<Boolean> original) {
        if (instance != null) {
            return original.call(instance, localX, localY, localZ, state);
        } else {
            return false;
        }
    }

    @Inject(
            method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/Heightmap;update(IIILnet/minecraft/world/level/block/state/BlockState;)Z",
                    ordinal = 0
            )
    )
    private void updateHeightmapsCombined(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir, @Local(name = "y") int y, @Local(name = "localX") int localX, @Local(name = "localZ") int localZ) {
        Heightmap heightmap0 = this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING);
        Heightmap heightmap1 = this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES);
        Heightmap heightmap2 = this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR);
        Heightmap heightmap3 = this.heightmaps.get(Heightmap.Types.WORLD_SURFACE);
        CombinedHeightmapUpdate.updateHeightmaps(heightmap0, heightmap1, heightmap2, heightmap3, (LevelChunk) (ChunkAccess) this, localX, y, localZ, state);
    }
}
