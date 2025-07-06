package net.pitan76.spacecube.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.pitan76.mcpitanlib.api.block.v2.CompatibleBlockSettings;
import net.pitan76.mcpitanlib.api.block.ExtendBlockEntityProvider;
import net.pitan76.mcpitanlib.api.event.block.AppendPropertiesArgs;
import net.pitan76.mcpitanlib.api.event.block.BlockBreakEvent;
import net.pitan76.mcpitanlib.api.event.block.BlockUseEvent;
import net.pitan76.mcpitanlib.api.event.block.TileCreateEvent;
import net.pitan76.mcpitanlib.api.event.block.result.BlockBreakResult;
import net.pitan76.mcpitanlib.api.state.property.BooleanProperty;
import net.pitan76.mcpitanlib.api.state.property.CompatProperties;
import net.pitan76.mcpitanlib.api.state.property.DirectionProperty;
import net.pitan76.mcpitanlib.api.util.*;
import net.pitan76.mcpitanlib.midohra.block.BlockState;
import net.pitan76.mcpitanlib.midohra.util.math.BlockPos;
import net.pitan76.mcpitanlib.midohra.util.math.Direction;
import net.pitan76.mcpitanlib.midohra.world.World;
import net.pitan76.spacecube.Blocks;
import net.pitan76.spacecube.api.data.TunnelSideData;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TunnelWallBlock extends WallBlock implements ExtendBlockEntityProvider {

    // 今のところは使っていない (Not used for now)
    public static final DirectionProperty TUNNEL_SIDE = DirectionProperty.of("tunnel_side");

    // トンネルの接続サイド (Connected side of the tunnel)
    public static final DirectionProperty CONNECTED_SIDE = DirectionProperty.of("connected_side");

    // Redstone Power
    public static final BooleanProperty POWERED = CompatProperties.POWERED;

    public TunnelWallBlock(CompatibleBlockSettings settings) {
        super(settings);
        setDefaultState(getDefaultMidohraState().with(CONNECTED_SIDE, Direction.UP).with(TUNNEL_SIDE, Direction.UP).with(POWERED, false));
    }

    public static TunnelType getTunnelType(World world, BlockPos pos) {
        return getTunnelType(world.getRaw(), pos.toMinecraft());
    }

    public static TunnelType getTunnelType(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos) {
        TunnelWallBlockEntity blockEntity = (TunnelWallBlockEntity) WorldUtil.getBlockEntity(world, pos);
        return blockEntity == null ? TunnelType.NONE : blockEntity.getTunnelType();
    }

    @Override
    public CompatActionResult onRightClick(BlockUseEvent e) {
        World world = e.getMidohraWorld();
        BlockPos pos = e.getMidohraPos();

        if (e.isClient()) return e.success();

        BlockState state = e.getMidohraState();

        if (e.isSneaking()) {
            // トンネルをはがす
            if (e.getBlockEntity() instanceof TunnelWallBlockEntity) {
                TunnelWallBlockEntity tunnelWallBlockEntity = (TunnelWallBlockEntity) e.getBlockEntity();

                Optional<Item> item = tunnelWallBlockEntity.getTunnelItem();
                item.ifPresent(value -> e.getPlayer().giveStack(ItemStackUtil.create(value, 1)));

                if (tunnelWallBlockEntity.existSpaceCubeBlockEntity()) {
                    Optional<SpaceCubeBlockEntity> spaceCubeBlockEntity = tunnelWallBlockEntity.getSpaceCubeBlockEntity();

                    TunnelType tunnelType = tunnelWallBlockEntity.getTunnelType();
                    Direction dir = state.get(CONNECTED_SIDE);

                    if (spaceCubeBlockEntity.isPresent() && spaceCubeBlockEntity.get().hasTunnel(tunnelType, dir))
                        spaceCubeBlockEntity.get().removeTunnel(tunnelType, dir);
                }
            }
            world.setBlockState(pos, Blocks.SOLID_WALL.getDefaultMidohraState());

            return e.success();
        }

        if (e.getBlockEntity() instanceof TunnelWallBlockEntity) {
            TunnelWallBlockEntity tunnelWallBlockEntity = (TunnelWallBlockEntity) e.getBlockEntity();

            if (tunnelWallBlockEntity.existSpaceCubeBlockEntity()) {
                Optional<SpaceCubeBlockEntity> spaceCubeBlockEntity = tunnelWallBlockEntity.getSpaceCubeBlockEntity();
                if (!spaceCubeBlockEntity.isPresent()) return e.fail();

                // トンネルの接続サイドを変更する Change the connected side of the tunnel
                if (state.contains(CONNECTED_SIDE)) {
                    Direction dir = state.get(CONNECTED_SIDE);

                    TunnelSideData tunnelSide = spaceCubeBlockEntity.get().getTunnelSide(tunnelWallBlockEntity.getTunnelType());

                    // すべての接続サイドが使われている場合 (If all connected sides are used)
                    if (spaceCubeBlockEntity.get().tunnelIsFull(tunnelWallBlockEntity.getTunnelType())) {
                        e.getPlayer().sendMessage(TextUtil.translatable("message.spacecube.tunnel_full"));
                        return e.fail();
                    }

                    Optional<Direction> nextDir = tunnelSide.getNextDir(dir);
                    if (!nextDir.isPresent()) {
                        e.getPlayer().sendMessage(TextUtil.literal("[SpaceCube] " + "Error: No next direction found"));
                        return e.fail();
                    }

                    Direction nextDirValue = nextDir.get();
                    world.setBlockState(pos, world.getBlockState(pos).with(CONNECTED_SIDE, nextDirValue));
                    if (!tunnelSide.hasTunnel(nextDirValue)) {
                        tunnelSide.removeTunnel(dir);
                        tunnelSide.addTunnel(nextDirValue, pos);
                    }
                }
            }
        }

        return super.onRightClick(e);
    }

    @Override
    public void appendProperties(AppendPropertiesArgs args) {
        args.addProperty(TUNNEL_SIDE);
        args.addProperty(CONNECTED_SIDE);
        args.addProperty(POWERED);
        super.appendProperties(args);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(TileCreateEvent event) {
        return new TunnelWallBlockEntity(event);
    }

    @Override
    public BlockBreakResult onBreak(BlockBreakEvent e) {
        BlockState state = e.getMidohraState();

        if (e.getBlockEntity() instanceof TunnelWallBlockEntity) {
            TunnelWallBlockEntity tunnelWallBlockEntity = (TunnelWallBlockEntity) e.getBlockEntity();

            if (tunnelWallBlockEntity.existSpaceCubeBlockEntity()) {
                Optional<SpaceCubeBlockEntity> spaceCubeBlockEntity = tunnelWallBlockEntity.getSpaceCubeBlockEntity();
                if (!spaceCubeBlockEntity.isPresent()) return super.onBreak(e);

                TunnelType tunnelType = tunnelWallBlockEntity.getTunnelType();
                Direction dir = state.get(CONNECTED_SIDE);

                if (spaceCubeBlockEntity.get().hasTunnel(tunnelType, dir))
                    spaceCubeBlockEntity.get().removeTunnel(tunnelType, dir);
            }
        }
        return super.onBreak(e);
    }
}
