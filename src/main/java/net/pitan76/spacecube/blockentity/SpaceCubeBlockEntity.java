package net.pitan76.spacecube.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.pitan76.mcpitanlib.api.event.block.TileCreateEvent;
import net.pitan76.mcpitanlib.api.event.nbt.ReadNbtArgs;
import net.pitan76.mcpitanlib.api.event.nbt.WriteNbtArgs;
import net.pitan76.mcpitanlib.api.gui.inventory.IInventory;
import net.pitan76.mcpitanlib.api.gui.inventory.sided.CompatSidedInventory;
import net.pitan76.mcpitanlib.api.gui.inventory.sided.args.AvailableSlotsArgs;
import net.pitan76.mcpitanlib.api.gui.inventory.sided.args.CanExtractArgs;
import net.pitan76.mcpitanlib.api.gui.inventory.sided.args.CanInsertArgs;
import net.pitan76.mcpitanlib.api.tile.CompatBlockEntity;
import net.pitan76.mcpitanlib.api.util.BlockEntityUtil;
import net.pitan76.mcpitanlib.api.util.ItemStackUtil;
import net.pitan76.mcpitanlib.api.util.NbtUtil;
import net.pitan76.mcpitanlib.api.util.WorldUtil;
import net.pitan76.mcpitanlib.api.util.collection.ItemStackList;
import net.pitan76.spacecube.BlockEntities;
import net.pitan76.spacecube.Config;
import net.pitan76.spacecube.api.data.TunnelSideData;
import net.pitan76.spacecube.api.list.TunnelIODefaultedList;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.api.tunnel.def.ITunnelDef;
import net.pitan76.spacecube.api.tunnel.def.ItemTunnel;
import net.pitan76.spacecube.api.util.SpaceCubeUtil;
import net.pitan76.spacecube.world.ChunkTicketTypes;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpaceCubeBlockEntity extends CompatBlockEntity implements CompatSidedInventory, IInventory {

    // scRoomPos = Space Cube Position in Space Cube Dimension (Space Cube Dimension内のスペースキューブの位置)
    public BlockPos scRoomPos = null;

    // tunnel linked sides
    // TunnelType, TunnelSideData(Direction[SpaceCubeBlock Side], BlockPos[Tunnel Position])
    private final Map<TunnelType, TunnelSideData> tunnelSides = new HashMap<>();

    public boolean ticketedChunkSpaceCubeWorld = false;
    public boolean ticketedChunkMainWorld = false;

    public SpaceCubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, new TileCreateEvent(pos, state));
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
    public void writeNbt(WriteNbtArgs args) {
        super.writeNbt(args);
        NbtCompound nbt = args.getNbt();

        if (!isScRoomPosNull()) {
            // scRoomPos
            // - x: int
            // - y: int
            // - z: int
            NbtUtil.setBlockPos(nbt, "scRoomPos", scRoomPos);
        }
        if (tunnelSides != null) {
            // tunnels
            // - [spacecube:energy_tunnel|spacecube:fluid_tunnel|spacecube:item_tunnel...]
            //  - [up|down|north|south|east|west]
            //   - x: int
            //   - y: int
            //   - z: int
            NbtCompound tunnels_nbt = NbtUtil.create();
            for (TunnelType type : tunnelSides.keySet()) {
                TunnelSideData data = getTunnelSide(type);
                NbtCompound data_nbt = NbtUtil.create();
                for (Map.Entry<Direction, BlockPos> entry : data.getTunnels().entrySet()) {
                    NbtCompound tunnel_nbt = NbtUtil.create();
                    NbtUtil.setBlockPosDirect(tunnel_nbt, entry.getValue());
                    NbtUtil.put(data_nbt, entry.getKey().toString(), tunnel_nbt);
                }
                NbtUtil.put(tunnels_nbt, type.getId().toString(), data_nbt);
            }
            NbtUtil.put(nbt, "tunnels", tunnels_nbt);
        }
    }

    @Override
    public void readNbt(ReadNbtArgs args) {
        super.readNbt(args);
        NbtCompound nbt = args.getNbt();

        if (NbtUtil.has(nbt, "scRoomPos")) {
            scRoomPos = NbtUtil.getBlockPos(nbt, "scRoomPos");
            addTicket();
        }
        if (NbtUtil.has(nbt, "tunnels")) {
            NbtCompound tunnels_nbt = NbtUtil.get(nbt, "tunnels");
            for (String type : NbtUtil.getKeys(tunnels_nbt)) {
                TunnelType tunnelType = TunnelType.fromString(type);
                NbtCompound data_nbt = NbtUtil.get(tunnels_nbt, type);
                TunnelSideData data = new TunnelSideData();
                for (String direction : NbtUtil.getKeys(data_nbt)) {
                    NbtCompound tunnel_nbt = NbtUtil.get(data_nbt, direction);
                    data.addTunnel(Direction.valueOf(direction.toUpperCase()), NbtUtil.getBlockPosDirect(tunnel_nbt));
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
        if (!(BlockEntityUtil.getWorld(this) instanceof ServerWorld)) return;

        ServerWorld spaceCubeWorld = SpaceCubeUtil.getSpaceCubeWorld((ServerWorld) BlockEntityUtil.getWorld(this));
        if (spaceCubeWorld == null) return;

        ChunkPos chunkPos = new ChunkPos(getScRoomPos());
        WorldUtil.addTicket(spaceCubeWorld, ChunkTicketTypes.CHUNK_LOADER.get(), chunkPos, Config.getChunkLoaderRadius());

        ticketedChunkSpaceCubeWorld = true;
    }

    public void addTicketMainWorld() {
        if (ticketedChunkMainWorld) return;

        if (!Config.enabledChunkLoader()) return;
        if (!(BlockEntityUtil.getWorld(this) instanceof ServerWorld)) return;

        World mainWorld = BlockEntityUtil.getWorld(this);
        if (!(mainWorld instanceof ServerWorld)) return;

        ChunkPos chunkPos = new ChunkPos(BlockEntityUtil.getPos(this));
        WorldUtil.addTicket((ServerWorld) mainWorld, ChunkTicketTypes.CHUNK_LOADER.get(), chunkPos, Config.getChunkLoaderRadius());

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

    public Optional<Direction> getRestDir(TunnelType type) {
        if (!tunnelSides.containsKey(type)) addTunnelType(type);
        TunnelSideData data = getTunnelSide(type);
        return data.getRestDir();
    }

    public Optional<Direction> getDir(TunnelType type, BlockPos pos) {
        if (!hasTunnelType(type)) return Optional.empty();
        TunnelSideData data = getTunnelSide(type);
        return data.getDir(pos);
    }

    public Optional<Direction> getNextDir(TunnelType type, Direction dir) {
        if (!hasTunnelType(type)) return Optional.empty();
        TunnelSideData data = getTunnelSide(type);
        return data.getNextDir(dir);
    }

    @Override
    public int[] getAvailableSlots(AvailableSlotsArgs args) {
        Direction side = args.getSide();

        if (!hasTunnelType(TunnelType.ITEM)) return new int[0];
        TunnelSideData data = getTunnelSide(TunnelType.ITEM);
        if (!data.hasTunnel(side)) return new int[0];
        ServerWorld spaceCubeWorld = SpaceCubeUtil.getSpaceCubeWorld((ServerWorld) world);

        if (spaceCubeWorld == null) return new int[0];
        BlockEntity blockEntity = WorldUtil.getBlockEntity(spaceCubeWorld, data.getTunnel(side));

        if (!(blockEntity instanceof TunnelWallBlockEntity)) return new int[0];
        TunnelWallBlockEntity tunnelWallBlockEntity = (TunnelWallBlockEntity) blockEntity;
        ITunnelDef tunnelDef = tunnelWallBlockEntity.getTunnelDef();
        if (!(tunnelDef instanceof ItemTunnel)) return new int[0];

        int dirindex = dirToIndex(side);
        addTicket();

        return new int[]{dirindex * 2, (dirindex * 2 + 1)};
    }

    @Override
    public boolean canInsert(CanInsertArgs args) {
        Direction dir = args.getDir();
        int slot = args.getSlot();

        if (!hasTunnelType(TunnelType.ITEM)) return false;
        TunnelSideData data = getTunnelSide(TunnelType.ITEM);

        if (dir == null) return false;
        int dirindex = dirToIndex(dir);
        return data.hasTunnel(dir) && slot == dirindex * 2 + 1;
    }

    @Override
    public boolean canExtract(CanExtractArgs args) {
        Direction dir = args.getDir();
        int slot = args.getSlot();

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
        ServerWorld serverWorld = SpaceCubeUtil.getSpaceCubeWorld((ServerWorld) world);
        if (serverWorld == null) return null;

        BlockEntity blockEntity = serverWorld.getBlockEntity(data.getTunnel(dir));
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
        if (!hasTunnelType(TunnelType.ITEM)) return IInventory.super.getStack(slot);
        Direction dir = indexToDir(Math.floorDiv(slot, 2));

        ItemTunnel itemTunnel = (ItemTunnel) getTunnelDef(TunnelType.ITEM, dir);
        if (itemTunnel == null) return IInventory.super.getStack(slot);

        if (slot % 2 == 0) {
            // 偶数なのでImportStack
            return itemTunnel.getImportStack();
        } else {
            // 奇数なのでExportStack
            return itemTunnel.getExportStack();
        }
    }

    @Override
    public ItemStackList getItems() {
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

        TunnelIODefaultedList stacks = TunnelIODefaultedList.ofSize(this);
        if (!hasTunnelType(TunnelType.ITEM)) return ItemStackList.ofSize(ItemTunnel.defaultSize * Direction.values().length, ItemStackUtil.empty());

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
            default:
                return -1;
        }
    }

    public static Direction indexToDir(int index) {
        switch (index) {
            case 0:
                return Direction.UP;
            case 1:
                return Direction.DOWN;
            case 2:
                return Direction.NORTH;
            case 3:
                return Direction.SOUTH;
            case 4:
                return Direction.EAST;
            case 5:
                return Direction.WEST;
            default:
                return null;
        }
    }

    public boolean hasTunnel(TunnelType type, net.pitan76.mcpitanlib.midohra.util.math.Direction direction) {
        return hasTunnel(type, direction.toMinecraft());
    }

    public boolean addTunnel(TunnelType type, net.pitan76.mcpitanlib.midohra.util.math.Direction direction, net.pitan76.mcpitanlib.midohra.util.math.BlockPos pos) {
        return addTunnel(type, direction.toMinecraft(), pos.toMinecraft());
    }

    public void removeTunnel(TunnelType type, net.pitan76.mcpitanlib.midohra.util.math.Direction direction) {
        removeTunnel(type, direction.toMinecraft());
    }
}
