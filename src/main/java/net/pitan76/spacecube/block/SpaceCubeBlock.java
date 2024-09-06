package net.pitan76.spacecube.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pitan76.mcpitanlib.api.block.CompatibleBlockSettings;
import net.pitan76.mcpitanlib.api.block.ExtendBlock;
import net.pitan76.mcpitanlib.api.block.ExtendBlockEntityProvider;
import net.pitan76.mcpitanlib.api.entity.Player;
import net.pitan76.mcpitanlib.api.event.block.*;
import net.pitan76.mcpitanlib.api.event.block.result.BlockBreakResult;
import net.pitan76.mcpitanlib.api.event.item.ItemAppendTooltipEvent;
import net.pitan76.mcpitanlib.api.event.nbt.WriteNbtArgs;
import net.pitan76.mcpitanlib.api.util.*;
import net.pitan76.mcpitanlib.api.util.entity.ItemEntityUtil;
import net.pitan76.spacecube.BlockEntities;
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

public class SpaceCubeBlock extends ExtendBlock implements ExtendBlockEntityProvider {
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
                return Blocks.TINY_SPCAE_CUBE;
            case 3:
                return Blocks.SMALL_SPCAE_CUBE;
            case 4:
                return Blocks.NORMAL_SPCAE_CUBE;
            case 5:
                return Blocks.LARGE_SPCAE_CUBE;
            case 6:
                return Blocks.GIANT_SPCAE_CUBE;
            case 7:
                return Blocks.MAXIMUM_SPCAE_CUBE;
            default:
                return Blocks.NORMAL_SPCAE_CUBE;
        }
    }

    @Override
    public ActionResult onRightClick(BlockUseEvent e) {
        Player player = e.getPlayer();

        // If the player is sneaking, pass the event
        // プレイヤーがスニークしている場合はイベントをパス
        if (e.isSneaking()) return ActionResult.PASS;
        // Only run on the server side (サーバー側のみで実行)
        if (e.isClient()) return ActionResult.SUCCESS;

        // If the player is holding a PersonalShrinkingDevice, pass the event
        // プレイヤーがPersonalShrinkingDeviceを持っている場合はイベントをパス
        //    → Process on the PersonalShrinkingDevice side
        //    → PersonalShrinkingDevice側で処理
        Item handItem = player.getMainHandStack().getItem();
        if (handItem instanceof PersonalShrinkingDevice) return ActionResult.PASS;
        if (handItem instanceof SpaceCubeUpgrader) return ActionResult.PASS;

        return ActionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(TileCreateEvent e) {
        return new SpaceCubeBlockEntity(e);
    }

    @Override
    public BlockBreakResult onBreak(BlockBreakEvent e) {
        World world = e.getWorld();
        BlockPos pos = e.getPos();

        // Creative only - サバイバルは getPickStack() で処理 (Survival is handled by getPickStack() )
        BlockEntity blockEntity = WorldUtil.getBlockEntity(world, pos);
        if (!e.isClient() && blockEntity instanceof SpaceCubeBlockEntity) {
            SpaceCubeBlockEntity tile = (SpaceCubeBlockEntity) blockEntity;
            if (e.player.isCreative() && !tile.isScRoomPosNull()) {
                ItemStack stack = ItemStackUtil.create(this);

                NbtCompound nbt = BlockEntityDataUtil.getBlockEntityNbt(stack);
                tile.writeNbt(new WriteNbtArgs(nbt));
                // Todo: use New MCPitanLib API
                NbtUtil.set(nbt, "id", BlockEntityTypeUtil.toID(BlockEntities.SPACE_CUBE_BLOCK_ENTITY.getOrNull()));
                BlockEntityDataUtil.setBlockEntityNbt(stack, nbt);

                ItemEntity itemEntity = ItemEntityUtil.create(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
                ItemEntityUtil.setToDefaultPickupDelay(itemEntity);
                WorldUtil.spawnEntity(world, itemEntity);
            }
        }
        return super.onBreak(e);
    }

    @Override
    public void onPlaced(BlockPlacedEvent e) {
        World world = e.getWorld();
        BlockPos pos = e.getPos();
        ItemStack stack = e.getStack();

        if (!e.isClient() && BlockEntityDataUtil.hasBlockEntityNbt(stack)) {
            BlockEntity blockEntity = WorldUtil.getBlockEntity(world, pos);
            if (blockEntity instanceof SpaceCubeBlockEntity) {
                SpaceCubeBlockEntity spaceCubeBlockEntity = (SpaceCubeBlockEntity) blockEntity;
                BlockEntityDataUtil.readCompatBlockEntityNbtFromStack(stack, spaceCubeBlockEntity);

                ServerWorld spaceCubeWorld = SpaceCubeUtil.getSpaceCubeWorld((ServerWorld) world);
                if (spaceCubeWorld == null)
                    SpaceCube.INSTANCE.error("[SpaceCube] Error: spaceCubeWorld is null.");

                SpaceCubeState spaceCubeState = SpaceCubeState.getOrCreate(spaceCubeWorld.getServer());
                Map<BlockPos, SCBlockPath> spacePosWithSCBlockPath =  spaceCubeState.getSpacePosWithSCBlockPath();

                BlockPos scRoomPos = spaceCubeBlockEntity.getScRoomPos();
                if (spacePosWithSCBlockPath.containsKey(scRoomPos)) {
                    SCBlockPath scBlockPath = spacePosWithSCBlockPath.get(scRoomPos);
                    scBlockPath.setPos(pos);
                    scBlockPath.setDimension(CompatIdentifier.fromMinecraft(WorldUtil.getWorldId(world)));
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
