package net.pitan76.spacecube.blockentity;

import ml.pkom.mcpitanlibarch.api.event.block.TileCreateEvent;
import ml.pkom.mcpitanlibarch.api.tile.ExtendBlockEntity;
import ml.pkom.mcpitanlibarch.api.util.ItemUtil;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pitan76.spacecube.BlockEntities;
import net.pitan76.spacecube.api.data.SCBlockPath;
import net.pitan76.spacecube.api.data.TunnelWallBlockEntityRenderAttachmentData;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.api.util.SpaceCubeUtil;
import net.pitan76.spacecube.world.SpaceCubeState;
import org.jetbrains.annotations.Nullable;

public class TunnelWallBlockEntity extends ExtendBlockEntity implements RenderAttachmentBlockEntity {
    private BlockPos scPos;
    private TunnelType tunnelType;
    private Identifier tunnelItemId;

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
        if (scPos != null) {
            NbtCompound posNbt = new NbtCompound();
            posNbt.putInt("x", scPos.getX());
            posNbt.putInt("y", scPos.getY());
            posNbt.putInt("z", scPos.getZ());
            nbt.put("scPos", posNbt);
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
        if (nbt.contains("scPos")) {
            NbtCompound posNbt = nbt.getCompound("scPos");
            scPos = new BlockPos(posNbt.getInt("x"), posNbt.getInt("y"), posNbt.getInt("z"));
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

    public void setScPos(BlockPos scPos) {
        this.scPos = scPos;
    }

    @Nullable
    public BlockPos getScPos() {
        if (scPos == null) return null;
        return scPos;
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
        World mcWorld = world.getMinecraftWorld();
        if (getScPos() == null) return null;
        if (mcWorld == null) return null;

        SpaceCubeState spaceCubeState = SpaceCubeState.getOrCreate(mcWorld.getServer());
        SCBlockPath scBlockPath = spaceCubeState.getSpacePosWithSCBlockPath().get(getScPos());
        BlockEntity blockEntity = mcWorld.getServer().getWorld(scBlockPath.getDimension()).getBlockEntity(scBlockPath.getPos());
        if (!(blockEntity instanceof SpaceCubeBlockEntity)) {return null;}
        return (SpaceCubeBlockEntity) blockEntity;
    }

    public boolean existSpaceCubeBlockEntity() {
        return getSpaceCubeBlockEntity() != null;
    }
}
