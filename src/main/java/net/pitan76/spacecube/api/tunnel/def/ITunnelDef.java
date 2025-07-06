package net.pitan76.spacecube.api.tunnel.def;

import net.pitan76.mcpitanlib.api.event.nbt.ReadNbtArgs;
import net.pitan76.mcpitanlib.api.event.nbt.WriteNbtArgs;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;

public interface ITunnelDef {
    TunnelType getTunnelType();

    TunnelWallBlockEntity getBlockEntity();

    default void writeNbt(WriteNbtArgs args) {

    }

    default void readNbt(ReadNbtArgs args) {

    }
}
