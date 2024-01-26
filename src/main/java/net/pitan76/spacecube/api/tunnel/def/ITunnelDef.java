package net.pitan76.spacecube.api.tunnel.def;

import net.minecraft.nbt.NbtCompound;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;

public interface ITunnelDef {
    TunnelType getTunnelType();

    TunnelWallBlockEntity getBlockEntity();

    default void writeNbt(NbtCompound nbt) {

    }

    default void readNbt(NbtCompound nbt) {

    }
}
