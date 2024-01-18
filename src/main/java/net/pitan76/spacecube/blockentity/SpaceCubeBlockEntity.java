package net.pitan76.spacecube.blockentity;

import ml.pkom.mcpitanlibarch.api.event.block.TileCreateEvent;
import ml.pkom.mcpitanlibarch.api.tile.ExtendBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.pitan76.spacecube.BlockEntities;
import net.pitan76.spacecube.api.data.TunnelSideData;
import net.pitan76.spacecube.api.tunnel.TunnelType;

import java.util.HashMap;
import java.util.Map;

public class SpaceCubeBlockEntity extends ExtendBlockEntity {
    // scPos = Space Cube Position in Space Cube Dimension (Space Cube Dimension内のスペースキューブの位置)
    public BlockPos scPos = null;

    // tunnel linked sides
    // TunnelType, TunnelSideData(Direction[SpaceCubeBlock Side], BlockPos[Tunnel Position])
    private final Map<TunnelType, TunnelSideData> tunnelSides = new HashMap<>();

    public SpaceCubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public SpaceCubeBlockEntity(TileCreateEvent event) {
        super(BlockEntities.SPACE_CUBE_BLOCK_ENTITY.getOrNull(), event);
    }

    public void setScPos(BlockPos scPos) {
        this.scPos = scPos;
    }

    public BlockPos getScPos() {
        return scPos;
    }

    public boolean isScPosNull() {
        return scPos == null;
    }

    @Override
    public void writeNbtOverride(NbtCompound nbt) {
        super.writeNbtOverride(nbt);
        if (!isScPosNull()) {
            // scPos
            // - x: int
            // - y: int
            // - z: int
            NbtCompound scPos_nbt = new NbtCompound();
            scPos_nbt.putInt("x", scPos.getX());
            scPos_nbt.putInt("y", scPos.getY());
            scPos_nbt.putInt("z", scPos.getZ());
            nbt.put("scPos", scPos_nbt);
        }
        if (tunnelSides != null) {
            // tunnels
            // - [spacecube:energy_tunnel|spacecube:fluid_tunnel|spacecube:item_tunnel...]
            //  - [up|down|north|south|east|west]
            //   - x: int
            //   - y: int
            //   - z: int
            NbtCompound tunnels_nbt = new NbtCompound();
            for (TunnelType type : tunnelSides.keySet()) {
                TunnelSideData data = getTunnelSide(type);
                NbtCompound data_nbt = new NbtCompound();
                for (Map.Entry<Direction, BlockPos> entry : data.getTunnels().entrySet()) {
                    NbtCompound tunnel_nbt = new NbtCompound();
                    tunnel_nbt.putInt("x", entry.getValue().getX());
                    tunnel_nbt.putInt("y", entry.getValue().getY());
                    tunnel_nbt.putInt("z", entry.getValue().getZ());
                    data_nbt.put(entry.getKey().toString(), tunnel_nbt);
                }
                tunnels_nbt.put(type.getId().toString(), data_nbt);
            }
            nbt.put("tunnels", tunnels_nbt);
        }
    }

    @Override
    public void readNbtOverride(NbtCompound nbt) {
        super.readNbtOverride(nbt);
        if (nbt.contains("scPos")) {
            NbtCompound scPos_nbt = nbt.getCompound("scPos");
            int x = scPos_nbt.getInt("x");
            int y = scPos_nbt.getInt("y");
            int z = scPos_nbt.getInt("z");
            scPos = new BlockPos(x, y, z);
        }
        if (nbt.contains("tunnels")) {
            NbtCompound tunnels_nbt = nbt.getCompound("tunnels");
            for (String type : tunnels_nbt.getKeys()) {
                TunnelType tunnelType = TunnelType.fromString(type);
                NbtCompound data_nbt = tunnels_nbt.getCompound(type);
                TunnelSideData data = new TunnelSideData();
                for (String direction : data_nbt.getKeys()) {
                    NbtCompound tunnel_nbt = data_nbt.getCompound(direction);
                    int x = tunnel_nbt.getInt("x");
                    int y = tunnel_nbt.getInt("y");
                    int z = tunnel_nbt.getInt("z");
                    data.addTunnel(Direction.valueOf(direction), new BlockPos(x, y, z));
                }
                tunnelSides.put(tunnelType, data);
            }
        }
    }

    public Map<TunnelType, TunnelSideData> getTunnelSides() {
        return tunnelSides;
    }

    public boolean hasTunnel(TunnelType type, Direction direction) {
        if (hasTunnelType(type)) return false;
        return getTunnelSide(type).hasTunnel(direction);
    }

    public void addTunnel(TunnelType type, Direction direction, BlockPos pos) {
        if (!tunnelSides.containsKey(type)) addTunnelType(type);
        getTunnelSide(type).addTunnel(direction, pos);
    }

    public void addTunnelType(TunnelType type) {
        tunnelSides.put(type, new TunnelSideData());
    }

    public void removeTunnel(TunnelType type, Direction direction) {
        if (!tunnelSides.containsKey(type)) return;
        getTunnelSide(type).removeTunnel(direction);
    }

    public TunnelSideData getTunnelSide(TunnelType type) {
        return tunnelSides.get(type);
    }

    public boolean hasTunnelType(TunnelType type) {
        return tunnelSides.containsKey(type);
    }

    public boolean tunnelIsFull(TunnelType type) {
        if (!hasTunnelType(type)) return false;
        return getTunnelSide(type).isFull();
    }
}
