package net.pitan76.spacecube.api.tunnel.def;

import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;

public class RedstoneTunnel implements ITunnelDef {
    private TunnelWallBlockEntity blockEntity = null;

    public RedstoneTunnel(TunnelWallBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public TunnelType getTunnelType() {
        return TunnelType.REDSTONE;
    }

    @Override
    public TunnelWallBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
