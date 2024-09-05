package net.pitan76.spacecube.api.data;

import net.minecraft.util.math.BlockPos;
import net.pitan76.mcpitanlib.api.util.CompatIdentifier;

public class SCBlockPath {
    public BlockPos pos;
    public CompatIdentifier dimension;

    public SCBlockPath(BlockPos blockPos, CompatIdentifier dimension) {
        this.pos = blockPos;
        this.dimension = dimension;
    }

    public SCBlockPath() {

    }

    public void setDimension(CompatIdentifier dimension) {
        this.dimension = dimension;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public CompatIdentifier getDimension() {
        return dimension;
    }

    public BlockPos getPos() {
        return pos;
    }
}
