package net.pitan76.spacecube.block;

import net.pitan76.mcpitanlib.api.block.v2.CompatBlock;
import net.pitan76.mcpitanlib.api.block.v2.CompatibleBlockSettings;
import net.pitan76.mcpitanlib.api.block.ExtendBlockEntityProvider;

public class WallBlock extends CompatBlock implements ExtendBlockEntityProvider {

    public WallBlock(CompatibleBlockSettings settings) {
        super(settings);
    }
}
