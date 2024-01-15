package net.pitan76.spacecube.item;

import ml.pkom.mcpitanlibarch.api.entity.Player;
import ml.pkom.mcpitanlibarch.api.event.item.ItemUseOnBlockEvent;
import ml.pkom.mcpitanlibarch.api.item.CompatibleItemSettings;
import ml.pkom.mcpitanlibarch.api.item.ExtendItem;
import ml.pkom.mcpitanlibarch.api.util.TextUtil;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.pitan76.spacecube.Blocks;
import net.pitan76.spacecube.SpaceCube;
import net.pitan76.spacecube.api.SpaceCubeUtil;
import net.pitan76.spacecube.block.SpaceCubeBlock;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.data.SCBlockPath;
import net.pitan76.spacecube.util.CubeGenerator;
import net.pitan76.spacecube.world.SpaceCubeState;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PersonalShrinkingDevice extends ExtendItem {
    public PersonalShrinkingDevice(CompatibleItemSettings settings) {
        super(settings);
    }

    @Override
    public ActionResult onRightClickOnBlock(ItemUseOnBlockEvent event) {
        Player player = event.getPlayer();
        World world = event.getWorld();
        BlockState state = world.getBlockState(event.getHit().getBlockPos());

        // If the player is sneaking, pass the event
        // プレイヤーがスニークしている場合はイベントをパス
        if (player.isSneaking()) return ActionResult.PASS;
        // Only run on the server side (サーバー側のみで実行)
        if (world.isClient()) return ActionResult.SUCCESS;

        // Process when SpaceCubeBlock (SpaceCubeBlockの場合の処理)
        if (state.getBlock() instanceof SpaceCubeBlock) {
            // set world of space cube dimension (space cube dimensionのワールドを代入)
            ServerWorld spaceCubeWorld = Objects.requireNonNull(world.getServer()).getWorld(SpaceCube.SPACE_CUBE_DIMENSION_WORLD_KEY);
            if (spaceCubeWorld == null) {
                System.out.println("[SpaceCube] Error: spaceCubeWorld is null.");
                return ActionResult.FAIL;
            }

            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player.getEntity();
            SpaceCubeState spaceCubeState = SpaceCubeState.getOrCreate(spaceCubeWorld.getServer());

            int size = ((SpaceCubeBlock) state.getBlock()).getSize();

            // Map of SpaceCube (room) coordinates in SpaceCubeDimension and Space Cube Block coordinates
            Map<BlockPos, SCBlockPath> spacePosWithSCBlockPath = spaceCubeState.getSpacePosWithSCBlockPath();

            SpaceCubeBlockEntity spaceCubeBlockEntity = (SpaceCubeBlockEntity) world.getBlockEntity(event.getHit().getBlockPos());
            if (spaceCubeBlockEntity == null) {
                System.out.println("[SpaceCube] Error: spaceCubeBlockEntity is null.");
                return ActionResult.FAIL;
            }

            /*
            if (true) {
                if (spaceCubeBlockEntity.isScPosNull()) {
                    player.sendMessage(TextUtil.literal("null"));
                } else {
                    player.sendMessage(TextUtil.literal("scPos: " + spaceCubeBlockEntity.getScPos()));
                }
                return ActionResult.FAIL;
            }
             */

            if (spaceCubeWorld.getRegistryKey() == world.getRegistryKey()) {
                // same dimension

                // scPos = Space Cube Position in Space Cube Dimension (Space Cube Dimension内のスペースキューブの位置)

                BlockPos scPos;
                if (!spaceCubeBlockEntity.isScPosNull())
                    scPos = spaceCubeBlockEntity.getScPos();
                else {
                    // 新規作成のための処理 (if内) Processing for new creation (if)

                    scPos = SpaceCubeUtil.getNewPos(spaceCubeState);
                    spaceCubeBlockEntity.setScPos(scPos);

                    // Generate a hollow cube with Solid Space Cube Wall (Solid Space Cube Wallで空洞のキューブを生成)
                    CubeGenerator.generateCube(spaceCubeWorld, scPos, Blocks.SOLID_WALL, size);
                }
                spacePosWithSCBlockPath.put(scPos, new SCBlockPath(event.getHit().getBlockPos(), world.getRegistryKey()));


                // 座標をディメンションに保存する
                // Save coordinates to dimension
                spaceCubeState.addEntryPos(serverPlayer.getUuid(), serverPlayer.getBlockPos());

                // Teleport the player to the space cube (プレイヤーをspace cubeにテレポート)
                serverPlayer.teleport(scPos.getX(), scPos.getY(), scPos.getZ());

            } else {
                // another dimension

                BlockPos scPos;
                if (!spaceCubeBlockEntity.isScPosNull())
                    scPos = spaceCubeBlockEntity.getScPos();
                else {
                    // 新規作成のための処理 (if内) Processing for new creation (if)

                    scPos = SpaceCubeUtil.getNewPos(spaceCubeState);
                    spaceCubeBlockEntity.setScPos(scPos);

                    // Generate a hollow cube with Solid Space Cube Wall (Solid Space Cube Wallで空洞のキューブを生成)
                    CubeGenerator.generateCube(spaceCubeWorld, scPos, Blocks.SOLID_WALL, size);
                }


                // 座標をディメンションに保存する
                // Save coordinates to dimension
                spacePosWithSCBlockPath.put(scPos, new SCBlockPath(event.getHit().getBlockPos(), world.getRegistryKey()));

                // 念のため
                spaceCubeState.removeEntryPosList(serverPlayer.getUuid());

                spaceCubeState.addEntryPos(serverPlayer.getUuid(), serverPlayer.getBlockPos(), world.getRegistryKey());

                spaceCubeState.markDirty();

                // Teleport the player to the space cube dimension (プレイヤーをspace cube dimensionにテレポート)
                serverPlayer.teleport(spaceCubeWorld, scPos.getX(), scPos.getY(), scPos.getZ(), serverPlayer.getYaw(), serverPlayer.getPitch());
            }

            // Play the sound of dimension teleportation (ディメンション移動の音を鳴らす)
            player.playSound(SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 1.0F, 1.0F);

        } else {
            // Process when the player's world is space cube dimension
            // プレイヤーのワールドがspace cube dimensionの場合の処理
            if (SpaceCube.SPACE_CUBE_DIMENSION_WORLD_KEY.equals(world.getRegistryKey())) {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player.getEntity();
                UUID uuid = serverPlayer.getUuid();

                SpaceCubeState spaceCubeState = SpaceCubeState.getOrCreate(world.getServer());

                if (spaceCubeState.hasEntryPos(uuid) && spaceCubeState.entryPosListSize(uuid) > 1) {
                    // 同ディメンション内での移動の場合

                    BlockPos entryPos = spaceCubeState.getLastEntryPosWithRemove(uuid);
                    int x = entryPos.getX();
                    int y = entryPos.getY();
                    int z = entryPos.getZ();

                    serverPlayer.teleport(x, y, z);

                } else {
                    // ディメンション間での移動の場合

                    // set world of return world (帰りのワールドを代入)
                    RegistryKey<World> worldKey = spaceCubeState.getWorldKey(uuid);

                    ServerWorld returnWorld = Objects.requireNonNull(world.getServer()).getWorld(RegistryKey.of(Registry.WORLD_KEY, worldKey.getValue()));
                    if (returnWorld == null) {
                        System.out.println("[SpaceCube] Error: overworld is null.");
                        return ActionResult.FAIL;
                    }

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
                        // ないと思うけど、念のため (I don't think so, but just in case)
                        x = returnWorld.getSpawnPos().getX();
                        y = returnWorld.getSpawnPos().getY();
                        z = returnWorld.getSpawnPos().getZ();
                    }
                    // Remove the entryPosList (entryPosListを削除)
                    spaceCubeState.removeEntryPosList(uuid);

                    // Teleport the player to the overworld (プレイヤーをoverworldにテレポート)
                    serverPlayer.teleport(returnWorld, x, y, z, serverPlayer.getYaw(), serverPlayer.getPitch());
                }
                // Play the sound of dimension teleportation (ディメンション移動の音を鳴らす)
                player.playSound(SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 1.0F, 1.0F);


            }
        }

        return ActionResult.SUCCESS;
    }
}
