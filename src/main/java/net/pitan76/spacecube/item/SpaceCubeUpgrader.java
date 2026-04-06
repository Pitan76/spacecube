package net.pitan76.spacecube.item;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.pitan76.mcpitanlib.api.entity.Player;
import net.pitan76.mcpitanlib.api.event.item.ItemUseEvent;
import net.pitan76.mcpitanlib.api.event.item.ItemUseOnBlockEvent;
import net.pitan76.mcpitanlib.api.event.nbt.ReadNbtArgs;
import net.pitan76.mcpitanlib.api.event.nbt.WriteNbtArgs;
import net.pitan76.mcpitanlib.api.item.v2.CompatItem;
import net.pitan76.mcpitanlib.api.item.v2.CompatibleItemSettings;
import net.pitan76.mcpitanlib.api.util.*;
import net.pitan76.mcpitanlib.midohra.block.BlockState;
import net.pitan76.mcpitanlib.midohra.block.MCBlocks;
import net.pitan76.mcpitanlib.midohra.server.MCServer;
import net.pitan76.mcpitanlib.midohra.util.math.BlockPos;
import net.pitan76.mcpitanlib.midohra.world.ServerWorld;
import net.pitan76.mcpitanlib.midohra.world.World;
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

public class SpaceCubeUpgrader extends CompatItem {
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
        World world = e.getMidohraWorld();
        BlockPos pos = e.getMidohraPos();
        BlockState state = e.getMidohraState();
        Player player = e.getPlayer();

        if (e.getBlockWrapper().get() instanceof SpaceCubeBlock) {
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
        World world = e.getMidohraWorld();
        Player player = e.getUser();
        ItemStack stack = e.getStack();

        // sneaking
        if (e.isSneaking()) return e.pass();
        // Only run on the server side
        if (e.isClient()) return e.success();

        ServerWorld serverWorld = world.toServerWorld().get();

        ServerWorld spaceCubeWorld = SpaceCubeUtil.getSpaceCubeWorld(serverWorld);
        if (world.equals(spaceCubeWorld)) {
            BlockPos spacePos = SpaceCubeUtil.getNearestPos(serverWorld, player.getBlockPosM());
            if (spacePos == null) return e.fail();

            MCServer server = serverWorld.getMCServer();
            SpaceCubeState spaceCubeState = SpaceCubeState.getOrCreate(server);
            Map<BlockPos, SCBlockPath> spacePosWithSCBlockPath =  spaceCubeState.getSpacePosWithSCBlockPath();
            if (!spacePosWithSCBlockPath.containsKey(spacePos)) return e.fail();

            SCBlockPath scBlockPath = spacePosWithSCBlockPath.get(spacePos);
            BlockPos placedPos = scBlockPath.getPos();

            Optional<ServerWorld> optionalPlacedWorld = world.getServerWorld(scBlockPath.getDimension());
            if (!optionalPlacedWorld.isPresent()) return e.fail();
            World placedWorld = optionalPlacedWorld.get();

            BlockState state = placedWorld.getBlockState(placedPos);
            if (state.getBlock().get() instanceof SpaceCubeBlock) {
                CompatActionResult result = upgradeSpaceCube(placedWorld, placedPos, state, stack);
                if (result == CompatActionResult.CONSUME)
                    player.sendMessage(TextUtil.literal("[SpaceCube] Upgraded!"));

                return StackActionResult.create(result, e.stack);
            }
        }

        return super.onRightClick(e);
    }

    public CompatActionResult upgradeSpaceCube(World world, BlockPos pos, BlockState state, ItemStack stack) {
        SpaceCubeBlock spaceCubeBlock = (SpaceCubeBlock) state.getBlock().get();
        if (spaceCubeBlock.getSize() < size) {
            NbtCompound nbt = NbtUtil.create();

            BlockEntity blockEntity = world.getBlockEntity(pos).get();
            if (blockEntity instanceof SpaceCubeBlockEntity) {
                SpaceCubeBlockEntity scBlockEntity = (SpaceCubeBlockEntity) blockEntity;
                scBlockEntity.writeNbt(new WriteNbtArgs(nbt));
            }

            BlockState newState = SpaceCubeBlock.getSpaceCubeBlockFromSize(size).getDefaultMidohraState();
            world.setBlockState(pos, newState);

            BlockEntity newBlockEntity = world.getBlockEntity(pos).get();
            if (newBlockEntity instanceof SpaceCubeBlockEntity && !nbt.isEmpty()) {
                SpaceCubeBlockEntity scBlockEntity = (SpaceCubeBlockEntity) newBlockEntity;
                scBlockEntity.readNbt(new ReadNbtArgs(nbt));
                if (scBlockEntity.scRoomPos != null) {
                    ServerWorld spaceCubeWorld = SpaceCubeUtil.getSpaceCubeWorld(world.toServerWorld().get());
                    if (spaceCubeWorld == null) {
                        SpaceCube.INSTANCE.error("[SpaceCube] Error: spaceCubeWorld is null.");
                        return CompatActionResult.FAIL;
                    }

                    CubeGenerator.generateCube(spaceCubeWorld, scBlockEntity.scRoomPos, MCBlocks.AIR, spaceCubeBlock.getSize());
                    CubeGenerator.generateCube(spaceCubeWorld, scBlockEntity.scRoomPos, Blocks.SOLID_WALL.getWrapper(), size);
                }
            }

            ItemStackUtil.decrementCount(stack, 1);
            return CompatActionResult.CONSUME;
        }
        return CompatActionResult.PASS;
    }
}
