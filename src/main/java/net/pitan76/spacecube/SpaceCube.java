package net.pitan76.spacecube;

import ml.pkom.mcpitanlibarch.api.item.CreativeTabBuilder;
import ml.pkom.mcpitanlibarch.api.registry.ArchRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class SpaceCube implements ModInitializer {

    public static final String MOD_ID = "spacecube";

    // MCPitanLibの独自のレジストリ MCPitanLib's own registry
    public static final ArchRegistry registry = ArchRegistry.createRegistry(MOD_ID);

    public static final ItemGroup SPACE_CUBE_CREATIVE_TAB = CreativeTabBuilder.create(id("creative_tab")).setIcon(() -> new ItemStack(Items.NORMAL_SPCAE_CUBE, 1)).build();
    public static final RegistryKey<World> SPACE_CUBE_DIMENSION_WORLD_KEY = RegistryKey.of(Registry.WORLD_KEY, id("space_cube_dimension"));

    // TODO: Space Cube Dimensionで雨が降らないようにする (Make it so that it doesn't rain in the Space Cube Dimension)

    @Override
    public void onInitialize() {
        Config.init(FabricLoader.getInstance().getConfigDir().toFile());

        // Register the creative tab
        registry.registerItemGroup(id("creative_tab"), () -> SPACE_CUBE_CREATIVE_TAB);

        // Register the block, item and block entity
        Blocks.init();
        Items.init();
        BlockEntities.init();

        // 1.16.5対応のため (1.16.5の動作は想定していないけどまあ、動けば嬉しいな（笑）)
        // For 1.16.5 support (I don't expect 1.16.5 to work, but I'm happy if it does (lol))
        registry.allRegister();
    }

    public static Identifier id(String id) {
        return new Identifier(MOD_ID, id);
    }

}
