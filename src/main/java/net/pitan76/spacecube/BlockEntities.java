package net.pitan76.spacecube;

import ml.pkom.mcpitanlibarch.api.event.registry.RegistryEvent;
import ml.pkom.mcpitanlibarch.api.tile.BlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;

import java.util.function.Supplier;

public class BlockEntities {
    public static RegistryEvent<BlockEntityType<?>> SPACE_CUBE_BLOCK_ENTITY;
    public static RegistryEvent<BlockEntityType<?>> TUNNEL_WALL_BLOCK_ENTITY;

    public static void init() {
        SPACE_CUBE_BLOCK_ENTITY = register(SpaceCube.id("space_cube_block"), () -> create(SpaceCubeBlockEntity::new, Blocks.TINY_SPCAE_CUBE, Blocks.SMALL_SPCAE_CUBE, Blocks.NORMAL_SPCAE_CUBE, Blocks.LARGE_SPCAE_CUBE, Blocks.GIANT_SPCAE_CUBE, Blocks.MAXIMUM_SPCAE_CUBE));
        TUNNEL_WALL_BLOCK_ENTITY = register(SpaceCube.id("tunnel_wall"), () -> create(TunnelWallBlockEntity::new, Blocks.TUNNEL_WALL));
    }

    public static <T extends BlockEntity> BlockEntityType<T> create(BlockEntityTypeBuilder.Factory<T> supplier, Block... blocks) {
        return BlockEntityTypeBuilder.create(supplier, blocks).build();
    }

    public static RegistryEvent<BlockEntityType<?>> register(Identifier identifier, Supplier<BlockEntityType<?>> supplier) {
        return SpaceCube.registry.registerBlockEntityType(identifier, supplier);
    }
}
