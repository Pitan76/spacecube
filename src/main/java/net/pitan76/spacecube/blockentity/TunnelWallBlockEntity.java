package net.pitan76.spacecube.blockentity;

import ml.pkom.mcpitanlibarch.api.event.block.TileCreateEvent;
import ml.pkom.mcpitanlibarch.api.gui.inventory.IInventory;
import ml.pkom.mcpitanlibarch.api.tile.ExtendBlockEntity;
import ml.pkom.mcpitanlibarch.api.util.ItemUtil;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.pitan76.spacecube.BlockEntities;
import net.pitan76.spacecube.api.data.SCBlockPath;
import net.pitan76.spacecube.api.data.TunnelWallBlockEntityRenderAttachmentData;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.api.tunnel.def.ITunnelDef;
import net.pitan76.spacecube.api.tunnel.def.ItemTunnel;
import net.pitan76.spacecube.world.ChunkLoaderManager;
import net.pitan76.spacecube.world.SpaceCubeState;
import org.jetbrains.annotations.Nullable;

public class TunnelWallBlockEntity extends ExtendBlockEntity implements RenderAttachmentBlockEntity, IInventory, SidedInventory {
    private BlockPos scRoomPos;
    private TunnelType tunnelType;
    private Identifier tunnelItemId;

    // Tunnelの機能定義 (Tunnel function definition)
    public ITunnelDef tunnelDef = null;

    // item tunnel用
    // private DefaultedList<ItemStack> stacks = DefaultedList.ofSize(1, ItemStack.EMPTY);


    public ITunnelDef getTunnelDef() {
        if (tunnelDef == null)
            tunnelDef = getTunnelType().createTunnelDef(this);

        return tunnelDef;
    }

    public TunnelWallBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TunnelWallBlockEntity(TileCreateEvent event) {
        super(BlockEntities.TUNNEL_WALL_BLOCK_ENTITY.getOrNull(), event);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = super.toInitialChunkDataNbt();
        writeNbtOverride(nbt);
        return nbt;
    }

    @Override
    public void writeNbtOverride(NbtCompound nbt) {
        super.writeNbtOverride(nbt);
        if (scRoomPos != null) {
            NbtCompound posNbt = new NbtCompound();
            posNbt.putInt("x", scRoomPos.getX());
            posNbt.putInt("y", scRoomPos.getY());
            posNbt.putInt("z", scRoomPos.getZ());
            nbt.put("scRoomPos", posNbt);
        }
        if (tunnelType != null) {
            nbt.putString("tunnelType", tunnelType.getId().toString());
        }
        if (tunnelItemId != null) {
            nbt.putString("tunnelItem", tunnelItemId.toString());
        }
    }

    @Override
    public void readNbtOverride(NbtCompound nbt) {
        super.readNbtOverride(nbt);
        if (nbt.contains("scRoomPos")) {
            NbtCompound posNbt = nbt.getCompound("scRoomPos");
            scRoomPos = new BlockPos(posNbt.getInt("x"), posNbt.getInt("y"), posNbt.getInt("z"));
        }
        if (nbt.contains("tunnelType")) {
            tunnelType = TunnelType.fromId(new Identifier(nbt.getString("tunnelType")));
        }
        if (nbt.contains("tunnelItem")) {
            tunnelItemId = new Identifier(nbt.getString("tunnelItem"));
        }
    }

    public TunnelType getTunnelType() {
        if (tunnelType == null) return TunnelType.NONE;
        return tunnelType;
    }

    public void setTunnelType(TunnelType tunnelType) {
        this.tunnelType = tunnelType;
    }

    public void setScRoomPos(BlockPos scRoomPos) {
        this.scRoomPos = scRoomPos;
    }

    @Nullable
    public BlockPos getScRoomPos() {
        if (scRoomPos == null) return null;
        return scRoomPos;
    }

    public void setTunnelItem(Item tunnelItem) {
        setTunnelItemId(ItemUtil.toID(tunnelItem));
    }

    @Nullable
    public Item getTunnelItem() {
        if (tunnelItemId == null) return null;
        return ItemUtil.fromId(getTunnelItemId());
    }

    @Nullable
    public Identifier getTunnelItemId() {
        if (tunnelItemId == null) return null;
        return tunnelItemId;
    }

    public void setTunnelItemId(Identifier tunnelItemId) {
        this.tunnelItemId = tunnelItemId;
    }

    @Override
    public @Nullable Object getRenderAttachmentData() {
        // Render用スレッドへのアクセスはこれを使う
        // Access to the Render thread is done using this

        return new TunnelWallBlockEntityRenderAttachmentData(getTunnelType());
    }

    public void sync() {
        World mcWorld = world.getMinecraftWorld();

        if (mcWorld == null) return;
        if (mcWorld.isClient()) return;
        if (!(mcWorld instanceof ServerWorld)) return;

        ((ServerWorld) world.getMinecraftWorld()).getChunkManager().markForUpdate(getPos());
    }

    public SpaceCubeBlockEntity getSpaceCubeBlockEntity() {
        if (getScRoomPos() == null) return null;
        if (getWorld() == null) return null;
        if (getWorld().getServer() == null) return null;

        SpaceCubeState spaceCubeState = SpaceCubeState.getOrCreate(getWorld().getServer());
        SCBlockPath scBlockPath = spaceCubeState.getSpacePosWithSCBlockPath().get(getScRoomPos());

        BlockEntity blockEntity = getWorld().getServer().getWorld(scBlockPath.getDimension()).getBlockEntity(scBlockPath.getPos());
        if (!(blockEntity instanceof SpaceCubeBlockEntity)) {return null;}
        return (SpaceCubeBlockEntity) blockEntity;
    }

    public boolean existSpaceCubeBlockEntity() {
        return getSpaceCubeBlockEntity() != null;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        if (getTunnelDef() instanceof ItemTunnel) {
            ItemTunnel tunnelDef = (ItemTunnel) getTunnelDef();
            return tunnelDef.getStacks();
        }
        return null;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (getTunnelDef() instanceof ItemTunnel) {
            SpaceCubeBlockEntity scBlockEntity = getSpaceCubeBlockEntity();
            if (scBlockEntity == null) return new int[]{0, 1};
            World mainWorld = scBlockEntity.getWorld();
            ChunkLoaderManager manager = ChunkLoaderManager.getOrCreate(mainWorld.getServer());
            manager.loadChunk(world.getMinecraftWorld(), new ChunkPos(scBlockEntity.getPos().getX() >> 4, scBlockEntity.getPos().getZ() >> 4), scBlockEntity.getScRoomPos());

            return new int[]{0, 1};
        }

        return new int[0];
    }

    public Direction getDirection() {
        SpaceCubeBlockEntity scBlockEntity = getSpaceCubeBlockEntity();
        if (scBlockEntity == null) return null;
        return scBlockEntity.getDir(getTunnelType(), getPos());
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (getTunnelType() != TunnelType.ITEM) return false;
        return slot == 0;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (getTunnelType() != TunnelType.ITEM) return false;
        return slot == 1;
    }
}
