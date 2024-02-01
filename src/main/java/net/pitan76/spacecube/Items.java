package net.pitan76.spacecube;

import ml.pkom.mcpitanlibarch.api.item.CompatibleItemSettings;
import ml.pkom.mcpitanlibarch.api.util.ItemUtil;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.pitan76.spacecube.item.*;

import java.util.function.Supplier;

public class Items {
    public static Item TINY_SPCAE_CUBE = ItemUtil.ofBlock(Blocks.TINY_SPCAE_CUBE, CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB, SpaceCube.id("tiny_space_cube")));
    public static Item SMALL_SPCAE_CUBE = ItemUtil.ofBlock(Blocks.SMALL_SPCAE_CUBE, CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB, SpaceCube.id("small_space_cube")));
    public static Item NORMAL_SPCAE_CUBE = ItemUtil.ofBlock(Blocks.NORMAL_SPCAE_CUBE, CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB, SpaceCube.id("normal_space_cube")));
    public static Item LARGE_SPCAE_CUBE = ItemUtil.ofBlock(Blocks.LARGE_SPCAE_CUBE, CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB, SpaceCube.id("large_space_cube")));
    public static Item GIANT_SPCAE_CUBE = ItemUtil.ofBlock(Blocks.GIANT_SPCAE_CUBE, CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB, SpaceCube.id("giant_space_cube")));
    public static Item MAXIMUM_SPCAE_CUBE = ItemUtil.ofBlock(Blocks.MAXIMUM_SPCAE_CUBE, CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB, SpaceCube.id("maximum_space_cube")));

    public static Item PERSONAL_SHRINKING_DEVICE = new PersonalShrinkingDevice(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB, SpaceCube.id("personal_shrinking_device")));

    public static Item SOLID_WALL = ItemUtil.ofBlock(Blocks.SOLID_WALL, CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB, SpaceCube.id("solid_wall")));
    public static Item WALL = ItemUtil.ofBlock(Blocks.WALL, CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB, SpaceCube.id("wall")));

    public static Item ENERGY_TUNNEL = new EnergyTunnelItem(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB, SpaceCube.id("energy_tunnel")));
    public static Item FLUID_TUNNEL = new FluidTunnelItem(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB, SpaceCube.id("fluid_tunnel")));
    public static Item ITEM_TUNNEL = new ItemTunnelItem(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB, SpaceCube.id("item_tunnel")));

    public static Item SMALL_UPGRADER = new SpaceCubeUpgrader(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB, SpaceCube.id("small_upgrader")), 3);
    public static Item NORMAL_UPGRADER = new SpaceCubeUpgrader(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB, SpaceCube.id("normal_upgrader")), 4);
    public static Item LARGE_UPGRADER = new SpaceCubeUpgrader(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB, SpaceCube.id("large_upgrader")), 5);
    public static Item GIANT_UPGRADER = new SpaceCubeUpgrader(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB, SpaceCube.id("giant_upgrader")), 6);
    public static Item MAXIMUM_UPGRADER = new SpaceCubeUpgrader(CompatibleItemSettings.of().addGroup(SpaceCube.SPACE_CUBE_CREATIVE_TAB, SpaceCube.id("maximum_upgrader")), 7);

    public static void init() {
        register(SpaceCube.id("tiny_space_cube"), () -> TINY_SPCAE_CUBE);
        register(SpaceCube.id("small_space_cube"), () -> SMALL_SPCAE_CUBE);
        register(SpaceCube.id("normal_space_cube"), () -> NORMAL_SPCAE_CUBE);
        register(SpaceCube.id("large_space_cube"), () -> LARGE_SPCAE_CUBE);
        register(SpaceCube.id("giant_space_cube"), () -> GIANT_SPCAE_CUBE);
        register(SpaceCube.id("maximum_space_cube"), () -> MAXIMUM_SPCAE_CUBE);

        register(SpaceCube.id("personal_shrinking_device"), () -> PERSONAL_SHRINKING_DEVICE);

        register(SpaceCube.id("solid_wall"), () -> SOLID_WALL);
        register(SpaceCube.id("wall"), () -> WALL);

        register(SpaceCube.id("energy_tunnel"), () -> ENERGY_TUNNEL);
        register(SpaceCube.id("fluid_tunnel"), () -> FLUID_TUNNEL);
        register(SpaceCube.id("item_tunnel"), () -> ITEM_TUNNEL);

        register(SpaceCube.id("small_upgrader"), () -> SMALL_UPGRADER);
        register(SpaceCube.id("normal_upgrader"), () -> NORMAL_UPGRADER);
        register(SpaceCube.id("large_upgrader"), () -> LARGE_UPGRADER);
        register(SpaceCube.id("giant_upgrader"), () -> GIANT_UPGRADER);
        register(SpaceCube.id("maximum_upgrader"), () -> MAXIMUM_UPGRADER);
    }

    public static void register(Identifier identifier, Supplier<Item> supplier) {
        SpaceCube.registry.registerItem(identifier, supplier);
    }
}
