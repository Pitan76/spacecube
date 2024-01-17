package net.pitan76.spacecube.item;

import ml.pkom.mcpitanlibarch.api.item.CompatibleItemSettings;
import net.pitan76.spacecube.api.tunnel.TunnelType;

public class EnergyTunnelItem extends TunnelItem {
    public EnergyTunnelItem(CompatibleItemSettings settings) {
        super(settings);
    }

    @Override
    public TunnelType getTunnelType() {
        return TunnelType.ENERGY;
    }
}
