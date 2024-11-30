package net.pitan76.spacecube.item;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.pitan76.mcpitanlib.api.event.item.ItemUseOnBlockEvent;
import net.pitan76.mcpitanlib.api.item.v2.CompatItem;
import net.pitan76.mcpitanlib.api.item.v2.CompatibleItemSettings;
import net.pitan76.mcpitanlib.api.util.*;
import net.pitan76.mcpitanlib.midohra.block.BlockState;
import net.pitan76.mcpitanlib.midohra.util.math.Direction;
import net.pitan76.mcpitanlib.midohra.world.World;
import net.pitan76.spacecube.Blocks;
import net.pitan76.spacecube.api.data.TunnelSideData;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.api.util.SpaceCubeUtil;
import net.pitan76.spacecube.block.TunnelWallBlock;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;

import java.util.Optional;

public class TunnelItem extends CompatItem {

    public TunnelItem(CompatibleItemSettings settings) {
        super(settings);
    }

    @Override
    public CompatActionResult onRightClickOnBlock(ItemUseOnBlockEvent e) {
        World world = e.getMidohraWorld();
        BlockPos pos = e.getBlockPos();
        BlockState state = e.getMidohraState();

        if (state.getBlock().get() == Blocks.SOLID_WALL) {
            world.setBlockState(net.pitan76.mcpitanlib.midohra.util.math.BlockPos.of(pos), Blocks.TUNNEL_WALL.getDefaultMidohraState().with(TunnelWallBlock.CONNECTED_SIDE, Direction.UP));

            BlockEntity blockEntity = e.getBlockEntity();
            if (blockEntity instanceof TunnelWallBlockEntity) {
                TunnelWallBlockEntity tunnelWallBlockEntity = (TunnelWallBlockEntity) blockEntity;
                tunnelWallBlockEntity.setTunnelType(getTunnelType());
                tunnelWallBlockEntity.setTunnelItem(e.getStack().getItem());
                if (e.isClient()) return e.success();

                BlockPos scRoomPos = SpaceCubeUtil.getNearestPos((ServerWorld) world.getRaw(), e.getBlockPos());

                tunnelWallBlockEntity.setScRoomPos(scRoomPos);
                BlockEntityUtil.markDirty(tunnelWallBlockEntity);
                tunnelWallBlockEntity.sync();

                if (tunnelWallBlockEntity.existSpaceCubeBlockEntity()) {
                    Optional<SpaceCubeBlockEntity> spaceCubeBlockEntity = tunnelWallBlockEntity.getSpaceCubeBlockEntity();
                    if (!spaceCubeBlockEntity.isPresent()) return e.fail();

                    state = world.getBlockState(net.pitan76.mcpitanlib.midohra.util.math.BlockPos.of(pos));
                    Direction dir = state.get(TunnelWallBlock.CONNECTED_SIDE);

                    TunnelSideData tunnelSide = spaceCubeBlockEntity.get().getTunnelSide(getTunnelType());
                    if (tunnelSide.isFull()) {
                        e.player.sendMessage(TextUtil.translatable("message.spacecube.tunnel_full"));
                        world.setBlockState(net.pitan76.mcpitanlib.midohra.util.math.BlockPos.of(pos), Blocks.SOLID_WALL.getDefaultMidohraState());
                        return e.fail();
                    }

                    // Connected Sideが存在する場合は別のSideに割り当てる
                    if (tunnelSide.hasTunnel(dir)) {
                        dir = tunnelSide.getRestMidohraDir().get();
                        world.setBlockState(net.pitan76.mcpitanlib.midohra.util.math.BlockPos.of(pos), state.with(TunnelWallBlock.CONNECTED_SIDE, dir));
                    }
                    tunnelSide.addTunnel(dir, net.pitan76.mcpitanlib.midohra.util.math.BlockPos.of(pos));
                }

                // Chunk Loader
                tunnelWallBlockEntity.addTicket();
            }

            ItemStackUtil.decrementCount(e.getStack(), 1);
            return e.consume();
        }

        return e.pass();
    }

    public TunnelType getTunnelType() {
        return TunnelType.NONE;
    }
}
