package net.pitan76.spacecube.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.pitan76.mcpitanlib.api.event.block.TileCreateEvent;
import net.pitan76.mcpitanlib.api.event.nbt.ReadNbtArgs;
import net.pitan76.mcpitanlib.api.event.nbt.WriteNbtArgs;
import net.pitan76.mcpitanlib.api.gui.inventory.IInventory;
import net.pitan76.mcpitanlib.api.packet.UpdatePacketType;
import net.pitan76.mcpitanlib.api.registry.CompatRegistryLookup;
import net.pitan76.mcpitanlib.api.tile.CompatBlockEntity;
import net.pitan76.mcpitanlib.api.tile.RenderAttachmentBlockEntity;
import net.pitan76.mcpitanlib.api.util.BlockEntityUtil;
import net.pitan76.mcpitanlib.api.util.CompatIdentifier;
import net.pitan76.mcpitanlib.api.util.WorldUtil;
import net.pitan76.mcpitanlib.api.util.collection.ItemStackList;
import net.pitan76.mcpitanlib.api.util.item.ItemUtil;
import net.pitan76.mcpitanlib.api.util.math.PosUtil;
import net.pitan76.mcpitanlib.api.util.nbt.v2.NbtRWUtil;
import net.pitan76.mcpitanlib.api.util.world.ChunkManagerUtil;
import net.pitan76.spacecube.BlockEntities;
import net.pitan76.spacecube.Config;
import net.pitan76.spacecube.api.data.SCBlockPath;
import net.pitan76.spacecube.api.data.TunnelWallBlockEntityRenderAttachmentData;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.api.tunnel.def.ITunnelDef;
import net.pitan76.spacecube.api.tunnel.def.ItemTunnel;
import net.pitan76.spacecube.world.SpaceCubeState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TunnelWallBlockEntity extends CompatBlockEntity implements IInventory, RenderAttachmentBlockEntity, SidedInventory {
    private BlockPos scRoomPos = PosUtil.flooredBlockPos(0, 0, 0);
    private TunnelType tunnelType = TunnelType.NONE;
    private CompatIdentifier tunnelItemId = CompatIdentifier.EMPTY;

    // Tunnelの機能定義 (Tunnel function definition)
    public ITunnelDef tunnelDef = null;

    public ITunnelDef getTunnelDef() {
        if (tunnelDef == null)
            tunnelDef = getTunnelType().createTunnelDef(this);

        return tunnelDef;
    }

    public TunnelWallBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, new TileCreateEvent(pos, state));
    }

    public TunnelWallBlockEntity(TileCreateEvent e) {
        super(BlockEntities.TUNNEL_WALL_BLOCK_ENTITY.getOrNull(), e);
    }

    @Override
    public UpdatePacketType getUpdatePacketType() {
        return UpdatePacketType.BLOCK_ENTITY_UPDATE_S2C;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(CompatRegistryLookup registryLookup) {
        NbtCompound nbt = super.toInitialChunkDataNbt(registryLookup);
        writeNbt(new WriteNbtArgs(nbt, registryLookup));
        return nbt;
    }

    @Override
    public void writeNbt(WriteNbtArgs args) {
        super.writeNbt(args);

        if (scRoomPos == null)
            scRoomPos = PosUtil.flooredBlockPos(0, 0, 0);

        NbtRWUtil.putBlockPos(args, "scRoomPos", scRoomPos);
        NbtRWUtil.putString(args, "tunnelType", tunnelType.getId().toString());
        NbtRWUtil.putString(args, "tunnelItem", tunnelItemId.toString());

        getTunnelDef().writeNbt(args);
    }

    @Override
    public void readNbt(ReadNbtArgs args) {
        super.readNbt(args);

        scRoomPos = NbtRWUtil.getBlockPosV(args, "scRoomPos");
        tunnelType = TunnelType.fromString(NbtRWUtil.getStringOrDefault(args, "tunnelType", TunnelType.NONE.getId().toString()));
        tunnelItemId = CompatIdentifier.of(
                NbtRWUtil.getStringOrDefault(args, "tunnelItem", CompatIdentifier.EMPTY.toString()));

        getTunnelDef().readNbt(args);
    }

    public void addTicket() {
        if (!Config.enabledChunkLoader()) return;
        if (!(BlockEntityUtil.getWorld(this) instanceof ServerWorld)) return;

        Optional<SpaceCubeBlockEntity> scBlockEntity = getSpaceCubeBlockEntity();

        scBlockEntity.ifPresent(SpaceCubeBlockEntity::addTicket);
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

    public Optional<BlockPos> getScRoomPos() {
        if (scRoomPos == null) return Optional.empty();
        return Optional.of(scRoomPos);
    }

    public void setTunnelItem(Item tunnelItem) {
        setTunnelItemId(ItemUtil.toId(tunnelItem));
    }

    public Optional<Item> getTunnelItem() {
        if (!getTunnelItemId().isPresent()) return Optional.empty();
        return Optional.ofNullable(ItemUtil.fromId(getTunnelItemId().get()));
    }

    public Optional<CompatIdentifier> getTunnelItemId() {
        if (tunnelItemId == null) return Optional.empty();
        return Optional.of(tunnelItemId);
    }

    public void setTunnelItemId(CompatIdentifier tunnelItemId) {
        this.tunnelItemId = tunnelItemId;
    }

    public void setTunnelItemId(Identifier tunnelItemId) {
        setTunnelItemId(CompatIdentifier.fromMinecraft(tunnelItemId));
    }

    @Override
    public @Nullable Object getCompatRenderData() {
        // Render用スレッドへのアクセスはこれを使う
        // Access to the Render thread is done using this
        return new TunnelWallBlockEntityRenderAttachmentData(getTunnelType());
    }

    public void sync() {
        World world = callGetWorld();
        if (world == null) return;
        if (WorldUtil.isClient(world)) return;
        if (!(world instanceof ServerWorld)) return;

        ChunkManagerUtil.markForUpdate(world, BlockEntityUtil.getPos(this));
    }

    public Optional<SpaceCubeBlockEntity> getSpaceCubeBlockEntity() {
        if (!getScRoomPos().isPresent()) return Optional.empty();
        if (BlockEntityUtil.getWorld(this) == null) return Optional.empty();
        if (!WorldUtil.getServer(BlockEntityUtil.getWorld(this)).isPresent()) return Optional.empty();

        Optional<MinecraftServer> optionalServer = WorldUtil.getServer(callGetWorld());
        if (!optionalServer.isPresent()) return Optional.empty();

        SpaceCubeState spaceCubeState = SpaceCubeState.getOrCreate(optionalServer.get());
        SCBlockPath scBlockPath = spaceCubeState.getSpacePosWithSCBlockPath().get(getScRoomPos().get());

        Optional<ServerWorld> optionalWorld = WorldUtil.getWorld(BlockEntityUtil.getWorld(this), scBlockPath.getDimension());
        if (!optionalWorld.isPresent()) return Optional.empty();

        BlockEntity blockEntity = WorldUtil.getBlockEntity(optionalWorld.get(), scBlockPath.getPos());
        if (!(blockEntity instanceof SpaceCubeBlockEntity)) return Optional.empty();

        return Optional.of((SpaceCubeBlockEntity) blockEntity);
    }

    public boolean existSpaceCubeBlockEntity() {
        return getSpaceCubeBlockEntity().isPresent();
    }

    @Override
    public ItemStack getStack(int slot) {
        if (getTunnelDef() instanceof ItemTunnel) {
            ItemTunnel tunnelDef = (ItemTunnel) getTunnelDef();

            if (slot == 0) {
                return tunnelDef.getImportStack();
            }
            if (slot == 1) {
                return tunnelDef.getExportStack();
            }
        }
        return IInventory.super.getStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        IInventory.super.setStack(slot, stack);
        if (getTunnelDef() instanceof ItemTunnel) {
            ItemTunnel tunnelDef = (ItemTunnel) getTunnelDef();

            if (slot == 0) {
                tunnelDef.setImportStack(stack);
            }
            if (slot == 1) {
                tunnelDef.setExportStack(stack);
            }
        }
    }

    @Override
    public ItemStackList getItems() {
        if (getTunnelDef() instanceof ItemTunnel) {
            ItemTunnel tunnelDef = (ItemTunnel) getTunnelDef();
            return tunnelDef.getStacks();
        }
        return null;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (getTunnelDef() instanceof ItemTunnel)
            return new int[]{0, 1};

        addTicket();
        return new int[0];
    }

    public Optional<Direction> getDirection() {
        Optional<SpaceCubeBlockEntity> scBlockEntity = getSpaceCubeBlockEntity();
        if (!scBlockEntity.isPresent()) return Optional.empty();
        return scBlockEntity.get().getDir(getTunnelType(), BlockEntityUtil.getPos(this));
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
