package net.pitan76.spacecube;

import net.pitan76.mcpitanlib.api.registry.result.RegistryResult;
import net.pitan76.mcpitanlib.api.tile.BlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;

import java.util.function.Supplier;

public class BlockEntities {
    public static RegistryResult<BlockEntityType<?>> SPACE_CUBE_BLOCK_ENTITY;
    public static RegistryResult<BlockEntityType<?>> TUNNEL_WALL_BLOCK_ENTITY;

    public static void init() {
        SPACE_CUBE_BLOCK_ENTITY = register(SpaceCube.id("space_cube_block"), () -> create(SpaceCubeBlockEntity::new, Blocks.TINY_SPCAE_CUBE, Blocks.SMALL_SPCAE_CUBE, Blocks.NORMAL_SPCAE_CUBE, Blocks.LARGE_SPCAE_CUBE, Blocks.GIANT_SPCAE_CUBE, Blocks.MAXIMUM_SPCAE_CUBE));
        TUNNEL_WALL_BLOCK_ENTITY = register(SpaceCube.id("tunnel_wall"), () -> create(TunnelWallBlockEntity::new, Blocks.TUNNEL_WALL));
    }

    public static <T extends BlockEntity> BlockEntityType<T> create(BlockEntityTypeBuilder.Factory<T> supplier, Block... blocks) {
        return BlockEntityTypeBuilder.create(supplier, blocks).build();
    }

    public static RegistryResult<BlockEntityType<?>> register(Identifier identifier, Supplier<BlockEntityType<?>> supplier) {
        return SpaceCube.registry.registerBlockEntityType(identifier, supplier);
    }
}
