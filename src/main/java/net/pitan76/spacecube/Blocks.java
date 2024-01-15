package net.pitan76.spacecube;

import ml.pkom.mcpitanlibarch.api.block.CompatibleBlockSettings;
import ml.pkom.mcpitanlibarch.api.block.ExtendBlock;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.pitan76.spacecube.block.SpaceCubeBlock;
import net.pitan76.spacecube.block.SpaceCubeWallBlock;

import java.util.function.Supplier;

public class Blocks {
    public static ExtendBlock TINY_SPCAE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings(), 2);
    public static ExtendBlock SMALL_SPCAE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings(), 3);
    public static ExtendBlock NORMAL_SPCAE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings(), 4);
    public static ExtendBlock LARGE_SPCAE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings(), 5);
    public static ExtendBlock GIANT_SPCAE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings(), 6);
    public static ExtendBlock MAXIMUM_SPCAE_CUBE = new SpaceCubeBlock(new CompatibleBlockSettings(), 7);

    public static ExtendBlock SOLID_WALL = new SpaceCubeWallBlock(new CompatibleBlockSettings().luminance((state) -> 15));
    public static ExtendBlock WALL = new SpaceCubeWallBlock(new CompatibleBlockSettings());

    public static void init() {
        register(SpaceCube.id("tiny_space_cube"), () -> TINY_SPCAE_CUBE);
        register(SpaceCube.id("small_space_cube"), () -> SMALL_SPCAE_CUBE);
        register(SpaceCube.id("normal_space_cube"), () -> NORMAL_SPCAE_CUBE);
        register(SpaceCube.id("large_space_cube"), () -> LARGE_SPCAE_CUBE);
        register(SpaceCube.id("giant_space_cube"), () -> GIANT_SPCAE_CUBE);
        register(SpaceCube.id("maximum_space_cube"), () -> MAXIMUM_SPCAE_CUBE);

        register(SpaceCube.id("solid_wall"), () -> SOLID_WALL);
        register(SpaceCube.id("wall"), () -> WALL);
    }

    public static void register(Identifier identifier, Supplier<Block> supplier) {
        SpaceCube.registry.registerBlock(identifier, supplier);
    }
}
