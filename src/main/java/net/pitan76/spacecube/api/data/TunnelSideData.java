package net.pitan76.spacecube.api.data;

import net.pitan76.mcpitanlib.midohra.util.math.BlockPos;
import net.pitan76.mcpitanlib.midohra.util.math.Direction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TunnelSideData {
    public final Map<Direction, BlockPos> tunnels = new HashMap<>();

    public TunnelSideData() {

    }

    public TunnelSideData(Direction dir, BlockPos pos) {
        tunnels.put(dir, pos);
    }

    public Map<Direction, BlockPos> getTunnels() {
        return tunnels;
    }

    public void addTunnel(Direction dir, BlockPos pos) {
        tunnels.put(dir, pos);
    }

    public void removeTunnel(Direction dir) {
        tunnels.remove(dir);
    }

    public boolean hasTunnel(Direction dir) {
        return tunnels.containsKey(dir);
    }

    public BlockPos getTunnel(Direction dir) {
        return tunnels.get(dir);
    }

    public boolean hasTunnels() {
        return !tunnels.isEmpty();
    }

    public void clear() {
        tunnels.clear();
    }

    public int size() {
        return tunnels.size();
    }

    public boolean isEmpty() {
        return tunnels.isEmpty();
    }

    public boolean isFull() {
        return tunnels.size() >= Direction.values().length;
    }

    public boolean isNotFull() {
        return tunnels.size() < Direction.values().length;
    }

    public boolean isNone() {
        return tunnels.isEmpty();
    }

    public Optional<Direction> getRestDir() {
        Direction[] dirs = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (Direction dir : dirs) {
            if (!hasTunnel(dir)) return Optional.of(dir);
        }
        return Optional.empty();
    }

    public Optional<Direction> getNextDir(Direction dir) {
        if (isFull()) return Optional.empty();
        if (dir.equals(Direction.UP)) {
            return (hasTunnel(Direction.DOWN)) ? getNextDir(Direction.DOWN) : Optional.of(Direction.DOWN);
        } else if (dir.equals(Direction.DOWN)) {
            return (hasTunnel(Direction.NORTH)) ? getNextDir(Direction.NORTH) : Optional.of(Direction.NORTH);
        } else if (dir.equals(Direction.NORTH)) {
            return (hasTunnel(Direction.SOUTH)) ? getNextDir(Direction.SOUTH) : Optional.of(Direction.SOUTH);
        } else if (dir.equals(Direction.SOUTH)) {
            return (hasTunnel(Direction.WEST)) ? getNextDir(Direction.WEST) : Optional.of(Direction.WEST);
        } else if (dir.equals(Direction.WEST)) {
            return (hasTunnel(Direction.EAST)) ? getNextDir(Direction.EAST) : Optional.of(Direction.EAST);
        } else if (dir.equals(Direction.EAST)) {
            return (hasTunnel(Direction.UP)) ? getNextDir(Direction.UP) : Optional.of(Direction.UP);
        }
        return Optional.empty();
    }

    public Optional<Direction> getDir(BlockPos pos) {
        for (Map.Entry<Direction, BlockPos> entry : tunnels.entrySet()) {
            if (entry.getValue() == pos)
                return Optional.ofNullable(entry.getKey());
        }
        return Optional.empty();
    }
}
