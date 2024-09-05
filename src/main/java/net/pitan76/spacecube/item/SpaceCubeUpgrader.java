package net.pitan76.spacecube.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pitan76.mcpitanlib.api.entity.Player;
import net.pitan76.mcpitanlib.api.event.item.ItemUseEvent;
import net.pitan76.mcpitanlib.api.event.item.ItemUseOnBlockEvent;
import net.pitan76.mcpitanlib.api.event.nbt.ReadNbtArgs;
import net.pitan76.mcpitanlib.api.event.nbt.WriteNbtArgs;
import net.pitan76.mcpitanlib.api.item.CompatibleItemSettings;
import net.pitan76.mcpitanlib.api.item.ExtendItem;
import net.pitan76.mcpitanlib.api.util.ActionResultUtil;
import net.pitan76.mcpitanlib.api.util.NbtUtil;
import net.pitan76.mcpitanlib.api.util.TextUtil;
import net.pitan76.mcpitanlib.api.util.WorldUtil;
import net.pitan76.spacecube.Blocks;
import net.pitan76.spacecube.api.data.SCBlockPath;
import net.pitan76.spacecube.api.util.CubeGenerator;
import net.pitan76.spacecube.api.util.SpaceCubeUtil;
import net.pitan76.spacecube.block.SpaceCubeBlock;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.world.SpaceCubeState;

import java.util.Map;

public class SpaceCubeUpgrader extends ExtendItem {
    public final int size;

    public SpaceCubeUpgrader(CompatibleItemSettings settings, int size) {
        super(settings);
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    @Override
    public ActionResult onRightClickOnBlock(ItemUseOnBlockEvent event) {
        World world = event.getWorld();
        BlockPos pos = event.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Player player = event.getPlayer();

        if (state.getBlock() instanceof SpaceCubeBlock) {
            // sneaking
            if (player.isSneaking()) return ActionResult.PASS;
            // Only run on the server side
            if (world.isClient()) return ActionResult.SUCCESS;

            ActionResult result = upgradeSpaceCube(world, pos, state, event.getStack());
            if (result == ActionResult.CONSUME) {
                player.sendMessage(TextUtil.literal("[SpaceCube] Upgraded!"));
            }
            return result;
        }

        return ActionResult.PASS;
    }

    @Override
    public TypedActionResult<ItemStack> onRightClick(ItemUseEvent event) {
        World world = event.getWorld();
        Player player = event.getUser();

        // sneaking
        if (player.isSneaking()) return TypedActionResult.pass(event.stack);
        // Only run on the server side
        if (world.isClient()) return TypedActionResult.success(event.stack);

        if (WorldUtil.equals(world, SpaceCubeUtil.getSpaceCubeWorld((ServerWorld) world))) {
            BlockPos spacePos = SpaceCubeUtil.getNearestPos((ServerWorld) world, player.getBlockPos());
            if (spacePos == null)
                return TypedActionResult.fail(event.stack);

            SpaceCubeState spaceCubeState = SpaceCubeState.getOrCreate(world.getServer());
            Map<BlockPos, SCBlockPath> spacePosWithSCBlockPath =  spaceCubeState.getSpacePosWithSCBlockPath();
            if (!spacePosWithSCBlockPath.containsKey(spacePos))
                return TypedActionResult.fail(event.stack);
            SCBlockPath scBlockPath = spacePosWithSCBlockPath.get(spacePos);

            BlockPos placedPos = scBlockPath.getPos();
            World placedWorld = WorldUtil.getWorld(world, scBlockPath.getDimension().toMinecraft());
            BlockState state = placedWorld.getBlockState(placedPos);
            if (state.getBlock() instanceof SpaceCubeBlock) {
                ActionResult result = upgradeSpaceCube(placedWorld, placedPos, state, event.stack);
                if (result == ActionResult.CONSUME) {
                    player.sendMessage(TextUtil.literal("[SpaceCube] Upgraded!"));
                }
                return ActionResultUtil.typedActionResult(result, event.stack);
            }
        }

        return super.onRightClick(event);
    }

    public ActionResult upgradeSpaceCube(World world, BlockPos pos, BlockState state, ItemStack stack) {
        SpaceCubeBlock spaceCubeBlock = (SpaceCubeBlock) state.getBlock();
        if (spaceCubeBlock.getSize() < size) {
            NbtCompound nbt = NbtUtil.create();

            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SpaceCubeBlockEntity) {
                SpaceCubeBlockEntity scBlockEntity = (SpaceCubeBlockEntity) blockEntity;
                scBlockEntity.writeNbt(new WriteNbtArgs(nbt));
            }

            BlockState newState = SpaceCubeBlock.getSpaceCubeBlockFromSize(size).getDefaultState();
            world.setBlockState(pos, newState);

            BlockEntity newBlockEntity = world.getBlockEntity(pos);
            if (newBlockEntity instanceof SpaceCubeBlockEntity && !nbt.isEmpty()) {
                SpaceCubeBlockEntity scBlockEntity = (SpaceCubeBlockEntity) newBlockEntity;
                scBlockEntity.readNbt(new ReadNbtArgs(nbt));
                if (scBlockEntity.scRoomPos != null) {
                    ServerWorld spaceCubeWorld = SpaceCubeUtil.getSpaceCubeWorld((ServerWorld) world);
                    if (spaceCubeWorld == null) {
                        System.out.println("[SpaceCube] Error: spaceCubeWorld is null.");
                        return ActionResult.FAIL;
                    }

                    CubeGenerator.generateCube(spaceCubeWorld, scBlockEntity.scRoomPos, net.minecraft.block.Blocks.AIR, spaceCubeBlock.getSize());
                    CubeGenerator.generateCube(spaceCubeWorld, scBlockEntity.scRoomPos, Blocks.SOLID_WALL, size);
                }
            }


            stack.decrement(1);
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }
}
