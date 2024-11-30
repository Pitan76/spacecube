package net.pitan76.spacecube.item;

import net.pitan76.mcpitanlib.api.item.v2.CompatibleItemSettings;
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
