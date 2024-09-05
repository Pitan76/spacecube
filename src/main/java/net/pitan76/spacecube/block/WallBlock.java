package net.pitan76.spacecube.block;

import net.pitan76.mcpitanlib.api.block.CompatibleBlockSettings;
import net.pitan76.mcpitanlib.api.block.ExtendBlock;
import net.pitan76.mcpitanlib.api.block.ExtendBlockEntityProvider;

public class WallBlock extends ExtendBlock implements ExtendBlockEntityProvider {

    public WallBlock(CompatibleBlockSettings settings) {
        super(settings);
    }
}
