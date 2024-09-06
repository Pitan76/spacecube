package net.pitan76.spacecube;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.item.ItemStack;
import net.pitan76.mcpitanlib.api.command.CommandRegistry;
import net.pitan76.mcpitanlib.api.item.CreativeTabBuilder;
import net.pitan76.mcpitanlib.api.registry.v2.CompatRegistryV2;
import net.pitan76.mcpitanlib.api.util.CompatIdentifier;
import net.pitan76.mcpitanlib.api.util.PlatformUtil;
import net.pitan76.mcpitanlib.fabric.ExtendModInitializer;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.api.tunnel.def.FluidTunnel;
import net.pitan76.spacecube.api.tunnel.def.ITunnelDef;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;
import net.pitan76.spacecube.cmd.SpaceCubeCommand;
import net.pitan76.spacecube.compat.RebornEnergyRegister;

public class SpaceCube extends ExtendModInitializer {

    public static final String MOD_ID = "spacecube";
    public static final String MOD_NAME = "Space Cube";

    public static SpaceCube INSTANCE;

    // MCPitanLibの独自のレジストリ MCPitanLib's own registry
    public static CompatRegistryV2 registry;

    public static final CreativeTabBuilder SPACE_CUBE_CREATIVE_TAB = CreativeTabBuilder.create(_id("creative_tab")).setIcon(() -> new ItemStack(Items.NORMAL_SPCAE_CUBE, 1));
    public static final CompatIdentifier SPACE_CUBE_DIMENSION_WORLD_KEY = _id("space_cube_dimension");

    // TODO: Space Cube Dimensionで雨が降らないようにする (Make it so that it doesn't rain in the Space Cube Dimension)
    // TODO: アップグレーダーの実装 (Implementation of upgrader)

    @Override
    public void init() {
        INSTANCE = this;
        registry = super.registry;

        Config.init(PlatformUtil.getConfigFolder().toFile());

        // Register the creative tab
        registry.registerItemGroup(SPACE_CUBE_CREATIVE_TAB);

        // Register the block, item and block entity
        Blocks.init();
        Items.init();
        BlockEntities.init();

        registerFluidStorage();
        registerEnergyStorage();

        CommandRegistry.register("spacecube", new SpaceCubeCommand());
    }

    @Override
    public String getId() {
        return MOD_ID;
    }

    @Override
    public String getName() {
        return MOD_NAME;
    }

    public static void registerEnergyStorage() {
        if (PlatformUtil.isModLoaded("team_reborn_energy")) {
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

    public static CompatIdentifier _id(String id) {
        return CompatIdentifier.of(MOD_ID, id);
    }
}
