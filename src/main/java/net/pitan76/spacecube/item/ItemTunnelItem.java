package net.pitan76.spacecube.item;

import net.pitan76.mcpitanlib.api.item.CompatibleItemSettings;
import net.pitan76.mcpitanlib.api.item.ExtendItem;
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
