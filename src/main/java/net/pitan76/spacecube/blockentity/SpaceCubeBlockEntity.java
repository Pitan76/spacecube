package net.pitan76.spacecube.blockentity;

import ml.pkom.mcpitanlibarch.api.event.block.TileCreateEvent;
import ml.pkom.mcpitanlibarch.api.tile.ExtendBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.pitan76.spacecube.BlockEntities;

public class SpaceCubeBlockEntity extends ExtendBlockEntity {
    // scPos = Space Cube Position in Space Cube Dimension (Space Cube Dimension内のスペースキューブの位置)
    public BlockPos scPos = null;

    public SpaceCubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public SpaceCubeBlockEntity(TileCreateEvent event) {
        super(BlockEntities.SPACE_CUBE_BLOCK.getOrNull(), event);
    }

    public void setScPos(BlockPos scPos) {
        this.scPos = scPos;
    }

    public BlockPos getScPos() {
        return scPos;
    }

    public boolean isScPosNull() {
        return scPos == null;
    }

    @Override
    public void writeNbtOverride(NbtCompound nbt) {
        if (!isScPosNull()) {
            NbtCompound scPos_nbt = new NbtCompound();
            scPos_nbt.putInt("x", scPos.getX());
            scPos_nbt.putInt("y", scPos.getY());
            scPos_nbt.putInt("z", scPos.getZ());
            nbt.put("scPos", scPos_nbt);
        }
    }

    @Override
    public void readNbtOverride(NbtCompound nbt) {
        super.readNbtOverride(nbt);
        if (nbt.contains("scPos")) {
            NbtCompound scPos_nbt = nbt.getCompound("scPos");
            int x = scPos_nbt.getInt("x");
            int y = scPos_nbt.getInt("y");
            int z = scPos_nbt.getInt("z");
            scPos = new BlockPos(x, y, z);
        }
    }
}
