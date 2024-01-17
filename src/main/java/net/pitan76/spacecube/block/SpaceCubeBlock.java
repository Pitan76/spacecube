package net.pitan76.spacecube.block;

import ml.pkom.mcpitanlibarch.api.block.CompatibleBlockSettings;
import ml.pkom.mcpitanlibarch.api.block.ExtendBlock;
import ml.pkom.mcpitanlibarch.api.block.ExtendBlockEntityProvider;
import ml.pkom.mcpitanlibarch.api.entity.Player;
import ml.pkom.mcpitanlibarch.api.event.block.BlockUseEvent;
import ml.pkom.mcpitanlibarch.api.event.block.TileCreateEvent;
import ml.pkom.mcpitanlibarch.api.util.TextUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.pitan76.spacecube.api.SpaceCubeUtil;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.api.data.SCBlockPath;
import net.pitan76.spacecube.item.PersonalShrinkingDevice;
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

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // Creative only - サバイバルは getPickStack() で処理 (Survival is handled by getPickStack() )
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!world.isClient() && blockEntity instanceof SpaceCubeBlockEntity) {
            SpaceCubeBlockEntity tile = (SpaceCubeBlockEntity) blockEntity;
            if (player.isCreative() && !tile.isScPosNull()) {
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

                BlockPos scPos = spaceCubeBlockEntity.getScPos();
                if (spacePosWithSCBlockPath.containsKey(scPos)) {
                    SCBlockPath scBlockPath = spacePosWithSCBlockPath.get(scPos);
                    scBlockPath.setPos(pos);
                    scBlockPath.setDimension(world.getRegistryKey());
                }
            }
        }
        super.onPlaced(world, pos, state, placer, stack);
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        ItemStack stack = super.getPickStack(world, pos, state);
        try {
            world.getBlockEntity(pos, BlockEntities.SPACE_CUBE_BLOCK.getOrNull()).ifPresent(blockEntity -> blockEntity.setStackNbt(stack));
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
