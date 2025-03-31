package net.pitan76.spacecube.blockentity;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
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
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.pitan76.mcpitanlib.api.event.block.TileCreateEvent;
import net.pitan76.mcpitanlib.api.event.nbt.ReadNbtArgs;
import net.pitan76.mcpitanlib.api.event.nbt.WriteNbtArgs;
import net.pitan76.mcpitanlib.api.gui.inventory.IInventory;
import net.pitan76.mcpitanlib.api.packet.UpdatePacketType;
import net.pitan76.mcpitanlib.api.registry.CompatRegistryLookup;
import net.pitan76.mcpitanlib.api.tile.CompatBlockEntity;
import net.pitan76.mcpitanlib.api.util.*;
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

public class TunnelWallBlockEntity extends CompatBlockEntity implements RenderAttachmentBlockEntity, IInventory, SidedInventory {
    private BlockPos scRoomPos;
    private TunnelType tunnelType;
    private CompatIdentifier tunnelItemId;

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
        NbtCompound nbt = args.getNbt();
        
        if (scRoomPos != null) {
            NbtCompound posNbt = NbtUtil.create();
            NbtUtil.set(posNbt, "x", scRoomPos.getX());
            NbtUtil.set(posNbt, "y", scRoomPos.getY());
            NbtUtil.set(posNbt, "z", scRoomPos.getZ());
            NbtUtil.put(nbt, "scRoomPos", posNbt);
        }
        if (tunnelType != null) {
            NbtUtil.set(nbt, "tunnelType", tunnelType.getId().toString());
        }
        if (tunnelItemId != null) {
            NbtUtil.set(nbt, "tunnelItem", tunnelItemId.toString());
        }

        getTunnelDef().writeNbt(nbt, args.registryLookup);
    }

    @Override
    public void readNbt(ReadNbtArgs args) {
        super.readNbt(args);
        NbtCompound nbt = args.getNbt();
        
        if (NbtUtil.has(nbt, "scRoomPos")) {
            scRoomPos = NbtUtil.getBlockPos(nbt, "scRoomPos");
            addTicket();
        }
        if (NbtUtil.has(nbt, "tunnelType")) {
            tunnelType = TunnelType.fromId(CompatIdentifier.of(NbtUtil.getString(nbt, "tunnelType")));
        }
        if (NbtUtil.has(nbt, "tunnelItem")) {
            tunnelItemId = CompatIdentifier.of(NbtUtil.getString(nbt, "tunnelItem"));
        }

        getTunnelDef().readNbt(nbt, args.registryLookup);
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
        setTunnelItemId(ItemUtil.toID(tunnelItem));
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
    public @Nullable Object getRenderAttachmentData() {
        // Render用スレッドへのアクセスはこれを使う
        // Access to the Render thread is done using this

        return new TunnelWallBlockEntityRenderAttachmentData(getTunnelType());
    }

    public void sync() {
        if (world == null) return;
        if (world.isClient()) return;
        if (!(world instanceof ServerWorld)) return;

        ((ServerWorld) world).getChunkManager().markForUpdate(BlockEntityUtil.getPos(this));
    }

    public Optional<SpaceCubeBlockEntity> getSpaceCubeBlockEntity() {
        if (!getScRoomPos().isPresent()) return Optional.empty();
        if (BlockEntityUtil.getWorld(this) == null) return Optional.empty();
        if (!WorldUtil.getServer(BlockEntityUtil.getWorld(this)).isPresent()) return Optional.empty();

        Optional<MinecraftServer> optionalServer = WorldUtil.getServer(getWorld());
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
    public DefaultedList<ItemStack> getItems() {
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
