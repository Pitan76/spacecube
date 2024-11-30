package net.pitan76.spacecube;

import net.minecraft.item.Item;
import net.pitan76.mcpitanlib.api.item.v2.CompatibleItemSettings;
import net.pitan76.mcpitanlib.api.util.CompatIdentifier;
import net.pitan76.mcpitanlib.api.util.item.ItemUtil;
import net.pitan76.spacecube.item.*;

import java.util.function.Supplier;

import static net.pitan76.spacecube.SpaceCube._id;
import static net.pitan76.spacecube.SpaceCube.registry;

public class Items {
    public static Item TINY_SPCAE_CUBE = ItemUtil.create(Blocks.TINY_SPCAE_CUBE, CompatibleItemSettings.of(_id("tiny_space_cube")).addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));
    public static Item SMALL_SPCAE_CUBE = ItemUtil.create(Blocks.SMALL_SPCAE_CUBE, CompatibleItemSettings.of(_id("small_space_cube")).addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));
    public static Item NORMAL_SPCAE_CUBE = ItemUtil.create(Blocks.NORMAL_SPCAE_CUBE, CompatibleItemSettings.of(_id("normal_space_cube")).addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));
    public static Item LARGE_SPCAE_CUBE = ItemUtil.create(Blocks.LARGE_SPCAE_CUBE, CompatibleItemSettings.of(_id("large_space_cube")).addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));
    public static Item GIANT_SPCAE_CUBE = ItemUtil.create(Blocks.GIANT_SPCAE_CUBE, CompatibleItemSettings.of(_id("giant_space_cube")).addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));
    public static Item MAXIMUM_SPCAE_CUBE = ItemUtil.create(Blocks.MAXIMUM_SPCAE_CUBE, CompatibleItemSettings.of(_id("maximum_space_cube")).addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));

    public static Item PERSONAL_SHRINKING_DEVICE = new PersonalShrinkingDevice(CompatibleItemSettings.of(_id("personal_shrinking_device")).addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));

    public static Item SOLID_WALL = ItemUtil.create(Blocks.SOLID_WALL, CompatibleItemSettings.of(_id("solid_wall")).addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));
    public static Item WALL = ItemUtil.create(Blocks.WALL, CompatibleItemSettings.of(_id("wall")).addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));

    public static Item ENERGY_TUNNEL = new EnergyTunnelItem(CompatibleItemSettings.of(_id("energy_tunnel")).addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));
    public static Item FLUID_TUNNEL = new FluidTunnelItem(CompatibleItemSettings.of(_id("fluid_tunnel")).addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));
    public static Item ITEM_TUNNEL = new ItemTunnelItem(CompatibleItemSettings.of(_id("item_tunnel")).addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));

    public static Item SMALL_UPGRADER = new SpaceCubeUpgrader(CompatibleItemSettings.of(_id("small_upgrader")).addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB), 3);
    public static Item NORMAL_UPGRADER = new SpaceCubeUpgrader(CompatibleItemSettings.of(_id("normal_upgrader")).addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB), 4);
    public static Item LARGE_UPGRADER = new SpaceCubeUpgrader(CompatibleItemSettings.of(_id("large_upgrader")).addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB), 5);
    public static Item GIANT_UPGRADER = new SpaceCubeUpgrader(CompatibleItemSettings.of(_id("giant_upgrader")).addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB), 6);
    public static Item MAXIMUM_UPGRADER = new SpaceCubeUpgrader(CompatibleItemSettings.of(_id("maximum_upgrader")).addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB), 7);

    public static void init() {
        register(_id("tiny_space_cube"), () -> TINY_SPCAE_CUBE);
        register(_id("small_space_cube"), () -> SMALL_SPCAE_CUBE);
        register(_id("normal_space_cube"), () -> NORMAL_SPCAE_CUBE);
        register(_id("large_space_cube"), () -> LARGE_SPCAE_CUBE);
        register(_id("giant_space_cube"), () -> GIANT_SPCAE_CUBE);
        register(_id("maximum_space_cube"), () -> MAXIMUM_SPCAE_CUBE);

        register(_id("personal_shrinking_device"), () -> PERSONAL_SHRINKING_DEVICE);

        register(_id("solid_wall"), () -> SOLID_WALL);
        register(_id("wall"), () -> WALL);

        register(_id("energy_tunnel"), () -> ENERGY_TUNNEL);
        register(_id("fluid_tunnel"), () -> FLUID_TUNNEL);
        register(_id("item_tunnel"), () -> ITEM_TUNNEL);

        register(_id("small_upgrader"), () -> SMALL_UPGRADER);
        register(_id("normal_upgrader"), () -> NORMAL_UPGRADER);
        register(_id("large_upgrader"), () -> LARGE_UPGRADER);
        register(_id("giant_upgrader"), () -> GIANT_UPGRADER);
        register(_id("maximum_upgrader"), () -> MAXIMUM_UPGRADER);
    }

    public static void register(CompatIdentifier identifier, Supplier<Item> supplier) {
        registry.registerItem(identifier, supplier);
    }
}
