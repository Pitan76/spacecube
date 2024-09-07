package net.pitan76.spacecube.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.pitan76.mcpitanlib.api.event.item.ItemUseOnBlockEvent;
import net.pitan76.mcpitanlib.api.item.CompatibleItemSettings;
import net.pitan76.mcpitanlib.api.item.ExtendItem;
import net.pitan76.mcpitanlib.api.util.BlockEntityUtil;
import net.pitan76.mcpitanlib.api.util.ItemStackUtil;
import net.pitan76.mcpitanlib.api.util.TextUtil;
import net.pitan76.mcpitanlib.api.util.WorldUtil;
import net.pitan76.spacecube.Blocks;
import net.pitan76.spacecube.api.data.TunnelSideData;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.api.util.SpaceCubeUtil;
import net.pitan76.spacecube.block.TunnelWallBlock;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;

import java.util.Optional;

public class TunnelItem extends ExtendItem {

    public TunnelItem(CompatibleItemSettings settings) {
        super(settings);
    }

    @Override
    public ActionResult onRightClickOnBlock(ItemUseOnBlockEvent e) {
        World world = e.getWorld();
        BlockPos pos = e.getBlockPos();
        BlockState state = WorldUtil.getBlockState(world, pos);

        if (state.getBlock() == Blocks.SOLID_WALL) {
            WorldUtil.setBlockState(world, pos, Blocks.TUNNEL_WALL.getDefaultState().with(TunnelWallBlock.CONNECTED_SIDE, Direction.UP));

            BlockEntity blockEntity = e.getBlockEntity();
            if (blockEntity instanceof TunnelWallBlockEntity) {
                TunnelWallBlockEntity tunnelWallBlockEntity = (TunnelWallBlockEntity) blockEntity;
                tunnelWallBlockEntity.setTunnelType(getTunnelType());
                tunnelWallBlockEntity.setTunnelItem(e.getStack().getItem());
                if (e.isClient()) return ActionResult.SUCCESS;

                BlockPos scRoomPos = SpaceCubeUtil.getNearestPos((ServerWorld) world, e.getBlockPos());

                tunnelWallBlockEntity.setScRoomPos(scRoomPos);
                BlockEntityUtil.markDirty(tunnelWallBlockEntity);
                tunnelWallBlockEntity.sync();

                if (tunnelWallBlockEntity.existSpaceCubeBlockEntity()) {
                    Optional<SpaceCubeBlockEntity> spaceCubeBlockEntity = tunnelWallBlockEntity.getSpaceCubeBlockEntity();
                    if (!spaceCubeBlockEntity.isPresent()) return ActionResult.FAIL;

                    state = WorldUtil.getBlockState(world, pos);
                    Direction dir = state.get(TunnelWallBlock.CONNECTED_SIDE);

                    TunnelSideData tunnelSide = spaceCubeBlockEntity.get().getTunnelSide(getTunnelType());
                    if (tunnelSide.isFull()) {
                        e.player.sendMessage(TextUtil.translatable("message.spacecube.tunnel_full"));
                        WorldUtil.setBlockState(world, pos, Blocks.SOLID_WALL.getDefaultState());
                        return ActionResult.FAIL;
                    }

                    // Connected Sideが存在する場合は別のSideに割り当てる
                    if (tunnelSide.hasTunnel(dir)) {
                        dir = tunnelSide.getRestDir().get();
                        WorldUtil.setBlockState(world, pos, state.with(TunnelWallBlock.CONNECTED_SIDE, dir));
                    }
                    tunnelSide.addTunnel(dir, pos);
                }

                // Chunk Loader
                tunnelWallBlockEntity.addTicket();
            }

            ItemStackUtil.decrementCount(e.getStack(), 1);
            return ActionResult.CONSUME;
        }

        return ActionResult.PASS;
    }

    public TunnelType getTunnelType() {
        return TunnelType.NONE;
    }
}
