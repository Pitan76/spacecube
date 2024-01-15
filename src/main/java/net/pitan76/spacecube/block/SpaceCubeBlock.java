package net.pitan76.spacecube.block;

import ml.pkom.mcpitanlibarch.api.block.CompatibleBlockSettings;
import ml.pkom.mcpitanlibarch.api.block.ExtendBlock;
import ml.pkom.mcpitanlibarch.api.block.ExtendBlockEntityProvider;
import ml.pkom.mcpitanlibarch.api.entity.Player;
import ml.pkom.mcpitanlibarch.api.event.block.BlockUseEvent;
import ml.pkom.mcpitanlibarch.api.event.block.TileCreateEvent;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.item.PersonalShrinkingDevice;
import org.jetbrains.annotations.Nullable;

public class SpaceCubeBlock extends ExtendBlock implements ExtendBlockEntityProvider {
    public SpaceCubeBlock(CompatibleBlockSettings settings) {
        super(settings);
    }

    @Override
    public ActionResult onRightClick(BlockUseEvent event) {
        Player player = event.getPlayer();
        World world = event.getWorld();

        // If the player is sneaking, pass the event
        // プレイヤーがスニークしている場合はイベントをパス
        if (player.isSneaking()) return ActionResult.PASS;
        // Only run on the server side (サーバー側のみで実行)
        if (world.isClient()) return ActionResult.SUCCESS;

        // If the player is holding a PersonalShrinkingDevice, pass the event
        // プレイヤーがPersonalShrinkingDeviceを持っている場合はイベントをパス
        //    → Process on the PersonalShrinkingDevice side
        //    → PersonalShrinkingDevice側で処理
        if (player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof PersonalShrinkingDevice) return ActionResult.PASS;

        return ActionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(TileCreateEvent event) {
        return new SpaceCubeBlockEntity(event);
    }
}
