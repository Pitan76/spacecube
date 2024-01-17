package net.pitan76.spacecube.api.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class SCBlockPath {
    public BlockPos pos;
    public RegistryKey<World> dimension;

    public SCBlockPath(BlockPos blockPos, RegistryKey<World> dimension) {
        this.pos = blockPos;
        this.dimension = dimension;
    }

    public SCBlockPath() {

    }

    public void setDimension(RegistryKey<World> dimension) {
        this.dimension = dimension;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public RegistryKey<World> getDimension() {
        return dimension;
    }

    public BlockPos getPos() {
        return pos;
    }
}
