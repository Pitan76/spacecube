package net.pitan76.spacecube.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.pitan76.spacecube.Blocks;

public class SpaceCubeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        setCutoutLayer(Blocks.TUNNEL_WALL);
    }

    private static void setCutoutLayer(Block block) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout());
    }
}
