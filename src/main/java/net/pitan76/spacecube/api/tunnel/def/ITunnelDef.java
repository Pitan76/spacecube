package net.pitan76.spacecube.api.tunnel.def;

import net.minecraft.nbt.NbtCompound;
import net.pitan76.mcpitanlib.api.registry.CompatRegistryLookup;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;

public interface ITunnelDef {
    TunnelType getTunnelType();

    TunnelWallBlockEntity getBlockEntity();

    default void writeNbt(NbtCompound nbt, CompatRegistryLookup registryLookup) {

    }

    default void readNbt(NbtCompound nbt, CompatRegistryLookup registryLookup) {

    }
}
