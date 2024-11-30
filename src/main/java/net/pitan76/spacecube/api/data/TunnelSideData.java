package net.pitan76.spacecube.api.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

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

    public TunnelSideData(net.pitan76.mcpitanlib.midohra.util.math.Direction dir, net.pitan76.mcpitanlib.midohra.util.math.BlockPos pos) {
        this(dir.toMinecraft(), pos.toMinecraft());
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
        switch (dir) {
            case UP:
                return (hasTunnel(Direction.DOWN)) ? getNextDir(Direction.DOWN) : Optional.of(Direction.DOWN);
            case DOWN:
                return (hasTunnel(Direction.NORTH)) ? getNextDir(Direction.NORTH) : Optional.of(Direction.NORTH);
            case NORTH:
                return (hasTunnel(Direction.SOUTH)) ? getNextDir(Direction.SOUTH) : Optional.of(Direction.SOUTH);
            case SOUTH:
                return (hasTunnel(Direction.WEST)) ? getNextDir(Direction.WEST) : Optional.of(Direction.WEST);
            case WEST:
                return (hasTunnel(Direction.EAST)) ? getNextDir(Direction.EAST) : Optional.of(Direction.EAST);
            case EAST:
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

    public void addTunnel(net.pitan76.mcpitanlib.midohra.util.math.Direction dir, net.pitan76.mcpitanlib.midohra.util.math.BlockPos pos) {
        addTunnel(dir.toMinecraft(), pos.toMinecraft());
    }

    public void removeTunnel(net.pitan76.mcpitanlib.midohra.util.math.Direction dir) {
        removeTunnel(dir.toMinecraft());
    }

    public boolean hasTunnel(net.pitan76.mcpitanlib.midohra.util.math.Direction dir) {
        return hasTunnel(dir.toMinecraft());
    }

    public BlockPos getTunnel(net.pitan76.mcpitanlib.midohra.util.math.Direction dir) {
        return getTunnel(dir.toMinecraft());
    }

    public Optional<net.pitan76.mcpitanlib.midohra.util.math.Direction> getRestMidohraDir() {
        return getRestDir().map(net.pitan76.mcpitanlib.midohra.util.math.Direction::of);
    }

    public Optional<net.pitan76.mcpitanlib.midohra.util.math.Direction> getNextDir(net.pitan76.mcpitanlib.midohra.util.math.Direction dir) {
        return getNextDir(dir.toMinecraft()).map(net.pitan76.mcpitanlib.midohra.util.math.Direction::of);
    }
}
