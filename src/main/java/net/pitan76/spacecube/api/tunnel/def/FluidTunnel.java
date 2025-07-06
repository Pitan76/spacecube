package net.pitan76.spacecube.api.tunnel.def;

import net.pitan76.mcpitanlib.api.event.nbt.ReadNbtArgs;
import net.pitan76.mcpitanlib.api.event.nbt.WriteNbtArgs;
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
    public void writeNbt(WriteNbtArgs args) {
        fluidStorage.writeNbt(args);
    }

    @Override
    public void readNbt(ReadNbtArgs args) {
        fluidStorage.readNbt(args);
    }

    public IFluidStorage getFluidStorage() {
        return fluidStorage;
    }
}
