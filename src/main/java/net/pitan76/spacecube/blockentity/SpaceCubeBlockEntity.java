package net.pitan76.spacecube.blockentity;

import ml.pkom.mcpitanlibarch.api.event.block.TileCreateEvent;
import ml.pkom.mcpitanlibarch.api.gui.inventory.IInventory;
import ml.pkom.mcpitanlibarch.api.tile.ExtendBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.pitan76.spacecube.BlockEntities;
import net.pitan76.spacecube.api.data.TunnelSideData;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.api.tunnel.def.ITunnelDef;
import net.pitan76.spacecube.api.tunnel.def.ItemTunnel;
import net.pitan76.spacecube.api.util.SpaceCubeUtil;
import net.pitan76.spacecube.world.ChunkLoaderManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SpaceCubeBlockEntity extends ExtendBlockEntity implements SidedInventory, IInventory {

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
                    data.addTunnel(Direction.valueOf(direction.toUpperCase()), new BlockPos(x, y, z));
                }
                tunnelSides.put(tunnelType, data);
            }
        }
    }

    public Map<TunnelType, TunnelSideData> getTunnelSides() {
        return tunnelSides;
    }

    public boolean hasTunnel(TunnelType type, Direction direction) {
        if (!hasTunnelType(type)) return false;
        return getTunnelSide(type).hasTunnel(direction);
    }

    public boolean addTunnel(TunnelType type, Direction direction, BlockPos pos) {
        if (!tunnelSides.containsKey(type)) addTunnelType(type);
        if (getTunnelSide(type).hasTunnel(direction)) {
            return false;
        }
        getTunnelSide(type).addTunnel(direction, pos);
        return true;
    }

    public void addTunnelType(TunnelType type) {
        tunnelSides.put(type, new TunnelSideData());
    }

    public void removeTunnel(TunnelType type, Direction direction) {
        if (!tunnelSides.containsKey(type)) return;
        getTunnelSide(type).removeTunnel(direction);
    }

    public TunnelSideData getTunnelSide(TunnelType type) {
        if (!tunnelSides.containsKey(type)) addTunnelType(type);
        return tunnelSides.get(type);
    }

    public boolean hasTunnelType(TunnelType type) {
        return tunnelSides.containsKey(type);
    }

    public boolean tunnelIsFull(TunnelType type) {
        if (!hasTunnelType(type)) return false;
        return getTunnelSide(type).isFull();
    }

    public Direction getRestDir(TunnelType type) {
        if (!tunnelSides.containsKey(type)) addTunnelType(type);
        TunnelSideData data = getTunnelSide(type);
        return data.getRestDir();
    }

    public Direction getDir(TunnelType type, BlockPos pos) {
        if (!hasTunnelType(type)) return null;
        TunnelSideData data = getTunnelSide(type);
        return data.getDir(pos);
    }

    public Direction getNextDir(TunnelType type, Direction dir) {
        if (!hasTunnelType(type)) return null;
        TunnelSideData data = getTunnelSide(type);
        return data.getNextDir(dir);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {

        if (!hasTunnelType(TunnelType.ITEM)) return new int[0];
        TunnelSideData data = getTunnelSide(TunnelType.ITEM);
        if (!data.hasTunnel(side)) return new int[0];
        ServerWorld spaceCubeWorld = SpaceCubeUtil.getSpaceCubeWorld((ServerWorld) world.getMinecraftWorld());
        BlockEntity blockEntity = spaceCubeWorld.getBlockEntity(data.getTunnel(side));
        if (!(blockEntity instanceof TunnelWallBlockEntity)) return new int[0];
        TunnelWallBlockEntity tunnelWallBlockEntity = (TunnelWallBlockEntity) blockEntity;
        ITunnelDef tunnelDef = tunnelWallBlockEntity.getTunnelDef();
        if (!(tunnelDef instanceof ItemTunnel)) return new int[0];

        ChunkLoaderManager manager = ChunkLoaderManager.getOrCreate(spaceCubeWorld.getServer());
        manager.loadChunk(spaceCubeWorld, new ChunkPos(data.getTunnel(side).getX() >> 4, data.getTunnel(side).getZ() >> 4), getPos());

        int dirindex = dirToIndex(side);
        return new int[]{dirindex * 2, dirindex * 2 + 1};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (!hasTunnelType(TunnelType.ITEM)) return false;
        TunnelSideData data = getTunnelSide(TunnelType.ITEM);

        if (dir == null) return false;
        int dirindex = dirToIndex(dir);
        return data.hasTunnel(dir) && slot == dirindex * 2 + 1;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (!hasTunnelType(TunnelType.ITEM)) return false;
        TunnelSideData data = getTunnelSide(TunnelType.ITEM);

        int dirindex = dirToIndex(dir);
        return data.hasTunnel(dir) && slot == dirindex * 2;
    }

    public ItemStack getImportStack(Direction dir) {
        if (!hasTunnelType(TunnelType.ITEM)) return ItemStack.EMPTY;
        TunnelSideData data = getTunnelSide(TunnelType.ITEM);
        if (!data.hasTunnel(dir)) return ItemStack.EMPTY;
        BlockEntity blockEntity = SpaceCubeUtil.getSpaceCubeWorld((ServerWorld) world.getMinecraftWorld()).getBlockEntity(data.getTunnel(dir));
        if (!(blockEntity instanceof TunnelWallBlockEntity)) return ItemStack.EMPTY;
        TunnelWallBlockEntity tunnelWallBlockEntity = (TunnelWallBlockEntity) blockEntity;
        ITunnelDef tunnelDef = tunnelWallBlockEntity.getTunnelDef();
        if (!(tunnelDef instanceof ItemTunnel)) return ItemStack.EMPTY;
        ItemTunnel itemTunnel = (ItemTunnel) tunnelDef;
        return itemTunnel.getImportStack();
    }

    public ItemStack getExportStack(Direction dir) {
        if (!hasTunnelType(TunnelType.ITEM)) return ItemStack.EMPTY;
        TunnelSideData data = getTunnelSide(TunnelType.ITEM);
        if (!data.hasTunnel(dir)) return ItemStack.EMPTY;
        BlockEntity blockEntity = SpaceCubeUtil.getSpaceCubeWorld((ServerWorld) world.getMinecraftWorld()).getBlockEntity(data.getTunnel(dir));
        if (!(blockEntity instanceof TunnelWallBlockEntity)) return ItemStack.EMPTY;
        TunnelWallBlockEntity tunnelWallBlockEntity = (TunnelWallBlockEntity) blockEntity;
        ITunnelDef tunnelDef = tunnelWallBlockEntity.getTunnelDef();
        if (!(tunnelDef instanceof ItemTunnel)) return ItemStack.EMPTY;
        ItemTunnel itemTunnel = (ItemTunnel) tunnelDef;
        return itemTunnel.getExportStack();
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        // Import Stacks = 偶数 (0, 2, 4, 6, 8, 10...)
        // Export Stacks = 奇数 (1, 3, 5, 7, 9, 11...)

        // ---- Index ----
        // 0: UP Dir Import Stack
        // 1: UP Dir Export Stack
        // 2: DOWN Dir Import Stack
        // 3: DOWN Dir Export Stack
        // 4: NORTH Dir Import Stack
        // 5: NORTH Dir Export Stack
        // 6: SOUTH Dir Import Stack
        // 7: SOUTH Dir Export Stack
        // 8: EAST Dir Import Stack
        // 9: EAST Dir Export Stack
        // 10: WEST Dir Import Stack
        // 11: WEST Dir Export Stack

        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(ItemTunnel.defaultSize * Direction.values().length, ItemStack.EMPTY);

        if (!hasTunnelType(TunnelType.ITEM)) return stacks;

        TunnelSideData data = getTunnelSide(TunnelType.ITEM);
        if (data.isEmpty()) return stacks;

        World scWorld = SpaceCubeUtil.getSpaceCubeWorld((ServerWorld) world.getMinecraftWorld());
        for (Map.Entry<Direction, BlockPos> entry : data.getTunnels().entrySet()) {
            BlockEntity blockEntity = scWorld.getBlockEntity(entry.getValue());
            if (blockEntity instanceof TunnelWallBlockEntity) {
                TunnelWallBlockEntity tunnelWallBlockEntity = (TunnelWallBlockEntity) blockEntity;
                ITunnelDef tunnelDef = tunnelWallBlockEntity.getTunnelDef();
                if (tunnelDef instanceof ItemTunnel) {
                    ItemTunnel itemTunnel = (ItemTunnel) tunnelDef;
                    int dirindex = dirToIndex(entry.getKey());

                    stacks.set(dirindex * 2, itemTunnel.getImportStack());
                    stacks.set(dirindex * 2 + 1, itemTunnel.getExportStack());
                }
            }
        }

        return stacks;

    }

    public static int dirToIndex(Direction dir) {
        switch (dir) {
            case UP:
                return 0;
            case DOWN:
                return 1;
            case NORTH:
                return 2;
            case SOUTH:
                return 3;
            case EAST:
                return 4;
            case WEST:
                return 5;
        }
        return -1;
    }
}
