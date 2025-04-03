package net.pitan76.spacecube;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.pitan76.mcpitanlib.api.registry.result.RegistryResult;
import net.pitan76.mcpitanlib.api.tile.BlockEntityTypeBuilder;
import net.pitan76.mcpitanlib.api.util.CompatIdentifier;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;

import java.util.function.Supplier;

import static net.pitan76.spacecube.SpaceCube.registry;

public class BlockEntities {
    public static RegistryResult<BlockEntityType<?>> SPACE_CUBE_BLOCK_ENTITY;
    public static RegistryResult<BlockEntityType<?>> TUNNEL_WALL_BLOCK_ENTITY;

    public static void init() {
        SPACE_CUBE_BLOCK_ENTITY = register(SpaceCube._id("space_cube_block"), () -> create(SpaceCubeBlockEntity::new, Blocks.TINY_SPACE_CUBE, Blocks.SMALL_SPACE_CUBE, Blocks.NORMAL_SPACE_CUBE, Blocks.LARGE_SPACE_CUBE, Blocks.GIANT_SPACE_CUBE, Blocks.MAXIMUM_SPACE_CUBE));
        TUNNEL_WALL_BLOCK_ENTITY = register(SpaceCube._id("tunnel_wall"), () -> create(TunnelWallBlockEntity::new, Blocks.TUNNEL_WALL));
    }

    public static <T extends BlockEntity> BlockEntityType<T> create(BlockEntityTypeBuilder.Factory<T> supplier, Block... blocks) {
        return BlockEntityTypeBuilder.create(supplier, blocks).build();
    }

    public static RegistryResult<BlockEntityType<?>> register(CompatIdentifier identifier, Supplier<BlockEntityType<?>> supplier) {
        return registry.registerBlockEntityType(identifier, supplier);
    }
}
