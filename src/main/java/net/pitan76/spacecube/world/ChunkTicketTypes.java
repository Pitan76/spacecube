package net.pitan76.spacecube.world;

import net.minecraft.util.math.ChunkPos;
import net.pitan76.mcpitanlib.midohra.world.chunk.ChunkTicketType;
import net.pitan76.spacecube.SpaceCube;

public class ChunkTicketTypes {
    public static final ChunkTicketType<ChunkPos> CHUNK_LOADER = ChunkTicketType.create("spacecube:chunk_loader");

    public static void init() {
        SpaceCube.registry.registerChunkTicketType(() -> CHUNK_LOADER);
    }
}
