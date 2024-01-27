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
import net.pitan76.spacecube.Config;
import net.pitan76.spacecube.api.data.TunnelSideData;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.api.tunnel.def.ITunnelDef;
import net.pitan76.spacecube.api.tunnel.def.ItemTunnel;
import net.pitan76.spacecube.api.util.SpaceCubeUtil;
import net.pitan76.spacecube.world.ChunkTicketTypes;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SpaceCubeBlockEntity extends ExtendBlockEntity implements SidedInventory, IInventory {

    // scRoomPos = Space Cube Position in Space Cube Dimension (Space Cube Dimension内のスペースキューブの位置)
    public BlockPos scRoomPos = null;

    // tunnel linked sides
    // TunnelType, TunnelSideData(Direction[SpaceCubeBlock Side], BlockPos[Tunnel Position])
    private final Map<TunnelType, TunnelSideData> tunnelSides = new HashMap<>();

    public boolean ticketedChunkSpaceCubeWorld = false;
    public boolean ticketedChunkMainWorld = false;

    public SpaceCubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public SpaceCubeBlockEntity(TileCreateEvent event) {
        super(BlockEntities.SPACE_CUBE_BLOCK_ENTITY.getOrNull(), event);
    }

    public void setScRoomPos(BlockPos scRoomPos) {
        this.scRoomPos = scRoomPos;
    }

    public BlockPos getScRoomPos() {
        return scRoomPos;
    }

    public boolean isScRoomPosNull() {
        return scRoomPos == null;
    }

    @Override
    public void writeNbtOverride(NbtCompound nbt) {
        super.writeNbtOverride(nbt);
        if (!isScRoomPosNull()) {
            // scRoomPos
            // - x: int
            // - y: int
            // - z: int
            NbtCompound scRoomPos_nbt = new NbtCompound();
            scRoomPos_nbt.putInt("x", scRoomPos.getX());
            scRoomPos_nbt.putInt("y", scRoomPos.getY());
            scRoomPos_nbt.putInt("z", scRoomPos.getZ());
            nbt.put("scRoomPos", scRoomPos_nbt);
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
        if (nbt.contains("scRoomPos")) {
            NbtCompound scRoomPos_nbt = nbt.getCompound("scRoomPos");
            int x = scRoomPos_nbt.getInt("x");
            int y = scRoomPos_nbt.getInt("y");
            int z = scRoomPos_nbt.getInt("z");
            scRoomPos = new BlockPos(x, y, z);

            addTicket();
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

    public void addTicket() {
        addTicketSpaceCubeWorld();
        addTicketMainWorld();
    }

    public void addTicketSpaceCubeWorld() {
        if (ticketedChunkSpaceCubeWorld) return;

        if (!Config.enabledChunkLoader()) return;
        if (!(getWorld() instanceof ServerWorld)) return;

        ServerWorld spaceCubeWorld = SpaceCubeUtil.getSpaceCubeWorld((ServerWorld) getWorld());
        if (spaceCubeWorld == null) return;

        ChunkPos chunkPos = new ChunkPos(getScRoomPos());
        spaceCubeWorld.getChunkManager().addTicket(ChunkTicketTypes.CHUNK_LOADER, chunkPos, Config.getChunkLoaderRadius(), chunkPos);
        ticketedChunkSpaceCubeWorld = true;
    }

    public void addTicketMainWorld() {
        if (ticketedChunkMainWorld) return;

        if (!Config.enabledChunkLoader()) return;
        if (!(getWorld() instanceof ServerWorld)) return;

        World mainWorld = getWorld();
        if (!(mainWorld instanceof ServerWorld)) return;

        ChunkPos chunkPos = new ChunkPos(getPos());
        ((ServerWorld) mainWorld).getChunkManager().addTicket(ChunkTicketTypes.CHUNK_LOADER, chunkPos, Config.getChunkLoaderRadius(), chunkPos);
        ticketedChunkMainWorld = true;
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

        int dirindex = dirToIndex(side);
        addTicket();

        return new int[]{dirindex * 2, (dirindex * 2 + 1)};
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

    @Nullable
    public ITunnelDef getTunnelDef(TunnelType type, Direction dir) {
        if (!hasTunnelType(type)) return null;
        TunnelSideData data = getTunnelSide(type);
        if (!data.hasTunnel(dir)) return null;
        BlockEntity blockEntity = SpaceCubeUtil.getSpaceCubeWorld((ServerWorld) world.getMinecraftWorld()).getBlockEntity(data.getTunnel(dir));
        if (!(blockEntity instanceof TunnelWallBlockEntity)) return null;
        TunnelWallBlockEntity tunnelWallBlockEntity = (TunnelWallBlockEntity) blockEntity;
        return tunnelWallBlockEntity.getTunnelDef();
    }

    public ItemStack getImportStack(Direction dir) {
        return getStack(dirToIndex(dir) * 2);
    }

    public ItemStack getExportStack(Direction dir) {
        return getStack(dirToIndex(dir) * 2 + 1);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        IInventory.super.setStack(slot, stack);
        if (!hasTunnelType(TunnelType.ITEM)) return;
        Direction dir = indexToDir(Math.floorDiv(slot, 2));

        ItemTunnel itemTunnel = (ItemTunnel) getTunnelDef(TunnelType.ITEM, dir);
        if (itemTunnel == null) return;

        if (slot % 2 == 0) {
            // 偶数なのでImportStack
            itemTunnel.setImportStack(stack);
        } else {
            // 奇数なのでExportStack
            itemTunnel.setExportStack(stack);
        }
    }

    @Override
    public ItemStack getStack(int slot) {
        if (!hasTunnelType(TunnelType.ITEM)) return IInventory.super.getStack(slot);;
        Direction dir = indexToDir(Math.floorDiv(slot, 2));

        ItemTunnel itemTunnel = (ItemTunnel) getTunnelDef(TunnelType.ITEM, dir);
        if (itemTunnel == null) return IInventory.super.getStack(slot);;

        if (slot % 2 == 0) {
            // 偶数なのでImportStack
            return itemTunnel.getImportStack();
        } else {
            // 奇数なのでExportStack
            return itemTunnel.getExportStack();
        }
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
        return switch (dir) {
            case UP -> 0;
            case DOWN -> 1;
            case NORTH -> 2;
            case SOUTH -> 3;
            case EAST -> 4;
            case WEST -> 5;
            default -> -1;
        };
    }

    public static Direction indexToDir(int index) {
        return switch (index) {
            case 0 -> Direction.UP;
            case 1 -> Direction.DOWN;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.EAST;
            case 5 -> Direction.WEST;
            default -> null;
        };
    }
}
