package net.pitan76.spacecube.world;

import net.pitan76.mcpitanlib.api.registry.result.RegistryResult;
import net.pitan76.mcpitanlib.midohra.world.chunk.ChunkTicketType;
import net.pitan76.spacecube.SpaceCube;

import static net.pitan76.spacecube.SpaceCube.registry;

public class ChunkTicketTypes {
    public static RegistryResult<ChunkTicketType<?>> CHUNK_LOADER = registry.registerChunkTicketType(SpaceCube._id("chunk_loader"),
            () -> ChunkTicketType.create("spacecube:chunk_loader"));

    public static void init() {

    }
}
