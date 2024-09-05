package net.pitan76.spacecube.blockentity;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
import net.pitan76.mcpitanlib.api.tile.CompatBlockEntity;
import net.pitan76.mcpitanlib.api.util.CompatIdentifier;
import net.pitan76.mcpitanlib.api.util.ItemUtil;
import net.pitan76.mcpitanlib.api.util.NbtUtil;
import net.pitan76.mcpitanlib.api.util.WorldUtil;
import net.pitan76.spacecube.BlockEntities;
import net.pitan76.spacecube.Config;
import net.pitan76.spacecube.api.data.SCBlockPath;
import net.pitan76.spacecube.api.data.TunnelWallBlockEntityRenderAttachmentData;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.api.tunnel.def.ITunnelDef;
import net.pitan76.spacecube.api.tunnel.def.ItemTunnel;
import net.pitan76.spacecube.world.SpaceCubeState;
import org.jetbrains.annotations.Nullable;

public class TunnelWallBlockEntity extends CompatBlockEntity implements RenderAttachmentBlockEntity, IInventory, SidedInventory {
    private BlockPos scRoomPos;
    private TunnelType tunnelType;
    private CompatIdentifier tunnelItemId;

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
        super(type, new TileCreateEvent(pos, state));
    }

    public TunnelWallBlockEntity(TileCreateEvent event) {
        super(BlockEntities.TUNNEL_WALL_BLOCK_ENTITY.getOrNull(), event);
    }

    @Override
    public UpdatePacketType getUpdatePacketType() {
        return UpdatePacketType.BLOCK_ENTITY_UPDATE_S2C;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = super.toInitialChunkDataNbt();
        writeNbt(new WriteNbtArgs(nbt));
        return nbt;
    }

    @Override
    public void writeNbt(WriteNbtArgs args) {
        super.writeNbt(args);
        NbtCompound nbt = args.getNbt();
        
        if (scRoomPos != null) {
            NbtCompound posNbt = NbtUtil.create();
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

        getTunnelDef().writeNbt(nbt);
    }

    @Override
    public void readNbt(ReadNbtArgs args) {
        super.readNbt(args);
        NbtCompound nbt = args.getNbt();
        
        if (nbt.contains("scRoomPos")) {
            NbtCompound posNbt = nbt.getCompound("scRoomPos");
            scRoomPos = new BlockPos(posNbt.getInt("x"), posNbt.getInt("y"), posNbt.getInt("z"));

            addTicket();
        }
        if (nbt.contains("tunnelType")) {
            tunnelType = TunnelType.fromId(CompatIdentifier.of(nbt.getString("tunnelType")));
        }
        if (nbt.contains("tunnelItem")) {
            tunnelItemId = CompatIdentifier.of(nbt.getString("tunnelItem"));
        }

        getTunnelDef().readNbt(nbt);
    }

    public void addTicket() {
        if (!Config.enabledChunkLoader()) return;
        if (!(getWorld() instanceof ServerWorld)) return;

        SpaceCubeBlockEntity scBlockEntity = getSpaceCubeBlockEntity();
        
        if (scBlockEntity != null) {
            scBlockEntity.addTicket();
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
        return ItemUtil.fromId(getTunnelItemId().toMinecraft());
    }

    @Nullable
    public CompatIdentifier getTunnelItemId() {
        if (tunnelItemId == null) return null;
        return tunnelItemId;
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

        ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
    }

    public SpaceCubeBlockEntity getSpaceCubeBlockEntity() {
        if (getScRoomPos() == null) return null;
        if (getWorld() == null) return null;
        if (getWorld().getServer() == null) return null;

        SpaceCubeState spaceCubeState = SpaceCubeState.getOrCreate(getWorld().getServer());
        SCBlockPath scBlockPath = spaceCubeState.getSpacePosWithSCBlockPath().get(getScRoomPos());

        BlockEntity blockEntity = WorldUtil.getWorld(getWorld(), scBlockPath.getDimension().toMinecraft()).getBlockEntity(scBlockPath.getPos());
        if (!(blockEntity instanceof SpaceCubeBlockEntity)) {return null;}
        return (SpaceCubeBlockEntity) blockEntity;
    }

    public boolean existSpaceCubeBlockEntity() {
        return getSpaceCubeBlockEntity() != null;
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
        if (getTunnelDef() instanceof ItemTunnel) {
            return new int[]{0, 1};
        }

        addTicket();

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
