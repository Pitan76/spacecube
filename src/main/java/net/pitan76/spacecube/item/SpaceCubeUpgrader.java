package net.pitan76.spacecube.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
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
import net.pitan76.mcpitanlib.api.item.v2.CompatibleItemSettings;
import net.pitan76.mcpitanlib.api.item.ExtendItem;
import net.pitan76.mcpitanlib.api.util.*;
import net.pitan76.spacecube.Blocks;
import net.pitan76.spacecube.SpaceCube;
import net.pitan76.spacecube.api.data.SCBlockPath;
import net.pitan76.spacecube.api.util.CubeGenerator;
import net.pitan76.spacecube.api.util.SpaceCubeUtil;
import net.pitan76.spacecube.block.SpaceCubeBlock;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.world.SpaceCubeState;

import java.util.Map;
import java.util.Optional;

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
    public CompatActionResult onRightClickOnBlock(ItemUseOnBlockEvent e) {
        World world = e.getWorld();
        BlockPos pos = e.getBlockPos();
        BlockState state = e.getBlockState();
        Player player = e.getPlayer();

        if (state.getBlock() instanceof SpaceCubeBlock) {
            // sneaking
            if (player.isSneaking()) return e.pass();
            // Only run on the server side
            if (e.isClient()) return e.success();

            CompatActionResult result = upgradeSpaceCube(world, pos, state, e.getStack());
            if (result == CompatActionResult.CONSUME) {
                player.sendMessage(TextUtil.literal("[SpaceCube] Upgraded!"));
            }
            return result;
        }

        return e.pass();
    }

    @Override
    public StackActionResult onRightClick(ItemUseEvent e) {
        World world = e.getWorld();
        Player player = e.getUser();
        ItemStack stack = e.getStack();

        // sneaking
        if (e.isSneaking()) return e.pass();
        // Only run on the server side
        if (e.isClient()) return e.success();

        ServerWorld spaceCubeWorld = SpaceCubeUtil.getSpaceCubeWorld((ServerWorld) world);
        if (spaceCubeWorld != null && WorldUtil.equals(world, spaceCubeWorld)) {
            BlockPos spacePos = SpaceCubeUtil.getNearestPos((ServerWorld) world, player.getBlockPos());
            if (spacePos == null) return e.fail();

            Optional<MinecraftServer> optionalServer = WorldUtil.getServer(world);
            SpaceCubeState spaceCubeState = SpaceCubeState.getOrCreate(optionalServer.get());
            Map<BlockPos, SCBlockPath> spacePosWithSCBlockPath =  spaceCubeState.getSpacePosWithSCBlockPath();
            if (!spacePosWithSCBlockPath.containsKey(spacePos)) return e.fail();

            SCBlockPath scBlockPath = spacePosWithSCBlockPath.get(spacePos);
            BlockPos placedPos = scBlockPath.getPos();

            Optional<ServerWorld> optionalPlacedWorld = WorldUtil.getWorld(world, scBlockPath.getDimension());
            if (!optionalPlacedWorld.isPresent()) return e.fail();
            World placedWorld = optionalPlacedWorld.get();

            BlockState state = WorldUtil.getBlockState(placedWorld, placedPos);
            if (state.getBlock() instanceof SpaceCubeBlock) {
                CompatActionResult result = upgradeSpaceCube(placedWorld, placedPos, state, stack);
                if (result == CompatActionResult.CONSUME)
                    player.sendMessage(TextUtil.literal("[SpaceCube] Upgraded!"));

                return StackActionResult.create(result, e.stack);
            }
        }

        return super.onRightClick(e);
    }

    public CompatActionResult upgradeSpaceCube(World world, BlockPos pos, BlockState state, ItemStack stack) {
        SpaceCubeBlock spaceCubeBlock = (SpaceCubeBlock) state.getBlock();
        if (spaceCubeBlock.getSize() < size) {
            NbtCompound nbt = NbtUtil.create();

            BlockEntity blockEntity = WorldUtil.getBlockEntity(world, pos);
            if (blockEntity instanceof SpaceCubeBlockEntity) {
                SpaceCubeBlockEntity scBlockEntity = (SpaceCubeBlockEntity) blockEntity;
                scBlockEntity.writeNbt(new WriteNbtArgs(nbt));
            }

            BlockState newState = SpaceCubeBlock.getSpaceCubeBlockFromSize(size).getNewDefaultState();
            WorldUtil.setBlockState(world, pos, newState);

            BlockEntity newBlockEntity = WorldUtil.getBlockEntity(world, pos);
            if (newBlockEntity instanceof SpaceCubeBlockEntity && !nbt.isEmpty()) {
                SpaceCubeBlockEntity scBlockEntity = (SpaceCubeBlockEntity) newBlockEntity;
                scBlockEntity.readNbt(new ReadNbtArgs(nbt));
                if (scBlockEntity.scRoomPos != null) {
                    ServerWorld spaceCubeWorld = SpaceCubeUtil.getSpaceCubeWorld((ServerWorld) world);
                    if (spaceCubeWorld == null) {
                        SpaceCube.INSTANCE.error("[SpaceCube] Error: spaceCubeWorld is null.");
                        return CompatActionResult.FAIL;
                    }

                    CubeGenerator.generateCube(spaceCubeWorld, scBlockEntity.scRoomPos, net.minecraft.block.Blocks.AIR, spaceCubeBlock.getSize());
                    CubeGenerator.generateCube(spaceCubeWorld, scBlockEntity.scRoomPos, Blocks.SOLID_WALL, size);
                }
            }

            ItemStackUtil.decrementCount(stack, 1);
            return CompatActionResult.CONSUME;
        }
        return CompatActionResult.PASS;
    }
}
