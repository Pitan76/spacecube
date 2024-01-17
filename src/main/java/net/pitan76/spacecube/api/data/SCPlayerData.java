package net.pitan76.spacecube.api.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SCPlayerData {
    // プレイヤーのUUID Player's UUID
    private UUID uuid;

    // プレイヤーのEntryPos(入場座標)のリスト List of player's EntryPos (entry coordinates)
    private List<BlockPos> entryPosList;

    // プレイヤーが入っていたワールド World where the player was
    private RegistryKey<World> worldKey;

    public SCPlayerData(UUID uuid, List<BlockPos> entryPosList, RegistryKey<World> worldKey) {
        this.uuid = uuid;
        this.entryPosList = entryPosList;
        this.worldKey = worldKey;
    }

    public SCPlayerData(UUID uuid, RegistryKey<World> worldKey) {
        this(uuid, new ArrayList<>(), worldKey);
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<BlockPos> getEntryPosList() {
        return entryPosList;
    }

    public RegistryKey<World> getWorldKey() {
        return worldKey;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setEntryPosList(List<BlockPos> entryPosList) {
        this.entryPosList = entryPosList;
    }

    public void setWorldKey(RegistryKey<World> worldKey) {
        this.worldKey = worldKey;
    }
}
