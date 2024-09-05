package net.pitan76.spacecube.api.data;

import net.minecraft.util.math.BlockPos;
import net.pitan76.mcpitanlib.api.util.CompatIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SCPlayerData {
    // プレイヤーのUUID Player's UUID
    private UUID uuid;

    // プレイヤーのEntryPos(入場座標)のリスト List of player's EntryPos (entry coordinates)
    private List<BlockPos> entryPosList;

    // プレイヤーが入っていたワールド World where the player was
    private CompatIdentifier dimension;

    public SCPlayerData(UUID uuid, List<BlockPos> entryPosList, CompatIdentifier dimension) {
        this.uuid = uuid;
        this.entryPosList = entryPosList;
        this.dimension = dimension;
    }

    public SCPlayerData(UUID uuid, CompatIdentifier dimension) {
        this(uuid, new ArrayList<>(), dimension);
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<BlockPos> getEntryPosList() {
        return entryPosList;
    }

    public CompatIdentifier getDimension() {
        return dimension;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setEntryPosList(List<BlockPos> entryPosList) {
        this.entryPosList = entryPosList;
    }

    public void setDimension(CompatIdentifier dimension) {
        this.dimension = dimension;
    }
}
