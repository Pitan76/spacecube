package net.pitan76.spacecube.item;

import ml.pkom.mcpitanlibarch.api.event.item.ItemUseOnBlockEvent;
import ml.pkom.mcpitanlibarch.api.item.CompatibleItemSettings;
import ml.pkom.mcpitanlibarch.api.item.ExtendItem;
import net.minecraft.block.BlockState;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.pitan76.spacecube.Blocks;

public class TunnelItem extends ExtendItem {
    public TunnelItem(CompatibleItemSettings settings) {
        super(settings);
    }

    @Override
    public ActionResult onRightClickOnBlock(ItemUseOnBlockEvent event) {
        World world = event.getWorld();
        BlockState state = world.getBlockState(event.getBlockPos());

        if (world.isClient()) return ActionResult.SUCCESS;
        if (state.getBlock() == Blocks.WALL || state.getBlock() == Blocks.SOLID_WALL) {

            return ActionResult.SUCCESS;

        }

        return super.onRightClickOnBlock(event);
    }
}
