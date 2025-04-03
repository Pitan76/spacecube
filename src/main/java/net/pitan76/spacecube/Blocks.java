package net.pitan76.spacecube;

import net.minecraft.block.Block;
import net.pitan76.mcpitanlib.api.block.v2.CompatBlock;
import net.pitan76.mcpitanlib.api.block.v2.CompatibleBlockSettings;
import net.pitan76.mcpitanlib.api.util.CompatIdentifier;
import net.pitan76.spacecube.block.SpaceCubeBlock;
import net.pitan76.spacecube.block.TunnelWallBlock;
import net.pitan76.spacecube.block.WallBlock;

import java.util.function.Supplier;

import static net.pitan76.spacecube.SpaceCube._id;
import static net.pitan76.spacecube.SpaceCube.registry;

public class Blocks {
    public static SpaceCubeBlock TINY_SPACE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings(_id("tiny_space_cube")).strength(5.0F, 6.0F), 2);
    public static SpaceCubeBlock SMALL_SPACE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings(_id("small_space_cube")).strength(5.0F, 6.0F), 3);
    public static SpaceCubeBlock NORMAL_SPACE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings(_id("normal_space_cube")).strength(5.0F, 6.0F), 4);
    public static SpaceCubeBlock LARGE_SPACE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings(_id("large_space_cube")).strength(5.0F, 6.0F), 5);
    public static SpaceCubeBlock GIANT_SPACE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings(_id("giant_space_cube")).strength(5.0F, 6.0F), 6);
    public static SpaceCubeBlock MAXIMUM_SPACE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings(_id("maximum_space_cube")).strength(5.0F, 6.0F), 7);

    public static CompatBlock TUNNEL_WALL = new TunnelWallBlock(new CompatibleBlockSettings(_id("tunnel_wall")).luminance((state) -> 15).strength(-1.0F, 3600000.0F));
    public static CompatBlock SOLID_WALL = new WallBlock(new CompatibleBlockSettings(_id("solid_wall")).luminance((state) -> 15).strength(-1.0F, 3600000.0F));
    public static CompatBlock WALL = new WallBlock(new CompatibleBlockSettings(_id("wall")).strength(3.0F, 6.0F));

    public static void init() {
        register(_id("tiny_space_cube"), () -> TINY_SPACE_CUBE);
        register(_id("small_space_cube"), () -> SMALL_SPACE_CUBE);
        register(_id("normal_space_cube"), () -> NORMAL_SPACE_CUBE);
        register(_id("large_space_cube"), () -> LARGE_SPACE_CUBE);
        register(_id("giant_space_cube"), () -> GIANT_SPACE_CUBE);
        register(_id("maximum_space_cube"), () -> MAXIMUM_SPACE_CUBE);

        register(_id("tunnel_wall"), () -> TUNNEL_WALL);
        register(_id("solid_wall"), () -> SOLID_WALL);
        register(_id("wall"), () -> WALL);
    }

    public static void register(CompatIdentifier identifier, Supplier<Block> supplier) {
        registry.registerBlock(identifier, supplier);
    }
}
