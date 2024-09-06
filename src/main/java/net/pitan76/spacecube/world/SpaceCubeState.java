package net.pitan76.spacecube.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.pitan76.mcpitanlib.api.event.nbt.ReadNbtArgs;
import net.pitan76.mcpitanlib.api.event.nbt.WriteNbtArgs;
import net.pitan76.mcpitanlib.api.util.CompatIdentifier;
import net.pitan76.mcpitanlib.api.util.NbtUtil;
import net.pitan76.mcpitanlib.api.util.PersistentStateUtil;
import net.pitan76.mcpitanlib.api.util.math.PosUtil;
import net.pitan76.mcpitanlib.api.world.CompatiblePersistentState;
import net.pitan76.spacecube.api.data.SCBlockPath;
import net.pitan76.spacecube.api.data.SCPlayerData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// ディメンションに保存するデータとかを管理できるPersistentStateを使ってみる
// Use PersistentState to manage data to be saved in dimension
public class SpaceCubeState extends CompatiblePersistentState {

    // Player's EntryPos list
    private final Map<UUID, SCPlayerData> playerDataMap = new HashMap<>();

    // 左側のBlockPosは固定、右側のSCBlockPathは一時的保存なことに注意 (右側のSCBlockPathは消えるまたは置換される)
    // Note that the left BlockPos is fixed and the right SCBlockPath is temporarily saved (the right SCBlockPath will be deleted or replaced)
    private final Map<BlockPos, SCBlockPath> spacePosWithSCBlockPath = new HashMap<>();

    public SpaceCubeState() {
        super("spacecube");
    }

    @Override
    public void readNbt(ReadNbtArgs args) {
        NbtCompound nbt = args.getNbt();
        NbtCompound players_nbt = NbtUtil.get(nbt, "players");

        // Read players NBT
        for (String key : NbtUtil.getKeys(players_nbt)) {
            // UUID
            UUID uuid = UUID.fromString(key);

            // EntryPosList
            List<BlockPos> entryPosList = new ArrayList<>();
            NbtList entryPosList_nbt = players_nbt.getList("entryPosList", 10);
            for (int i = 0; i < entryPosList_nbt.size(); i++) {
                NbtCompound entryPos_nbt = entryPosList_nbt.getCompound(i);
                int x = NbtUtil.get(entryPos_nbt, "x", Integer.class);
                int y = NbtUtil.get(entryPos_nbt, "y", Integer.class);
                int z = NbtUtil.get(entryPos_nbt, "z", Integer.class);
                entryPosList.add(PosUtil.flooredBlockPos(x, y, z));
            }

            // Dimension
            CompatIdentifier dimensionId = CompatIdentifier.fromMinecraft(World.OVERWORLD.getValue());
            if (NbtUtil.has(players_nbt, "dimension"))
                dimensionId = CompatIdentifier.of(NbtUtil.get(players_nbt, "dimension", String.class));

            SCPlayerData playerData = new SCPlayerData(uuid, entryPosList, dimensionId);
            playerDataMap.put(uuid, playerData);
        }

        // Get spacePosWithSCBlockPath
        NbtList spacePosWithSCBlockPathList_nbt = nbt.getList("spacePosWithSCBlockPathList", 10);
        for (int i = 0; i < spacePosWithSCBlockPathList_nbt.size(); i++) {
            NbtCompound spacePosWithSCBlockPath_nbt = spacePosWithSCBlockPathList_nbt.getCompound(i);

            // BlockPos (spacePos)
            BlockPos spacePos;
            if (NbtUtil.has(spacePosWithSCBlockPath_nbt, "spacePos")) {
                NbtCompound spacePos_nbt = NbtUtil.get(spacePosWithSCBlockPath_nbt, "spacePos");
                int x = NbtUtil.get(spacePos_nbt, "x", Integer.class);
                int y = NbtUtil.get(spacePos_nbt, "y", Integer.class);
                int z = NbtUtil.get(spacePos_nbt, "z", Integer.class);
                spacePos = PosUtil.flooredBlockPos(x, y, z);
            } else continue;

            // SCBlockPath (spacePosWithSCBlockPath)
            SCBlockPath scBlockPath = new SCBlockPath();
            if (NbtUtil.has(spacePosWithSCBlockPath_nbt, "scBlockPath")) {
                NbtCompound scBlockPath_nbt = NbtUtil.get(spacePosWithSCBlockPath_nbt, "scBlockPath");
                int x = NbtUtil.get(scBlockPath_nbt, "x", Integer.class);
                int y = NbtUtil.get(scBlockPath_nbt, "y", Integer.class);
                int z = NbtUtil.get(scBlockPath_nbt, "z", Integer.class);
                scBlockPath.setPos(PosUtil.flooredBlockPos(x, y, z));

                scBlockPath.setDimension(CompatIdentifier.of(NbtUtil.get(scBlockPath_nbt, "dimension", String.class)));
            }

            spacePosWithSCBlockPath.put(spacePos, scBlockPath);
        }
    }

    public static SpaceCubeState create(ReadNbtArgs args) {
        return new SpaceCubeState(args);
    }

    public SpaceCubeState(ReadNbtArgs args) {
        this();
        readNbt(args);
    }
    
    public static SpaceCubeState create(NbtCompound nbt) {
        return new SpaceCubeState(new ReadNbtArgs(nbt));
    }

    @Override
    public NbtCompound writeNbt(WriteNbtArgs args) {
        NbtCompound nbt = args.getNbt();
        NbtCompound players_nbt = NbtUtil.create();

        // Write players NBT
        for (Map.Entry<UUID, SCPlayerData> entry : playerDataMap.entrySet()) {
            NbtCompound player_nbt = NbtUtil.create();

            // UUID
            UUID uuid = entry.getKey();

            // EntryPosList
            List<BlockPos> entryPosList = entry.getValue().getEntryPosList();
            NbtList entryPosList_nbt = new NbtList();
            for (BlockPos entryPos : entryPosList) {
                NbtCompound entryPos_nbt = NbtUtil.create();
                NbtUtil.set(entryPos_nbt, "x", entryPos.getX());
                NbtUtil.set(entryPos_nbt, "y", entryPos.getY());
                NbtUtil.set(entryPos_nbt, "z", entryPos.getZ());
                entryPosList_nbt.add(entryPos_nbt);
            }
            player_nbt.put("entryPosList", entryPosList_nbt);

            // Dimension
            NbtUtil.set(player_nbt, "dimension",  entry.getValue().getDimension().toString());

            NbtUtil.put(players_nbt, uuid.toString(), player_nbt);
        }

        NbtUtil.put(nbt, "players", players_nbt);

        // Get spacePosWithSCBlockPath
        NbtList spacePosWithSCBlockPathList_nbt = new NbtList();
        for (Map.Entry<BlockPos, SCBlockPath> entry : spacePosWithSCBlockPath.entrySet()) {
            NbtCompound spacePosWithSCBlockPath_nbt = NbtUtil.create();

            // BlockPos (spacePos)
            NbtCompound spacePos_nbt = NbtUtil.create();

            BlockPos spacePos = entry.getKey();
            NbtUtil.set(spacePos_nbt, "x", spacePos.getX());
            NbtUtil.set(spacePos_nbt, "y", spacePos.getY());
            NbtUtil.set(spacePos_nbt, "z", spacePos.getZ());
            NbtUtil.put(spacePosWithSCBlockPath_nbt, "spacePos", spacePos_nbt);

            // SCBlockPath (spacePosWithSCBlockPath)
            if (entry.getValue().pos != null && entry.getValue().dimension != null) {
                NbtCompound scBlockPath_nbt = NbtUtil.create();

                SCBlockPath scBlockPath = entry.getValue();
                BlockPos scBlockPathPos = scBlockPath.getPos();
                NbtUtil.set(scBlockPath_nbt, "x", scBlockPathPos.getX());
                NbtUtil.set(scBlockPath_nbt, "y", scBlockPathPos.getY());
                NbtUtil.set(scBlockPath_nbt, "z", scBlockPathPos.getZ());
                NbtUtil.set(scBlockPath_nbt, "dimension", scBlockPath.getDimension().toString());
                NbtUtil.put(spacePosWithSCBlockPath_nbt, "scBlockPath", scBlockPath_nbt);
            }

            spacePosWithSCBlockPathList_nbt.add(spacePosWithSCBlockPath_nbt);
        }
        nbt.put("spacePosWithSCBlockPathList", spacePosWithSCBlockPathList_nbt);

        return nbt;
    }

    // SpaceCubeStateを取るならこれを使えばいい (Use this to get SpaceCubeState)
    // まあ、サーバーからSpaceCubeStateを取得するって感じだけどなければ、作成する
    // Well, it's like getting SpaceCubeState from the server, but if it doesn't exist, create it
    public static SpaceCubeState getOrCreate(MinecraftServer server) {
        PersistentStateManager manager = PersistentStateUtil.getManagerFromServer(server);
        return PersistentStateUtil.getOrCreate(manager, "spacecube", SpaceCubeState::new, SpaceCubeState::create);
    }

    public Map<UUID, SCPlayerData> getPlayerDataMap() {
        return playerDataMap;
    }

    public Map<BlockPos, SCBlockPath> getSpacePosWithSCBlockPath() {
        return spacePosWithSCBlockPath;
    }

    // ここから下はentryPosListsの操作とかのメソッド

    // SpaceCubeDimension内で追加するときはこっちを使う (Use this when adding in SpaceCubeDimension)
    public void addEntryPos(UUID uuid, BlockPos blockPos) {
        if (playerDataMap.containsKey(uuid)) {
            List<BlockPos> entryPosList = playerDataMap.get(uuid).getEntryPosList();
            entryPosList.add(blockPos);
        } else {
            SCPlayerData playerData = new SCPlayerData(uuid, new ArrayList<>(Collections.singletonList(blockPos)), CompatIdentifier.of("overworld"));
            playerDataMap.put(uuid, playerData);
        }
    }

    // 最初に追加するときはこっちを使う (Use this when adding for the first time)
    public void addEntryPos(UUID uuid, BlockPos blockPos, CompatIdentifier worldId) {
        if (playerDataMap.containsKey(uuid)) {
            List<BlockPos> entryPosList = getEntryPosList(uuid);
            entryPosList.add(blockPos);
        } else {
            SCPlayerData playerData = new SCPlayerData(uuid, new ArrayList<>(Collections.singletonList(blockPos)), worldId);
            playerDataMap.put(uuid, playerData);
        }
    }

    public List<BlockPos> getEntryPosList(UUID uuid) {
        return playerDataMap.get(uuid).getEntryPosList();
    }

    @Nullable
    public BlockPos getLastEntryPos(UUID uuid) {
        if (hasEntryPos(uuid)) {
            List<BlockPos> entryPosList = getEntryPosList(uuid);
            return entryPosList.get(entryPosList.size() - 1);
        }
        return null;
    }

    public BlockPos getLastEntryPosWithRemove(UUID uuid) {
        if (hasEntryPos(uuid)) {
            List<BlockPos> entryPosList = getEntryPosList(uuid);
            BlockPos blockPos = entryPosList.get(entryPosList.size() - 1);
            entryPosList.remove(blockPos);
            return blockPos;
        }
        return null;
    }

    public boolean hasEntryPos(UUID uuid) {
        return existPlayerData(uuid) && !getEntryPosList(uuid).isEmpty();
    }

    public boolean existPlayerData(UUID uuid) {
        return playerDataMap.containsKey(uuid);
    }

    public void removeEntryPos(UUID uuid, BlockPos blockPos) {
        if (hasEntryPos(uuid)) {
            getEntryPosList(uuid).remove(blockPos);
        }
    }

    public void removeEntryPosList(UUID uuid) {
        if (existPlayerData(uuid)) {
            playerDataMap.remove(uuid);
        }
    }

    public int entryPosListSize(UUID uuid) {
        if (hasEntryPos(uuid)) {
            return getEntryPosList(uuid).size();
        }
        return 0;
    }

    public CompatIdentifier getWorldId(UUID uuid) {
        if (!existPlayerData(uuid)) return CompatIdentifier.of("overworld");
        return playerDataMap.get(uuid).getDimension();
    }
}
