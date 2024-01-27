package net.pitan76.spacecube.world;

import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.math.ChunkPos;

import java.util.Comparator;

public class ChunkTicketTypes {
    public static final ChunkTicketType<ChunkPos> CHUNK_LOADER = ChunkTicketType.create("spacecube:chunk_loader", Comparator.comparingLong(ChunkPos::toLong));
}
