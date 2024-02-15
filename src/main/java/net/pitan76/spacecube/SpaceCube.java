package net.pitan76.spacecube;

import net.pitan76.mcpitanlib.api.command.CommandRegistry;
import net.pitan76.mcpitanlib.api.item.CreativeTabBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import net.pitan76.mcpitanlib.api.registry.CompatRegistry;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.api.tunnel.def.FluidTunnel;
import net.pitan76.spacecube.api.tunnel.def.ITunnelDef;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;
import net.pitan76.spacecube.cmd.SpaceCubeCommand;
import net.pitan76.spacecube.compat.RebornEnergyRegister;

public class SpaceCube implements ModInitializer {

    public static final String MOD_ID = "spacecube";

    // MCPitanLibの独自のレジストリ MCPitanLib's own registry
    public static final CompatRegistry registry = CompatRegistry.createRegistry(MOD_ID);

    public static final ItemGroup SPACE_CUBE_CREATIVE_TAB = CreativeTabBuilder.create(id("creative_tab")).setIcon(() -> new ItemStack(Items.NORMAL_SPCAE_CUBE, 1)).build();
    public static final Identifier SPACE_CUBE_DIMENSION_WORLD_KEY = id("space_cube_dimension");

    // TODO: Space Cube Dimensionで雨が降らないようにする (Make it so that it doesn't rain in the Space Cube Dimension)
    // TODO: アップグレーダーの実装 (Implementation of upgrader)

    @Override
    public void onInitialize() {
        Config.init(FabricLoader.getInstance().getConfigDir().toFile());

        // Register the creative tab
        registry.registerItemGroup(id("creative_tab"), () -> SPACE_CUBE_CREATIVE_TAB);

        // Register the block, item and block entity
        Blocks.init();
        Items.init();
        BlockEntities.init();

        registerFluidStorage();
        registerEnergyStorage();

        CommandRegistry.register("spacecube", new SpaceCubeCommand());

        // 1.16.5対応のため (1.16.5の動作は想定していないけどまあ、動けば嬉しいな（笑）)
        // For 1.16.5 support (I don't expect 1.16.5 to work, but I'm happy if it does (lol))
        registry.allRegister();
    }

    public static void registerEnergyStorage() {
        if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")) {
            RebornEnergyRegister.init();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void registerFluidStorage() {
        FluidStorage.SIDED.registerForBlockEntity((blockEntity, dir) -> {
            if (blockEntity instanceof TunnelWallBlockEntity) {
                ITunnelDef def = ((TunnelWallBlockEntity) blockEntity).getTunnelDef();
                if (def instanceof FluidTunnel)
                    return ((FluidTunnel) def).getFluidStorage();
            }

            return null;
        }, BlockEntities.TUNNEL_WALL_BLOCK_ENTITY.getOrNull());

        FluidStorage.SIDED.registerForBlockEntity((blockEntity, dir) -> {
            if (blockEntity instanceof SpaceCubeBlockEntity) {
                SpaceCubeBlockEntity scBlockEntity = (SpaceCubeBlockEntity) blockEntity;
                ITunnelDef def = scBlockEntity.getTunnelDef(TunnelType.FLUID, dir);
                if (def instanceof FluidTunnel)
                    return ((FluidTunnel) def).getFluidStorage();
            }

            return null;
        }, BlockEntities.SPACE_CUBE_BLOCK_ENTITY.getOrNull());

    }

    public static Identifier id(String id) {
        return new Identifier(MOD_ID, id);
    }

}
