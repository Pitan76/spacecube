package net.pitan76.spacecube.api.tunnel;

import net.pitan76.mcpitanlib.api.util.CompatIdentifier;
import net.pitan76.spacecube.SpaceCube;
import net.pitan76.spacecube.api.tunnel.def.*;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;

public class TunnelType {

    public static TunnelType NONE = new TunnelType(SpaceCube._id("none"), 0x6D6D6D, 0x6D6D6D, 0x19C419, ItemTunnel::new);
    public static TunnelType ENERGY = new TunnelType(SpaceCube._id("energy_tunnel"), 0x6D6D6D, 0x19C419, 0x19C419, EnergyTunnel::new);
    public static TunnelType FLUID = new TunnelType(SpaceCube._id("fluid_tunnel"), 0x6D6D6D, 0x19C4C4, 0x19C419, FluidTunnel::new);
    public static TunnelType ITEM = new TunnelType(SpaceCube._id("item_tunnel"), 0x6D6D6D, 0xE1C400, 0x19C419, ItemTunnel::new);
    public static TunnelType REDSTONE = new TunnelType(SpaceCube._id("redstone_tunnel"), 0xE11900, 0xE11900, 0x19C419, RedstoneTunnel::new);

    private final CompatIdentifier id;
    public int INDICATOR_COLOR = 0x6D6D6D;
    public int IMPORT_COLOR = 0x6D6D6D;
    public int EXPORT_COLOR = 0x19C419;

    private Factory<ITunnelDef> tunnelDefFactory = null;

    public TunnelType(CompatIdentifier id, int INDICATOR_COLOR, int IMPORT_COLOR, int EXPORT_COLOR, Factory<ITunnelDef> tunnelDefFactory) {
        this.id = id;
        this.INDICATOR_COLOR = INDICATOR_COLOR;
        this.IMPORT_COLOR = IMPORT_COLOR;
        this.EXPORT_COLOR = EXPORT_COLOR;
        this.tunnelDefFactory = tunnelDefFactory;
    }

    public TunnelType(CompatIdentifier id, int INDICATOR_COLOR, int IMPORT_COLOR, int EXPORT_COLOR) {
        this.id = id;
        this.INDICATOR_COLOR = INDICATOR_COLOR;
        this.IMPORT_COLOR = IMPORT_COLOR;
        this.EXPORT_COLOR = EXPORT_COLOR;
    }

    public TunnelType(CompatIdentifier id) {
        this.id = id;
    }

    public ITunnelDef createTunnelDef(TunnelWallBlockEntity blockEntity) {
        return tunnelDefFactory.create(blockEntity);
    }

    public CompatIdentifier getId() {
        return id;
    }

    public static TunnelType fromString(String id) {
        return fromId(CompatIdentifier.of(id));
    }

    public static TunnelType fromId(CompatIdentifier id) {
        switch (id.toString()) {
            case "spacecube:none":
                return NONE;
            case "spacecube:energy_tunnel":
                return ENERGY;
            case "spacecube:fluid_tunnel":
                return FLUID;
            case "spacecube:item_tunnel":
                return ITEM;
            case "spacecube:redstone_tunnel":
                return REDSTONE;
            default:
                return NONE;
        }
    }

    @FunctionalInterface
    public interface Factory<T extends ITunnelDef> {
        T create(TunnelWallBlockEntity blockEntity);
    }
}
