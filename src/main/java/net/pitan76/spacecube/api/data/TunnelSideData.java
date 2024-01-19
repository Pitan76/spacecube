package net.pitan76.spacecube.api.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.Map;

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

    public Direction getRestDir() {
        Direction[] dirs = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (Direction dir : dirs) {
            if (!hasTunnel(dir)) return dir;
        }
        return null;
    }

    public Direction getNextDir(Direction dir) {
        if (isFull()) return null;
        return switch (dir) {
            case UP -> Direction.DOWN;
            case DOWN -> Direction.NORTH;
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.EAST;
            case EAST -> getNextDir(Direction.EAST);
        };
    }

    public Direction getDir(BlockPos pos) {
        for (Map.Entry<Direction, BlockPos> entry : tunnels.entrySet()) {
            if (entry.getValue() == pos)
                return entry.getKey();
        }
        return null;
    }
}
