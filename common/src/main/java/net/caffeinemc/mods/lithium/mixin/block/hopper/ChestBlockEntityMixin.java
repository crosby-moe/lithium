package net.caffeinemc.mods.lithium.mixin.block.hopper;

import net.caffeinemc.mods.lithium.api.inventory.LithiumCacheableInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin extends BlockEntity implements LithiumCacheableInventory {
    public ChestBlockEntityMixin(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    //Prevent single chest inventories being cached when their block state indicates they part of a compound container
    // Addresses the issue below, however this does not fix it for other (modded) compound inventories, e.g. double barrel from TIS carpet addition

    // Hopper under a double chest only pulls from the half above it if that half was placed with chest type already set
    // https://github.com/CaffeineMC/lithium/issues/789

    @Override
    public boolean hopperCanReuseInventoryUntilBlockEntityInvalidatedOrBlockStateChangedLithium() {
        return this.getBlockState().getValueOrElse(ChestBlock.TYPE, ChestType.LEFT) == ChestType.SINGLE;
    }
}
