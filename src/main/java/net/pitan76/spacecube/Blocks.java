package net.pitan76.spacecube;

import ml.pkom.mcpitanlibarch.api.block.CompatibleBlockSettings;
import ml.pkom.mcpitanlibarch.api.block.ExtendBlock;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.pitan76.spacecube.block.SpaceCubeBlock;
import net.pitan76.spacecube.block.TunnelWallBlock;
import net.pitan76.spacecube.block.WallBlock;

import java.util.function.Supplier;

public class Blocks {
    public static SpaceCubeBlock TINY_SPCAE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings().strength(5.0F, 6.0F), 2);
    public static SpaceCubeBlock SMALL_SPCAE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings().strength(5.0F, 6.0F), 3);
    public static SpaceCubeBlock NORMAL_SPCAE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings().strength(5.0F, 6.0F), 4);
    public static SpaceCubeBlock LARGE_SPCAE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings().strength(5.0F, 6.0F), 5);
    public static SpaceCubeBlock GIANT_SPCAE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings().strength(5.0F, 6.0F), 6);
    public static SpaceCubeBlock MAXIMUM_SPCAE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings().strength(5.0F, 6.0F), 7);

    public static ExtendBlock TUNNEL_WALL = new TunnelWallBlock(new CompatibleBlockSettings().luminance((state) -> 15).strength(-1.0F, 3600000.0F));
    public static ExtendBlock SOLID_WALL = new WallBlock(new CompatibleBlockSettings().luminance((state) -> 15).strength(-1.0F, 3600000.0F));
    public static ExtendBlock WALL = new WallBlock(new CompatibleBlockSettings().strength(3.0F, 6.0F));

    public static void init() {
        register(SpaceCube.id("tiny_space_cube"), () -> TINY_SPCAE_CUBE);
        register(SpaceCube.id("small_space_cube"), () -> SMALL_SPCAE_CUBE);
        register(SpaceCube.id("normal_space_cube"), () -> NORMAL_SPCAE_CUBE);
        register(SpaceCube.id("large_space_cube"), () -> LARGE_SPCAE_CUBE);
        register(SpaceCube.id("giant_space_cube"), () -> GIANT_SPCAE_CUBE);
        register(SpaceCube.id("maximum_space_cube"), () -> MAXIMUM_SPCAE_CUBE);

        register(SpaceCube.id("tunnel_wall"), () -> TUNNEL_WALL);
        register(SpaceCube.id("solid_wall"), () -> SOLID_WALL);
        register(SpaceCube.id("wall"), () -> WALL);
    }

    public static void register(Identifier identifier, Supplier<Block> supplier) {
        SpaceCube.registry.registerBlock(identifier, supplier);
    }
}
