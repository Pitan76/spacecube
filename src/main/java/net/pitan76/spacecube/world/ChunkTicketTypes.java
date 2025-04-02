package net.pitan76.spacecube.world;

import net.minecraft.util.math.ChunkPos;
import net.pitan76.mcpitanlib.midohra.world.chunk.ChunkTicketType;

import java.util.Comparator;

public class ChunkTicketTypes {
    public static final ChunkTicketType<ChunkPos> CHUNK_LOADER = ChunkTicketType.create("spacecube:chunk_loader", Comparator.comparingLong(ChunkPos::toLong));
}
