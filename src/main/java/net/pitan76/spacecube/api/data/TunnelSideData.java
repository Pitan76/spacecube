package net.pitan76.spacecube.api.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.Map;

public class TunnelSideData {
    public final Map<Direction, BlockPos> tunnels = new HashMap<>();

    public TunnelSideData() {
    }

    public TunnelSideData(Direction direction, BlockPos pos) {
        tunnels.put(direction, pos);
    }

    public Map<Direction, BlockPos> getTunnels() {
        return tunnels;
    }

    public void addTunnel(Direction direction, BlockPos pos) {
        tunnels.put(direction, pos);
    }

    public void removeTunnel(Direction direction) {
        tunnels.remove(direction);
    }

    public boolean hasTunnel(Direction direction) {
        return tunnels.containsKey(direction);
    }

    public BlockPos getTunnel(Direction direction) {
        return tunnels.get(direction);
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
}
