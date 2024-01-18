package net.pitan76.spacecube.api.tunnel;

import net.minecraft.util.Identifier;
import net.pitan76.spacecube.SpaceCube;

public class TunnelType {

    public static TunnelType NONE = new TunnelType(SpaceCube.id("none"), 0x6D6D6D, 0x6D6D6D, 0x19C419);
    public static TunnelType ENERGY = new TunnelType(SpaceCube.id("energy_tunnel"), 0x6D6D6D, 0x19C419, 0x19C419);
    public static TunnelType FLUID = new TunnelType(SpaceCube.id("fluid_tunnel"), 0x6D6D6D, 0x19C4C4, 0x19C419);
    public static TunnelType ITEM = new TunnelType(SpaceCube.id("item_tunnel"), 0x6D6D6D, 0xE1C400, 0x19C419);
    public static TunnelType REDSTONE = new TunnelType(SpaceCube.id("redstone_tunnel"), 0xE11900, 0xE11900, 0x19C419);

    private final Identifier id;
    public int INDICATOR_COLOR = 0x6D6D6D;
    public int IMPORT_COLOR = 0x6D6D6D;
    public int EXPORT_COLOR = 0x19C419;

    public TunnelType(Identifier id, int INDICATOR_COLOR, int IMPORT_COLOR, int EXPORT_COLOR) {
        this.id = id;
        this.INDICATOR_COLOR = INDICATOR_COLOR;
        this.IMPORT_COLOR = IMPORT_COLOR;
        this.EXPORT_COLOR = EXPORT_COLOR;
    }

    public TunnelType(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return id;
    }

    public static TunnelType fromId(Identifier id) {
        return switch (id.toString()) {
            case "spacecube:none" -> NONE;
            case "spacecube:energy_tunnel" -> ENERGY;
            case "spacecube:fluid_tunnel" -> FLUID;
            case "spacecube:item_tunnel" -> ITEM;
            case "spacecube:redstone_tunnel" -> REDSTONE;
            default -> NONE;
        };
    }
}
