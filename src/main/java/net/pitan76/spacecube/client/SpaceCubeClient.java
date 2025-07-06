package net.pitan76.spacecube.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.entity.BlockEntity;
import net.pitan76.mcpitanlib.api.client.registry.CompatRegistryClient;
import net.pitan76.spacecube.Blocks;
import net.pitan76.spacecube.api.data.TunnelWallBlockEntityRenderAttachmentData;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;

public class SpaceCubeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CompatRegistryClient.registerCutoutBlock(Blocks.TUNNEL_WALL);
        CompatRegistryClient.registerColorProviderBlock((e) -> {
            int tintIndex = e.getTintIndex();
            BlockEntity blockEntity = e.getBlockEntity();
            if (blockEntity instanceof TunnelWallBlockEntity) {
                // getRenderData() で、RenderスレッドでもTunnelWallBlockEntityのデータへアクセスできるようになる
                TunnelWallBlockEntityRenderAttachmentData renderAttachmentData = (TunnelWallBlockEntityRenderAttachmentData) e.getRenderData();
                //System.out.println(renderAttachmentData.getTunnelType().getId());

                if (tintIndex == 0)
                    return renderAttachmentData.getTunnelType().IMPORT_COLOR;

                if (tintIndex == 1)
                    return renderAttachmentData.getTunnelType().INDICATOR_COLOR;

            }
            return TunnelType.NONE.IMPORT_COLOR;
        }, Blocks.TUNNEL_WALL);
    }
}
