package net.pitan76.spacecube.world;

// Tech RebornのChunkLoaderを参考にしています

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ml.pkom.mcpitanlibarch.api.util.PersistentStateUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.stream.Collectors;

public class ChunkLoaderManager extends PersistentState {

    public static Codec<List<LoadedChunk>> CODEC = Codec.list(LoadedChunk.CODEC);

    private static final ChunkTicketType<ChunkPos> CHUNK_LOADER = ChunkTicketType.create("spacecube:chunk_loader", Comparator.comparingLong(ChunkPos::toLong));
    private static final int RADIUS = 1;

    public ChunkLoaderManager() {
        super();
    }

    public static ChunkLoaderManager getOrCreate(MinecraftServer server) {
        PersistentStateManager manager = PersistentStateUtil.getManagerFromServer(server);
        return PersistentStateUtil.getOrCreate(manager, "spacecube_chunk_loader", ChunkLoaderManager::new, ChunkLoaderManager::create);
    }

    private final List<LoadedChunk> loadedChunks = new ArrayList<>();

    public static ChunkLoaderManager create(NbtCompound tag) {
        ChunkLoaderManager chunkLoaderManager = new ChunkLoaderManager();

        chunkLoaderManager.loadedChunks.clear();

        List<LoadedChunk> chunks = CODEC.parse(NbtOps.INSTANCE, tag.getList("loadedchunks", NbtElement.COMPOUND_TYPE))
                .result()
                .orElse(Collections.emptyList());

        chunkLoaderManager.loadedChunks.addAll(chunks);

        return chunkLoaderManager;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compoundTag) {
        CODEC.encodeStart(NbtOps.INSTANCE, loadedChunks)
                .result()
                .ifPresent(tag -> compoundTag.put("loadedchunks", tag));
        return compoundTag;
    }

    public Optional<LoadedChunk> getLoadedChunk(World world, ChunkPos chunkPos, BlockPos chunkLoader){
        return loadedChunks.stream()
                .filter(loadedChunk -> loadedChunk.getWorld().equals(getWorldName(world)))
                .filter(loadedChunk -> loadedChunk.getChunk().equals(chunkPos))
                .filter(loadedChunk -> loadedChunk.getChunkLoader().equals(chunkLoader))
                .findFirst();
    }

    public Optional<LoadedChunk> getLoadedChunk(World world, ChunkPos chunkPos){
        return loadedChunks.stream()
                .filter(loadedChunk -> loadedChunk.getWorld().equals(getWorldName(world)))
                .filter(loadedChunk -> loadedChunk.getChunk().equals(chunkPos))
                .findFirst();
    }

    public List<LoadedChunk> getLoadedChunks(World world, BlockPos chunkLoader){
        return loadedChunks.stream()
                .filter(loadedChunk -> loadedChunk.getWorld().equals(getWorldName(world)))
                .filter(loadedChunk -> loadedChunk.getChunkLoader().equals(chunkLoader))
                .collect(Collectors.toList());
    }

    public boolean isChunkLoaded(World world, ChunkPos chunkPos, BlockPos chunkLoader){
        return getLoadedChunk(world, chunkPos, chunkLoader).isPresent();
    }

    public boolean isChunkLoaded(World world, ChunkPos chunkPos){
        return getLoadedChunk(world, chunkPos).isPresent();
    }


    public void loadChunk(World world, ChunkPos chunkPos, BlockPos chunkLoader){
        if (isChunkLoaded(world, chunkPos, chunkLoader)) return;
        LoadedChunk loadedChunk = new LoadedChunk(chunkPos, getWorldName(world), chunkLoader);
        loadedChunks.add(loadedChunk);

        loadChunk((ServerWorld) world, loadedChunk);

        markDirty();
    }

    public void unloadChunkLoader(World world, BlockPos chunkLoader){
        getLoadedChunks(world, chunkLoader).forEach(loadedChunk -> unloadChunk(world, loadedChunk.getChunk(), chunkLoader));
    }

    public void unloadChunk(World world, ChunkPos chunkPos, BlockPos chunkLoader){
        Optional<LoadedChunk> optionalLoadedChunk = getLoadedChunk(world, chunkPos, chunkLoader);
        Validate.isTrue(optionalLoadedChunk.isPresent(), "chunk is not loaded");

        LoadedChunk loadedChunk = optionalLoadedChunk.get();

        loadedChunks.remove(loadedChunk);

        if(!isChunkLoaded(world, loadedChunk.getChunk())){
            final ServerChunkManager serverChunkManager = ((ServerWorld) world).getChunkManager();
            serverChunkManager.removeTicket(ChunkLoaderManager.CHUNK_LOADER, loadedChunk.getChunk(), RADIUS, loadedChunk.getChunk());
        }
        markDirty();
    }

    public static Identifier getWorldName(World world){
        return world.getRegistryKey().getValue();
    }

    public void loadChunk(ServerWorld world, LoadedChunk loadedChunk) {
        ChunkPos chunkPos = loadedChunk.getChunk();
        world.getChunkManager().addTicket(ChunkLoaderManager.CHUNK_LOADER, chunkPos, RADIUS, chunkPos);
    }

    public static class LoadedChunk {

        public static Codec<ChunkPos> CHUNK_POS_CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                                Codec.INT.fieldOf("x").forGetter(p -> p.x),
                                Codec.INT.fieldOf("z").forGetter(p -> p.z)
                        )
                        .apply(instance, ChunkPos::new));

        public static Codec<LoadedChunk> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                                CHUNK_POS_CODEC.fieldOf("chunk").forGetter(LoadedChunk::getChunk),
                                Identifier.CODEC.fieldOf("world").forGetter(LoadedChunk::getWorld),
                                BlockPos.CODEC.fieldOf("chunkLoader").forGetter(LoadedChunk::getChunkLoader)
                        )
                        .apply(instance, LoadedChunk::new));

        private ChunkPos chunk;
        private Identifier world;
        private BlockPos chunkLoader;

        public LoadedChunk(ChunkPos chunk, Identifier world, BlockPos chunkLoader) {
            this.chunk = chunk;
            this.world = world;
            this.chunkLoader = chunkLoader;
        }

        public ChunkPos getChunk() {
            return chunk;
        }

        public Identifier getWorld() {
            return world;
        }

        public BlockPos getChunkLoader() {
            return chunkLoader;
        }
    }
}