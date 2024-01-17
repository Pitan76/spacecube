package net.pitan76.spacecube.item;

import ml.pkom.mcpitanlibarch.api.event.item.ItemUseOnBlockEvent;
import ml.pkom.mcpitanlibarch.api.item.CompatibleItemSettings;
import ml.pkom.mcpitanlibarch.api.item.ExtendItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pitan76.spacecube.Blocks;
import net.pitan76.spacecube.api.util.SpaceCubeUtil;
import net.pitan76.spacecube.api.tunnel.TunnelType;
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

        if (world.isClient()) return ActionResult.SUCCESS;
        if (state.getBlock() == Blocks.SOLID_WALL) {
            world.setBlockState(pos, Blocks.TUNNEL_WALL.getDefaultState());

            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TunnelWallBlockEntity) {
                TunnelWallBlockEntity tunnelWallBlockEntity = (TunnelWallBlockEntity) blockEntity;

                // TODO: トンネルの座標を SpaceCubeState に追加する

                BlockPos scPos = SpaceCubeUtil.getNearestPos((ServerWorld) world, event.getBlockPos());

                tunnelWallBlockEntity.setTunnelType(getTunnelType());
                tunnelWallBlockEntity.setScPos(scPos);
                tunnelWallBlockEntity.setTunnelItem(event.getStack().getItem());
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
