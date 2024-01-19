package net.pitan76.spacecube.api.tunnel.def;

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
}
