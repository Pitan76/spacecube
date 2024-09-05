package net.pitan76.spacecube;

import net.minecraft.item.Item;
import net.pitan76.mcpitanlib.api.item.CompatibleItemSettings;
import net.pitan76.mcpitanlib.api.util.CompatIdentifier;
import net.pitan76.mcpitanlib.api.util.ItemUtil;
import net.pitan76.spacecube.item.*;

import java.util.function.Supplier;

import static net.pitan76.spacecube.SpaceCube.registry;

public class Items {
    public static Item TINY_SPCAE_CUBE = ItemUtil.ofBlock(Blocks.TINY_SPCAE_CUBE, CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));
    public static Item SMALL_SPCAE_CUBE = ItemUtil.ofBlock(Blocks.SMALL_SPCAE_CUBE, CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));
    public static Item NORMAL_SPCAE_CUBE = ItemUtil.ofBlock(Blocks.NORMAL_SPCAE_CUBE, CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));
    public static Item LARGE_SPCAE_CUBE = ItemUtil.ofBlock(Blocks.LARGE_SPCAE_CUBE, CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));
    public static Item GIANT_SPCAE_CUBE = ItemUtil.ofBlock(Blocks.GIANT_SPCAE_CUBE, CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));
    public static Item MAXIMUM_SPCAE_CUBE = ItemUtil.ofBlock(Blocks.MAXIMUM_SPCAE_CUBE, CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));

    public static Item PERSONAL_SHRINKING_DEVICE = new PersonalShrinkingDevice(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));

    public static Item SOLID_WALL = ItemUtil.ofBlock(Blocks.SOLID_WALL, CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));
    public static Item WALL = ItemUtil.ofBlock(Blocks.WALL, CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));

    public static Item ENERGY_TUNNEL = new EnergyTunnelItem(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));
    public static Item FLUID_TUNNEL = new FluidTunnelItem(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));
    public static Item ITEM_TUNNEL = new ItemTunnelItem(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB));

    public static Item SMALL_UPGRADER = new SpaceCubeUpgrader(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB), 3);
    public static Item NORMAL_UPGRADER = new SpaceCubeUpgrader(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB), 4);
    public static Item LARGE_UPGRADER = new SpaceCubeUpgrader(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB), 5);
    public static Item GIANT_UPGRADER = new SpaceCubeUpgrader(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB), 6);
    public static Item MAXIMUM_UPGRADER = new SpaceCubeUpgrader(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB), 7);

    public static void init() {
        register(SpaceCube._id("tiny_space_cube"), () -> TINY_SPCAE_CUBE);
        register(SpaceCube._id("small_space_cube"), () -> SMALL_SPCAE_CUBE);
        register(SpaceCube._id("normal_space_cube"), () -> NORMAL_SPCAE_CUBE);
        register(SpaceCube._id("large_space_cube"), () -> LARGE_SPCAE_CUBE);
        register(SpaceCube._id("giant_space_cube"), () -> GIANT_SPCAE_CUBE);
        register(SpaceCube._id("maximum_space_cube"), () -> MAXIMUM_SPCAE_CUBE);

        register(SpaceCube._id("personal_shrinking_device"), () -> PERSONAL_SHRINKING_DEVICE);

        register(SpaceCube._id("solid_wall"), () -> SOLID_WALL);
        register(SpaceCube._id("wall"), () -> WALL);

        register(SpaceCube._id("energy_tunnel"), () -> ENERGY_TUNNEL);
        register(SpaceCube._id("fluid_tunnel"), () -> FLUID_TUNNEL);
        register(SpaceCube._id("item_tunnel"), () -> ITEM_TUNNEL);

        register(SpaceCube._id("small_upgrader"), () -> SMALL_UPGRADER);
        register(SpaceCube._id("normal_upgrader"), () -> NORMAL_UPGRADER);
        register(SpaceCube._id("large_upgrader"), () -> LARGE_UPGRADER);
        register(SpaceCube._id("giant_upgrader"), () -> GIANT_UPGRADER);
        register(SpaceCube._id("maximum_upgrader"), () -> MAXIMUM_UPGRADER);
    }

    public static void register(CompatIdentifier identifier, Supplier<Item> supplier) {
        registry.registerItem(identifier, supplier);
    }
}
