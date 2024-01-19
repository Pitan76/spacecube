package net.pitan76.spacecube.api.tunnel.def;

import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;

public class EnergyTunnel implements ITunnelDef {
    private TunnelWallBlockEntity blockEntity = null;

    public EnergyTunnel(TunnelWallBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public TunnelType getTunnelType() {
        return TunnelType.ENERGY;
    }

    @Override
    public TunnelWallBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
