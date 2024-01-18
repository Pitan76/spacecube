package net.pitan76.spacecube.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.pitan76.spacecube.Blocks;
import net.pitan76.spacecube.api.data.TunnelWallBlockEntityRenderAttachmentData;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;

public class SpaceCubeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        setCutoutLayer(Blocks.TUNNEL_WALL);
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TunnelWallBlockEntity) {
                // getRenderAttachmentData() で、RenderスレッドでもTunnelWallBlockEntityのデータへアクセスできるようになる
                TunnelWallBlockEntityRenderAttachmentData renderAttachmentData = (TunnelWallBlockEntityRenderAttachmentData) ((RenderAttachmentBlockEntity) blockEntity).getRenderAttachmentData();
                if (tintIndex == 0)
                    return renderAttachmentData.getTunnelType().IMPORT_COLOR;

                if (tintIndex == 1)
                    return renderAttachmentData.getTunnelType().INDICATOR_COLOR;

            }
            return TunnelType.NONE.IMPORT_COLOR;
        }, Blocks.TUNNEL_WALL);
    }

    private static void setCutoutLayer(Block block) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout());
    }
}
