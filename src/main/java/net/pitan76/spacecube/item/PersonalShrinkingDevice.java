package net.pitan76.spacecube.item;

import net.pitan76.mcpitanlib.api.entity.Player;
import net.pitan76.mcpitanlib.api.event.item.ItemAppendTooltipEvent;
import net.pitan76.mcpitanlib.api.event.item.ItemUseEvent;
import net.pitan76.mcpitanlib.api.event.item.ItemUseOnBlockEvent;
import net.pitan76.mcpitanlib.api.item.v2.CompatibleItemSettings;
import net.pitan76.mcpitanlib.api.item.v2.CompatItem;
import net.pitan76.mcpitanlib.api.sound.CompatSoundCategory;
import net.pitan76.mcpitanlib.api.sound.CompatSoundEvents;
import net.pitan76.mcpitanlib.api.util.*;
import net.pitan76.mcpitanlib.api.util.entity.ServerPlayerUtil;
import net.pitan76.mcpitanlib.midohra.block.BlockState;
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
import java.util.UUID;

public class PersonalShrinkingDevice extends CompatItem {
    public PersonalShrinkingDevice(CompatibleItemSettings settings) {
        super(settings);
    }

    @Override
    public StackActionResult onRightClick(ItemUseEvent e) {
        Player player = e.getUser();
        World world = e.getMidohraWorld();

        // sneaking
        if (e.isSneaking()) return e.pass();
        // Only run on the server side
        if (e.isClient()) {

            // Play the sound of dimension teleportation (ディメンション移動の音を鳴らす)
            if (SpaceCube.SPACE_CUBE_DIMENSION_WORLD_KEY.equals(world.getId())) {
                player.playSound(CompatSoundEvents.BLOCK_PORTAL_TRAVEL, CompatSoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            return e.success();
        }

        // Process when the player's world is space cube dimension
        // プレイヤーのワールドがspace cube dimensionの場合の処理
        if (SpaceCube.SPACE_CUBE_DIMENSION_WORLD_KEY.equals(world.getId())) {
            CompatActionResult result = tpPrevCubeOrWorld(world, player);
            return StackActionResult.create(result, e.stack);
        }

        return super.onRightClick(e);
    }

    @Override
    public CompatActionResult onRightClickOnBlock(ItemUseOnBlockEvent e) {
        Player player = e.getPlayer();
        World world = e.getMidohraWorld();
        BlockState state = e.getMidohraState();

        // sneaking
        //if (player.isSneaking()) return ActionResult.PASS;
        if (player.isSneaking()) {

            /*
            TunnelWallBlockEntity tunnelWallBlockEntity = (TunnelWallBlockEntity) WorldUtil.getBlockEntity(world, event.getHit().getBlockPos());
            if (tunnelWallBlockEntity == null) {
                SpaceCube.INSTANCE.error("[SpaceCube] Error: tunnelWallBlockEntity is null.");
                return ActionResult.FAIL;
            }
            if (tunnelWallBlockEntity.getTunnelDef() instanceof ItemTunnel) {
                ItemTunnel tunnel = (ItemTunnel) tunnelWallBlockEntity.getTunnelDef();
                //System.out.println("importStack: " + tunnel.getImportStack());
                //System.out.println("exportStack: " + tunnel.getExportStack());
            }
             */

            return e.pass();
        }

        // Only run on the server side
        if (e.isClient()) {
            // Play the sound of dimension teleportation (ディメンション移動の音を鳴らす)
            if (state.getBlock().get() instanceof SpaceCubeBlock) {
                // TODO: entity.moveToWorld(serverWorld);
                player.playSound(CompatSoundEvents.BLOCK_PORTAL_TRAVEL, CompatSoundCategory.BLOCKS, 1.0F, 1.0F);
                return e.success();
            }

            if (SpaceCube.SPACE_CUBE_DIMENSION_WORLD_KEY.equals(world.getId())) {
                player.playSound(CompatSoundEvents.BLOCK_PORTAL_TRAVEL, CompatSoundCategory.BLOCKS, 1.0F, 1.0F);
                return e.success();
            }

            return e.success();
        }

        // Process when SpaceCubeBlock (SpaceCubeBlockの場合の処理)
        if (state.getBlock().get() instanceof SpaceCubeBlock) {
            // set world of space cube dimension (space cube dimensionのワールドを代入)
            ServerWorld spaceCubeWorld = SpaceCubeUtil.getSpaceCubeWorld(world.toServerWorld().get());
            if (spaceCubeWorld == null) {
                SpaceCube.INSTANCE.error("[SpaceCube] Error: spaceCubeWorld is null.");
                return e.fail();
            }

//            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player.getEntity();

            MCServer server = spaceCubeWorld.getMCServer();
            SpaceCubeState spaceCubeState = SpaceCubeState.getOrCreate(server);

            int size = ((SpaceCubeBlock) state.getBlock().get()).getSize();

            // Map of SpaceCube (room) coordinates in SpaceCubeDimension and Space Cube Block coordinates
            Map<BlockPos, SCBlockPath> spacePosWithSCBlockPath = spaceCubeState.getSpacePosWithSCBlockPath();

            SpaceCubeBlockEntity spaceCubeBlockEntity = (SpaceCubeBlockEntity) e.getBlockEntity();
            if (spaceCubeBlockEntity == null) {
                SpaceCube.INSTANCE.error("[SpaceCube] Error: spaceCubeBlockEntity is null.");
                return e.fail();
            }

            if (spaceCubeWorld.equals(world)) {
                // same dimension

                // scRoomPos = Space Cube Position in Space Cube Dimension (Space Cube Dimension内のスペースキューブの位置)

                BlockPos scRoomPos;
                if (!spaceCubeBlockEntity.isScRoomPosNull())
                    scRoomPos = spaceCubeBlockEntity.getScRoomPos();
                else {
                    // 新規作成のための処理 (if内) Processing for new creation (if)

                    scRoomPos = SpaceCubeUtil.getNewPos(spaceCubeState);
                    spaceCubeBlockEntity.setScRoomPos(scRoomPos);

                    // Generate a hollow cube with Solid Space Cube Wall (Solid Space Cube Wallで空洞のキューブを生成)
                    CubeGenerator.generateCube(spaceCubeWorld, scRoomPos, Blocks.SOLID_WALL.getWrapper(), size);

                    spacePosWithSCBlockPath.put(scRoomPos, new SCBlockPath(e.getMidohraPos(), world.getId()));

                    // Chunk Loader
                    spaceCubeBlockEntity.addTicket();

                }


                // 座標をディメンションに保存する
                // Save coordinates to dimension
                spaceCubeState.addEntryPos(player.getUUID(), player.getBlockPosM());

                // Teleport the player to the space cube (プレイヤーをspace cubeにテレポート)
                player.teleport(scRoomPos.getX(), scRoomPos.getY(), scRoomPos.getZ());

            } else {
                // another dimension

                BlockPos scRoomPos;
                if (!spaceCubeBlockEntity.isScRoomPosNull())
                    scRoomPos = spaceCubeBlockEntity.getScRoomPos();
                else {
                    // 新規作成のための処理 (if内) Processing for new creation (if)

                    scRoomPos = SpaceCubeUtil.getNewPos(spaceCubeState);
                    spaceCubeBlockEntity.setScRoomPos(scRoomPos);

                    // Generate a hollow cube with Solid Space Cube Wall (Solid Space Cube Wallで空洞のキューブを生成)
                    CubeGenerator.generateCube(spaceCubeWorld, scRoomPos, Blocks.SOLID_WALL.getWrapper(), size);

                    spacePosWithSCBlockPath.put(scRoomPos, new SCBlockPath(e.getMidohraPos(), world.getId()));

                    // Chunk Loader
                    spaceCubeBlockEntity.addTicket();
                }


                // 座標をディメンションに保存する
                // Save coordinates to dimension

                // 念のため
                spaceCubeState.removeEntryPosList(player.getUUID());

                spaceCubeState.addEntryPos(player.getUUID(), player.getBlockPosM(), world.getId());

                PersistentStateUtil.markDirty(spaceCubeState);

                // Teleport the player to the space cube dimension (プレイヤーをspace cube dimensionにテレポート)
                ServerPlayerUtil.teleport(player, spaceCubeWorld, scRoomPos.getX(), scRoomPos.getY(), scRoomPos.getZ(), player.getYaw(), player.getPitch());
            }

            // Play the sound of dimension teleportation (ディメンション移動の音を鳴らす)
            // ServerPlayerUtil.playSound(serverPlayer, CompatSoundEvents.BLOCK_PORTAL_TRAVEL, CompatSoundCategory.PLAYERS, 1.0F, 1.0F);

            return e.success();
        }
        if (SpaceCube.SPACE_CUBE_DIMENSION_WORLD_KEY.equals(world.getId())) {
            return tpPrevCubeOrWorld(world, player);
        }

        return e.pass();
    }

    // Teleport to the previous cube(room) or world (前のキューブ(部屋)またはワールドにテレポート)
    public static CompatActionResult tpPrevCubeOrWorld(World playerWorld, Player player) {
//        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player.getEntity();
        UUID uuid = player.getUUID();

        MCServer server = playerWorld.getMCServer();
        SpaceCubeState spaceCubeState = SpaceCubeState.getOrCreate(server);

        if (spaceCubeState.existPlayerData(uuid)) {
            if (spaceCubeState.hasEntryPos(uuid) && spaceCubeState.entryPosListSize(uuid) > 1) {
                // same dimension

                BlockPos entryPos = spaceCubeState.getLastEntryPosWithRemove(uuid);
                int x = entryPos.getX();
                int y = entryPos.getY();
                int z = entryPos.getZ();

                ServerPlayerUtil.teleport(player, x, y, z);

            } else {
                // another dimension

                // set world of return world (帰りのワールドを代入)
                CompatIdentifier worldId = spaceCubeState.getWorldId(uuid);

                Optional<ServerWorld> returnWorldOptional = playerWorld.getServerWorld(worldId);
                if (!returnWorldOptional.isPresent()) {
                    SpaceCube.INSTANCE.error("[SpaceCube] Error: player's world is null.");
                    return CompatActionResult.PASS;
                }
                ServerWorld returnWorld = returnWorldOptional.get();

                int x, y, z;
                if (spaceCubeState.hasEntryPos(uuid)) {
                    // entryPosListがある場合は、その最後の座標を取得 (普通はこっち)
                    // If entryPosList exists, get the last coordinate (usually this one)
                    BlockPos entryPos = spaceCubeState.getLastEntryPosWithRemove(uuid);
                    x = entryPos.getX();
                    y = entryPos.getY();
                    z = entryPos.getZ();
                } else {
                    // 未知のバグが発生した場合は、スポーン地点を取得 (普通は発生しない)
                    // If an unknown bug occurs, get the spawn point (usually doesn't happen)
                    BlockPos spawnPos = returnWorld.getSpawnPos();
                    x = spawnPos.getX();
                    y = spawnPos.getY();
                    z = spawnPos.getZ();
                }
                // Remove entryPosList
                spaceCubeState.removeEntryPosList(uuid);

                // Teleport the player to the return world (プレイヤーをreturn worldにテレポート)
                ServerPlayerUtil.teleport(player, returnWorld, x, y, z, player.getYaw(), player.getPitch());
            }
            // Play the sound of dimension teleportation (ディメンション移動の音を鳴らす)
            // player.playSound(CompatSoundEvents.BLOCK_PORTAL_TRAVEL, CompatSoundCategory.PLAYERS, 1.0F, 1.0F);
            return CompatActionResult.SUCCESS;
        } else {
            // データがないときの処理
            // Processing if no data
            BlockPos pos = SpaceCubeUtil.getNearestPos(spaceCubeState, player.getBlockPosM());
            if (pos != null) {
                Map<BlockPos, SCBlockPath> spacePosWithSCBlockPath = spaceCubeState.getSpacePosWithSCBlockPath();
                SCBlockPath scBlockPath = spacePosWithSCBlockPath.get(pos);
                Optional<ServerWorld> returnWorldOptional = playerWorld.getServerWorld(scBlockPath.getDimension());

                if (!returnWorldOptional.isPresent()) {
                    SpaceCube.INSTANCE.error("[SpaceCube] Error: player's world is null.");
                    return CompatActionResult.PASS;
                }
                ServerWorld returnWorld = returnWorldOptional.get();

                int x, y, z;
                if (scBlockPath.pos != null) {
                    // entryPosListがある場合は、その最後の座標を取得 (普通はこっち)
                    // If entryPosList exists, get the last coordinate (usually this one)
                    BlockPos entryPos = scBlockPath.getPos();
                    x = entryPos.getX();
                    y = entryPos.getY();
                    z = entryPos.getZ();
                } else {
                    // 未知のバグが発生した場合は、スポーン地点を取得 (普通は発生しない)
                    // If an unknown bug occurs, get the spawn point (usually doesn't happen)
                    BlockPos spawnPos = returnWorld.getSpawnPos();
                    x = spawnPos.getX();
                    y = spawnPos.getY();
                    z = spawnPos.getZ();
                }
                ServerPlayerUtil.teleport(player, returnWorld, x, y, z, player.getYaw(), player.getPitch());

                // Play the sound of dimension teleportation (ディメンション移動の音を鳴らす)
                // player.playSound(CompatSoundEvents.BLOCK_PORTAL_TRAVEL, CompatSoundCategory.PLAYERS, 1.0F, 1.0F);
                return CompatActionResult.SUCCESS;
            }

        }
        return CompatActionResult.PASS;
    }

    public static CompatActionResult tpPrevCubeOrWorld(net.minecraft.world.World world, Player player) {
        return tpPrevCubeOrWorld(World.of(world), player);
    }

    @Override
    public void appendTooltip(ItemAppendTooltipEvent e) {
        super.appendTooltip(e);
        e.addTooltip(TextUtil.translatable("tooltip.spacecube.personal_shrinking_device"));
    }
}
