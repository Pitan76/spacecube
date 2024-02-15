package net.pitan76.spacecube.block;

import net.pitan76.mcpitanlib.api.block.CompatibleBlockSettings;
import net.pitan76.mcpitanlib.api.block.ExtendBlock;
import net.pitan76.mcpitanlib.api.block.ExtendBlockEntityProvider;
import net.pitan76.mcpitanlib.api.event.block.TileCreateEvent;
import net.minecraft.block.entity.BlockEntity;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;
import org.jetbrains.annotations.Nullable;

public class WallBlock extends ExtendBlock implements ExtendBlockEntityProvider {

    public WallBlock(CompatibleBlockSettings settings) {
        super(settings);
    }
}
