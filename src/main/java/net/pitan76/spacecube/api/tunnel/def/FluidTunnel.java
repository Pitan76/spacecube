package net.pitan76.spacecube.api.tunnel.def;

import net.minecraft.nbt.NbtCompound;
import net.pitan76.mcpitanlib.api.registry.CompatRegistryLookup;
import net.pitan76.mcpitanlib.api.transfer.fluid.v1.FluidStorageUtil;
import net.pitan76.mcpitanlib.api.transfer.fluid.v1.IFluidStorage;
import net.pitan76.mcpitanlib.api.util.BlockEntityUtil;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;

public class FluidTunnel implements ITunnelDef {
    private TunnelWallBlockEntity blockEntity = null;

    public FluidTunnel(TunnelWallBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public TunnelType getTunnelType() {
        return TunnelType.FLUID;
    }

    @Override
    public TunnelWallBlockEntity getBlockEntity() {
        return blockEntity;
    }

    private final IFluidStorage fluidStorage = FluidStorageUtil.withFixedCapacity(
            81000 * 8,
            () -> BlockEntityUtil.markDirty(blockEntity));

    public boolean isEmpty() {
        return fluidStorage.getAmount() == 0;
    }

    @Override
    public void writeNbt(NbtCompound nbt, CompatRegistryLookup registryLookup) {
        fluidStorage.writeNbt(nbt, registryLookup);
    }

    @Override
    public void readNbt(NbtCompound nbt, CompatRegistryLookup registryLookup) {
        fluidStorage.readNbt(nbt, registryLookup);
    }

    public IFluidStorage getFluidStorage() {
        return fluidStorage;
    }
}
