package net.pitan76.spacecube.api.data;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SCBlockPath {
    public BlockPos pos;
    public Identifier dimension;

    public SCBlockPath(BlockPos blockPos, Identifier dimension) {
        this.pos = blockPos;
        this.dimension = dimension;
    }

    public SCBlockPath() {

    }

    public void setDimension(Identifier dimension) {
        this.dimension = dimension;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public Identifier getDimension() {
        return dimension;
    }

    public BlockPos getPos() {
        return pos;
    }
}
