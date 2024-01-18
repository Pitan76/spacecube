package net.pitan76.spacecube.item;

import ml.pkom.mcpitanlibarch.api.event.item.ItemUseOnBlockEvent;
import ml.pkom.mcpitanlibarch.api.item.CompatibleItemSettings;
import ml.pkom.mcpitanlibarch.api.item.ExtendItem;
import ml.pkom.mcpitanlibarch.api.util.TextUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.pitan76.spacecube.Blocks;
import net.pitan76.spacecube.api.data.TunnelSideData;
import net.pitan76.spacecube.api.util.SpaceCubeUtil;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.block.TunnelWallBlock;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;

public class TunnelItem extends ExtendItem {

    public TunnelItem(CompatibleItemSettings settings) {
        super(settings);
    }

    @Override
    public ActionResult onRightClickOnBlock(ItemUseOnBlockEvent event) {
        World world = event.getWorld();
        BlockPos pos = event.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (state.getBlock() == Blocks.SOLID_WALL) {
            world.setBlockState(pos, Blocks.TUNNEL_WALL.getDefaultState());

            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TunnelWallBlockEntity) {
                TunnelWallBlockEntity tunnelWallBlockEntity = (TunnelWallBlockEntity) blockEntity;
                tunnelWallBlockEntity.setTunnelType(getTunnelType());
                tunnelWallBlockEntity.setTunnelItem(event.getStack().getItem());
                if (world.isClient()) return ActionResult.SUCCESS;

                BlockPos scPos = SpaceCubeUtil.getNearestPos((ServerWorld) world, event.getBlockPos());

                tunnelWallBlockEntity.setScPos(scPos);
                tunnelWallBlockEntity.markDirty();
                tunnelWallBlockEntity.sync();

                if (tunnelWallBlockEntity.existSpaceCubeBlockEntity()) {
                    SpaceCubeBlockEntity spaceCubeBlockEntity = tunnelWallBlockEntity.getSpaceCubeBlockEntity();
                    if (spaceCubeBlockEntity.tunnelIsFull(getTunnelType())) {
                        event.player.sendMessage(TextUtil.translatable("message.spacecube.tunnel_full"));
                        return ActionResult.FAIL;
                    }
                    Direction direction = state.get(TunnelWallBlock.CONNECTED_SIDE);
                    spaceCubeBlockEntity.addTunnel(getTunnelType(), direction, pos);
                }
            }

            event.getStack().decrement(1);

            return ActionResult.CONSUME;
        }

        return ActionResult.PASS;
    }

    public TunnelType getTunnelType() {
        return TunnelType.NONE;
    }
}
