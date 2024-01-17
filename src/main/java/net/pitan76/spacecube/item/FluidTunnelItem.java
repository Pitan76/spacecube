package net.pitan76.spacecube.item;

import ml.pkom.mcpitanlibarch.api.item.CompatibleItemSettings;
import net.pitan76.spacecube.api.tunnel.TunnelType;

public class FluidTunnelItem extends TunnelItem {
    public FluidTunnelItem(CompatibleItemSettings settings) {
        super(settings);
    }

    @Override
    public TunnelType getTunnelType() {
        return TunnelType.FLUID;
    }
}
