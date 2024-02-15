package net.pitan76.spacecube.block;

import net.pitan76.mcpitanlib.api.block.CompatibleBlockSettings;
import net.pitan76.mcpitanlib.api.block.ExtendBlock;
import net.pitan76.mcpitanlib.api.block.ExtendBlockEntityProvider;
import net.pitan76.mcpitanlib.api.entity.Player;
import net.pitan76.mcpitanlib.api.event.block.BlockUseEvent;
import net.pitan76.mcpitanlib.api.event.block.TileCreateEvent;
import net.pitan76.mcpitanlib.api.util.TextUtil;
import net.pitan76.mcpitanlib.api.util.WorldUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.pitan76.spacecube.BlockEntities;
import net.pitan76.spacecube.Blocks;
import net.pitan76.spacecube.api.data.SCBlockPath;
import net.pitan76.spacecube.api.util.SpaceCubeUtil;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.item.PersonalShrinkingDevice;
import net.pitan76.spacecube.item.SpaceCubeUpgrader;
import net.pitan76.spacecube.world.SpaceCubeState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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
        Item handItem = player.getStackInHand(Hand.MAIN_HAND).getItem();
        if (handItem instanceof PersonalShrinkingDevice) return ActionResult.PASS;
        if (handItem instanceof SpaceCubeUpgrader) return ActionResult.PASS;

        return ActionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(TileCreateEvent event) {
        return new SpaceCubeBlockEntity(event);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // Creative only - サバイバルは getPickStack() で処理 (Survival is handled by getPickStack() )
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!world.isClient() && blockEntity instanceof SpaceCubeBlockEntity) {
            SpaceCubeBlockEntity tile = (SpaceCubeBlockEntity) blockEntity;
            if (player.isCreative() && !tile.isScRoomPosNull()) {
                ItemStack itemStack = new ItemStack(this);
                NbtCompound nbt = new NbtCompound();
                tile.writeNbtOverride(nbt);

                // if there is a BlockEntityTag, set nbt to the item (BlockEntityTagが存在する際はnbtをアイテムにセット)
                if (!nbt.isEmpty())
                    itemStack.setSubNbt("BlockEntityTag", nbt);

                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, itemStack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            }
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!world.isClient() && stack.hasNbt()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SpaceCubeBlockEntity) {
                SpaceCubeBlockEntity spaceCubeBlockEntity = (SpaceCubeBlockEntity) blockEntity;

                NbtCompound nbt = stack.getSubNbt("BlockEntityTag");
                spaceCubeBlockEntity.readNbtOverride(nbt);

                ServerWorld spaceCubeWorld = SpaceCubeUtil.getSpaceCubeWorld((ServerWorld) world);
                if (spaceCubeWorld == null) {
                    System.out.println("[SpaceCube] Error: spaceCubeWorld is null.");
                }

                SpaceCubeState spaceCubeState = SpaceCubeState.getOrCreate(spaceCubeWorld.getServer());
                Map<BlockPos, SCBlockPath> spacePosWithSCBlockPath =  spaceCubeState.getSpacePosWithSCBlockPath();

                BlockPos scRoomPos = spaceCubeBlockEntity.getScRoomPos();
                if (spacePosWithSCBlockPath.containsKey(scRoomPos)) {
                    SCBlockPath scBlockPath = spacePosWithSCBlockPath.get(scRoomPos);
                    scBlockPath.setPos(pos);
                    scBlockPath.setDimension(WorldUtil.getWorldId(world));
                }
            }
        }
        super.onPlaced(world, pos, state, placer, stack);
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        ItemStack stack = super.getPickStack(world, pos, state);
        try {
            world.getBlockEntity(pos, BlockEntities.SPACE_CUBE_BLOCK_ENTITY.getOrNull()).ifPresent(blockEntity -> blockEntity.setStackNbt(stack));
        } catch (NullPointerException e) {
            System.out.println("[SpaceCube] Error: SpaceCubeBlockEntity is null. BlockPos: " + pos.toString());
        }
        return stack;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        super.appendTooltip(stack, world, tooltip, options);
        int side = size * 2 - 1;
        tooltip.add(TextUtil.translatable("tooltip.spacecube.space_cube_block.size",  side + "x" + side + "x" + side));
    }
}
