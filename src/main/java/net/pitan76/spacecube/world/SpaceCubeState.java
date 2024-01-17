package net.pitan76.spacecube.world;

import ml.pkom.mcpitanlibarch.api.util.PersistentStateUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.pitan76.spacecube.api.data.SCBlockPath;
import net.pitan76.spacecube.api.data.SCPlayerData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// ディメンションに保存するデータとかを管理できるPersistentStateを使ってみる
// Use PersistentState to manage data to be saved in dimension
public class SpaceCubeState extends PersistentState {

    // Player's EntryPos list
    private final Map<UUID, SCPlayerData> playerDataMap = new HashMap<>();

    // 左側のBlockPosは固定、右側のSCBlockPathは一時的保存なことに注意 (右側のSCBlockPathは消えるまたは置換される)
    // Note that the left BlockPos is fixed and the right SCBlockPath is temporarily saved (the right SCBlockPath will be deleted or replaced)
    private final Map<BlockPos, SCBlockPath> spacePosWithSCBlockPath = new HashMap<>();

    public static SpaceCubeState create(NbtCompound nbt) {
        return new SpaceCubeState(nbt);
    }

    public SpaceCubeState(NbtCompound nbt) {
        this();
        NbtCompound players_nbt = nbt.getCompound("players");

        // Read players NBT
        for (String key : players_nbt.getKeys()) {
            // UUID
            UUID uuid = UUID.fromString(key);

            // EntryPosList
            List<BlockPos> entryPosList = new ArrayList<>();
            NbtList entryPosList_nbt = players_nbt.getList("entryPosList", 10);
            for (int i = 0; i < entryPosList_nbt.size(); i++) {
                NbtCompound entryPos_nbt = entryPosList_nbt.getCompound(i);
                int x = entryPos_nbt.getInt("x");
                int y = entryPos_nbt.getInt("y");
                int z = entryPos_nbt.getInt("z");

                entryPosList.add(new BlockPos(x, y, z));
            }

            // Dimension
            Identifier dimensionId = World.OVERWORLD.getValue();
            if (players_nbt.contains("dimension"))
                dimensionId = new Identifier(players_nbt.getString("dimension"));

            RegistryKey<World> worldKey = RegistryKey.of(Registry.WORLD_KEY, dimensionId);

            SCPlayerData playerData = new SCPlayerData(uuid, entryPosList, worldKey);
            playerDataMap.put(uuid, playerData);
        }

        // Get spacePosWithSCBlockPath
        NbtList spacePosWithSCBlockPathList_nbt = nbt.getList("spacePosWithSCBlockPathList", 10);
        for (int i = 0; i < spacePosWithSCBlockPathList_nbt.size(); i++) {
            NbtCompound spacePosWithSCBlockPath_nbt = spacePosWithSCBlockPathList_nbt.getCompound(i);

            // BlockPos (spacePos)
            BlockPos spacePos;
            if (spacePosWithSCBlockPath_nbt.contains("spacePos")) {
                NbtCompound spacePos_nbt = spacePosWithSCBlockPath_nbt.getCompound("spacePos");
                int x = spacePos_nbt.getInt("x");
                int y = spacePos_nbt.getInt("y");
                int z = spacePos_nbt.getInt("z");

                spacePos = new BlockPos(x, y, z);
            } else continue;

            // SCBlockPath (spacePosWithSCBlockPath)
            SCBlockPath scBlockPath = new SCBlockPath();
            if (spacePosWithSCBlockPath_nbt.contains("scBlockPath")) {
                NbtCompound scBlockPath_nbt = spacePosWithSCBlockPath_nbt.getCompound("scBlockPath");
                int x = scBlockPath_nbt.getInt("x");
                int y = scBlockPath_nbt.getInt("y");
                int z = scBlockPath_nbt.getInt("z");

                scBlockPath.setPos(new BlockPos(x, y, z));

                RegistryKey<World> worldKey = RegistryKey.of(Registry.WORLD_KEY, new Identifier(scBlockPath_nbt.getString("dimension")));

                scBlockPath.setDimension(worldKey);
            }

            spacePosWithSCBlockPath.put(spacePos, scBlockPath);
        }

    }

    public SpaceCubeState() {
        super();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound players_nbt = new NbtCompound();

        // Write players NBT
        for (Map.Entry<UUID, SCPlayerData> entry : playerDataMap.entrySet()) {
            NbtCompound player_nbt = new NbtCompound();

            // UUID
            UUID uuid = entry.getKey();

            // EntryPosList
            List<BlockPos> entryPosList = entry.getValue().getEntryPosList();
            NbtList entryPosList_nbt = new NbtList();
            for (BlockPos entryPos : entryPosList) {
                NbtCompound entryPos_nbt = new NbtCompound();
                entryPos_nbt.putInt("x", entryPos.getX());
                entryPos_nbt.putInt("y", entryPos.getY());
                entryPos_nbt.putInt("z", entryPos.getZ());
                entryPosList_nbt.add(entryPos_nbt);
            }
            player_nbt.put("entryPosList", entryPosList_nbt);

            // Dimension
            RegistryKey<World> worldKey = entry.getValue().getWorldKey();
            player_nbt.putString("dimension", worldKey.getValue().toString());

            players_nbt.put(uuid.toString(), player_nbt);
        }

        nbt.put("players", players_nbt);

        // Get spacePosWithSCBlockPath
        NbtList spacePosWithSCBlockPathList_nbt = new NbtList();
        for (Map.Entry<BlockPos, SCBlockPath> entry : spacePosWithSCBlockPath.entrySet()) {
            NbtCompound spacePosWithSCBlockPath_nbt = new NbtCompound();

            // BlockPos (spacePos)
            NbtCompound spacePos_nbt = new NbtCompound();

            BlockPos spacePos = entry.getKey();
            spacePos_nbt.putInt("x", spacePos.getX());
            spacePos_nbt.putInt("y", spacePos.getY());
            spacePos_nbt.putInt("z", spacePos.getZ());
            spacePosWithSCBlockPath_nbt.put("spacePos", spacePos_nbt);

            // SCBlockPath (spacePosWithSCBlockPath)
            if (entry.getValue().pos != null && entry.getValue().dimension != null) {
                NbtCompound scBlockPath_nbt = new NbtCompound();

                SCBlockPath scBlockPath = entry.getValue();
                BlockPos scBlockPathPos = scBlockPath.getPos();
                scBlockPath_nbt.putInt("x", scBlockPathPos.getX());
                scBlockPath_nbt.putInt("y", scBlockPathPos.getY());
                scBlockPath_nbt.putInt("z", scBlockPathPos.getZ());
                scBlockPath_nbt.putString("dimension", scBlockPath.getDimension().getValue().toString());
                spacePosWithSCBlockPath_nbt.put("scBlockPath", scBlockPath_nbt);
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
            SCPlayerData playerData = new SCPlayerData(uuid, new ArrayList<>(Collections.singletonList(blockPos)), World.OVERWORLD);
            playerDataMap.put(uuid, playerData);
        }
    }

    // 最初に追加するときはこっちを使う (Use this when adding for the first time)
    public void addEntryPos(UUID uuid, BlockPos blockPos, RegistryKey<World> worldKey) {
        if (playerDataMap.containsKey(uuid)) {
            List<BlockPos> entryPosList = getEntryPosList(uuid);
            entryPosList.add(blockPos);
        } else {
            SCPlayerData playerData = new SCPlayerData(uuid, new ArrayList<>(Collections.singletonList(blockPos)), worldKey);
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

    public RegistryKey<World> getWorldKey(UUID uuid) {
        if (!existPlayerData(uuid)) return World.OVERWORLD;
        return playerDataMap.get(uuid).getWorldKey();
    }
}
