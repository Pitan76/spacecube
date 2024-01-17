package net.pitan76.spacecube.block;

import ml.pkom.mcpitanlibarch.api.block.CompatibleBlockSettings;
import ml.pkom.mcpitanlibarch.api.block.ExtendBlock;
import ml.pkom.mcpitanlibarch.api.block.ExtendBlockEntityProvider;
import ml.pkom.mcpitanlibarch.api.event.block.TileCreateEvent;
import net.minecraft.block.entity.BlockEntity;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;
import org.jetbrains.annotations.Nullable;

public class WallBlock extends ExtendBlock implements ExtendBlockEntityProvider {

    public WallBlock(CompatibleBlockSettings settings) {
        super(settings);
    }
}
