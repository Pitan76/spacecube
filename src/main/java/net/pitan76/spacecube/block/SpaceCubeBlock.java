package net.pitan76.spacecube.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.pitan76.mcpitanlib.api.block.v2.CompatibleBlockSettings;
import net.pitan76.mcpitanlib.api.block.ExtendBlockEntityProvider;
import net.pitan76.mcpitanlib.api.block.v2.CompatBlock;
import net.pitan76.mcpitanlib.api.entity.Player;
import net.pitan76.mcpitanlib.api.event.block.*;
import net.pitan76.mcpitanlib.api.event.block.result.BlockBreakResult;
import net.pitan76.mcpitanlib.api.event.item.ItemAppendTooltipEvent;
import net.pitan76.mcpitanlib.api.util.*;
import net.pitan76.mcpitanlib.api.util.entity.ItemEntityUtil;
import net.pitan76.mcpitanlib.midohra.util.math.BlockPos;
import net.pitan76.mcpitanlib.midohra.world.World;
import net.pitan76.spacecube.Blocks;
import net.pitan76.spacecube.SpaceCube;
import net.pitan76.spacecube.api.data.SCBlockPath;
import net.pitan76.spacecube.api.util.SpaceCubeUtil;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.item.PersonalShrinkingDevice;
import net.pitan76.spacecube.item.SpaceCubeUpgrader;
import net.pitan76.spacecube.world.SpaceCubeState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class SpaceCubeBlock extends CompatBlock implements ExtendBlockEntityProvider {
    public final int size;

    public SpaceCubeBlock(CompatibleBlockSettings settings, int size) {
        super(settings);
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public static SpaceCubeBlock getSpaceCubeBlockFromSize(int size) {
        switch (size) {
            case 2:
                return Blocks.TINY_SPACE_CUBE;
            case 3:
                return Blocks.SMALL_SPACE_CUBE;
            case 4:
                return Blocks.NORMAL_SPACE_CUBE;
            case 5:
                return Blocks.LARGE_SPACE_CUBE;
            case 6:
                return Blocks.GIANT_SPACE_CUBE;
            case 7:
                return Blocks.MAXIMUM_SPACE_CUBE;
            default:
                return Blocks.NORMAL_SPACE_CUBE;
        }
    }

    @Override
    public CompatActionResult onRightClick(BlockUseEvent e) {
        Player player = e.getPlayer();

        // If the player is sneaking, pass the event
        // プレイヤーがスニークしている場合はイベントをパス
        if (e.isSneaking()) return e.pass();
        // Only run on the server side (サーバー側のみで実行)
        if (e.isClient()) return e.success();

        // If the player is holding a PersonalShrinkingDevice, pass the event
        // プレイヤーがPersonalShrinkingDeviceを持っている場合はイベントをパス
        //    → Process on the PersonalShrinkingDevice side
        //    → PersonalShrinkingDevice側で処理
        Item handItem = player.getMainHandStack().getItem();
        if (handItem instanceof PersonalShrinkingDevice ||
                handItem instanceof SpaceCubeUpgrader) return e.pass();

        return e.success();
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(TileCreateEvent e) {
        return new SpaceCubeBlockEntity(e);
    }

    @Override
    public BlockBreakResult onBreak(BlockBreakEvent e) {
        World world = e.getMidohraWorld();
        BlockPos pos = e.getMidohraPos();

        // Creative only - サバイバルは getPickStack() で処理 (Survival is handled by getPickStack() )
        BlockEntity blockEntity = e.getBlockEntity();
        if (!e.isClient() && blockEntity instanceof SpaceCubeBlockEntity) {
            SpaceCubeBlockEntity spaceCubeBlockEntity = (SpaceCubeBlockEntity) blockEntity;
            if (e.player.isCreative() && !spaceCubeBlockEntity.isScRoomPosNull()) {
                ItemStack stack = ItemStackUtil.create(this);
                BlockEntityDataUtil.writeCompatBlockEntityNbtToStack(stack, spaceCubeBlockEntity);

                ItemEntity itemEntity = ItemEntityUtil.create(world.getRaw(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
                ItemEntityUtil.setToDefaultPickupDelay(itemEntity);
                world.spawnEntity(itemEntity);
            }
        }
        return super.onBreak(e);
    }

    @Override
    public void onPlaced(BlockPlacedEvent e) {
        World world = e.getMidohraWorld();
        BlockPos pos = e.getMidohraPos();
        ItemStack stack = e.getStack();


        if (!e.isClient() && BlockEntityDataUtil.hasBlockEntityNbt(stack)) {
            BlockEntity blockEntity = e.getBlockEntity();
            if (blockEntity instanceof SpaceCubeBlockEntity) {
                SpaceCubeBlockEntity spaceCubeBlockEntity = (SpaceCubeBlockEntity) blockEntity;
                BlockEntityDataUtil.readCompatBlockEntityNbtFromStack(stack, spaceCubeBlockEntity);

                ServerWorld spaceCubeWorld = SpaceCubeUtil.getSpaceCubeWorld((ServerWorld) world.getRaw());
                if (spaceCubeWorld == null) {
                    SpaceCube.INSTANCE.error("[SpaceCube] Error: spaceCubeWorld is null.");
                    super.onPlaced(e);
                    return;
                }

                Optional<MinecraftServer> optionalServer = WorldUtil.getServer(spaceCubeWorld);
                SpaceCubeState spaceCubeState = SpaceCubeState.getOrCreate(optionalServer.get());
                Map<net.minecraft.util.math.BlockPos, SCBlockPath> spacePosWithSCBlockPath = spaceCubeState.getSpacePosWithSCBlockPath();

                net.minecraft.util.math.BlockPos scRoomPos = spaceCubeBlockEntity.getScRoomPos();
                if (spacePosWithSCBlockPath.containsKey(scRoomPos)) {
                    SCBlockPath scBlockPath = spacePosWithSCBlockPath.get(scRoomPos);
                    scBlockPath.setPos(pos.toMinecraft());
                    scBlockPath.setDimension(world.getId());
                }
            }
        }
        super.onPlaced(e);
    }

    @Override
    public ItemStack getPickStack(PickStackEvent e) {
        ItemStack stack = super.getPickStack(e);
        try {
            if (e.getBlockEntity() instanceof SpaceCubeBlockEntity) {
                SpaceCubeBlockEntity spaceCubeBlockEntity = (SpaceCubeBlockEntity) e.getBlockEntity();
                BlockEntityDataUtil.writeCompatBlockEntityNbtToStack(stack, spaceCubeBlockEntity);
            }
        } catch (NullPointerException exception) {
            SpaceCube.INSTANCE.error("[SpaceCube] Error: SpaceCubeBlockEntity is null. BlockPos: " + e.pos.toString());
        }
        return stack;
    }

    @Override
    public void appendTooltip(ItemAppendTooltipEvent e) {
        super.appendTooltip(e);
        int side = size * 2 - 1;
        e.addTooltip(TextUtil.translatable("tooltip.spacecube.space_cube_block.size",  side + "x" + side + "x" + side));
    }
}
