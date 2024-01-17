package net.pitan76.spacecube.item;

import ml.pkom.mcpitanlibarch.api.item.CompatibleItemSettings;
import ml.pkom.mcpitanlibarch.api.item.ExtendItem;
import net.pitan76.spacecube.api.tunnel.TunnelType;

public class ItemTunnelItem extends TunnelItem {
    public ItemTunnelItem(CompatibleItemSettings settings) {
        super(settings);
    }

    @Override
    public TunnelType getTunnelType() {
        return TunnelType.ITEM;
    }
}
