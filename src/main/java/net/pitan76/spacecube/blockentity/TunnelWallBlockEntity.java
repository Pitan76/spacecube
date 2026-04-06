package net.pitan76.spacecube.blockentity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.pitan76.mcpitanlib.api.event.block.TileCreateEvent;
import net.pitan76.mcpitanlib.api.event.nbt.ReadNbtArgs;
import net.pitan76.mcpitanlib.api.event.nbt.WriteNbtArgs;
import net.pitan76.mcpitanlib.api.gui.inventory.IInventory;
import net.pitan76.mcpitanlib.api.packet.UpdatePacketType;
import net.pitan76.mcpitanlib.api.registry.CompatRegistryLookup;
import net.pitan76.mcpitanlib.api.tile.CompatBlockEntity;
import net.pitan76.mcpitanlib.api.tile.RenderAttachmentBlockEntity;
import net.pitan76.mcpitanlib.api.util.CompatIdentifier;
import net.pitan76.mcpitanlib.api.util.collection.ItemStackList;
import net.pitan76.mcpitanlib.api.util.item.ItemUtil;
import net.pitan76.mcpitanlib.api.util.nbt.v2.NbtRWUtil;
import net.pitan76.mcpitanlib.api.util.world.ChunkManagerUtil;
import net.pitan76.mcpitanlib.midohra.block.BlockState;
import net.pitan76.mcpitanlib.midohra.server.MCServer;
import net.pitan76.mcpitanlib.midohra.util.math.BlockPos;
import net.pitan76.mcpitanlib.midohra.util.math.Direction;
import net.pitan76.mcpitanlib.midohra.world.ServerWorld;
import net.pitan76.mcpitanlib.midohra.world.World;
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
    private BlockPos scRoomPos = BlockPos.of(0, 0, 0);
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
        super(type, new TileCreateEvent(pos.toMinecraft(), state.toMinecraft()));
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
            scRoomPos = BlockPos.of(0, 0, 0);

        NbtRWUtil.putBlockPos(args, "scRoomPos", scRoomPos);
        NbtRWUtil.putString(args, "tunnelType", tunnelType.getId().toString());
        NbtRWUtil.putString(args, "tunnelItem", tunnelItemId.toString());

        getTunnelDef().writeNbt(args);
    }

    @Override
    public void readNbt(ReadNbtArgs args) {
        super.readNbt(args);

        scRoomPos = NbtRWUtil.getBlockPos(args, "scRoomPos");
        tunnelType = TunnelType.fromString(NbtRWUtil.getStringOrDefault(args, "tunnelType", TunnelType.NONE.getId().toString()));
        tunnelItemId = CompatIdentifier.of(
                NbtRWUtil.getStringOrDefault(args, "tunnelItem", CompatIdentifier.EMPTY.toString()));

        getTunnelDef().readNbt(args);
    }

    public void addTicket() {
        if (!Config.enabledChunkLoader()) return;

        World world = getMidohraWorld();
        if (world.isClient()) return;

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

    @Override
    public @Nullable Object getCompatRenderData() {
        // Render用スレッドへのアクセスはこれを使う
        // Access to the Render thread is done using this
        return new TunnelWallBlockEntityRenderAttachmentData(getTunnelType());
    }

    public void sync() {
        World world = getMidohraWorld();
        if (world.toMinecraft() == null) return;
        if (world.isClient()) return;

        ChunkManagerUtil.markForUpdate(world.toMinecraft(), callGetPos());
    }

    public Optional<SpaceCubeBlockEntity> getSpaceCubeBlockEntity() {
        if (!getScRoomPos().isPresent()) return Optional.empty();

        World world = getMidohraWorld();
        if (world.toMinecraft() == null) return Optional.empty();
        if (!world.isServer()) return Optional.empty();

        MCServer server = world.getMCServer();

        SpaceCubeState spaceCubeState = SpaceCubeState.getOrCreate(server);
        SCBlockPath scBlockPath = spaceCubeState.getSpacePosWithSCBlockPath().get(getScRoomPos().get());

        Optional<ServerWorld> optionalWorld = world.getServerWorld(scBlockPath.getDimension());
        if (!optionalWorld.isPresent()) return Optional.empty();

        BlockEntity blockEntity = optionalWorld.get().getBlockEntity(scBlockPath.getPos()).get();
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
    public int[] getAvailableSlots(net.minecraft.util.math.Direction side) {
        if (getTunnelDef() instanceof ItemTunnel)
            return new int[]{0, 1};

        addTicket();
        return new int[0];
    }

    public Optional<Direction> getDirection() {
        Optional<SpaceCubeBlockEntity> scBlockEntity = getSpaceCubeBlockEntity();
        if (!scBlockEntity.isPresent()) return Optional.empty();
        return scBlockEntity.get().getDir(getTunnelType(), getMidohraPos());
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable net.minecraft.util.math.Direction dir) {
        if (getTunnelType() != TunnelType.ITEM) return false;
        return slot == 0;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, net.minecraft.util.math.Direction dir) {
        if (getTunnelType() != TunnelType.ITEM) return false;
        return slot == 1;
    }
}
