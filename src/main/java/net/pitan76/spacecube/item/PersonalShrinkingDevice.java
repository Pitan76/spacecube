package net.pitan76.spacecube.item;

import ml.pkom.mcpitanlibarch.api.entity.Player;
import ml.pkom.mcpitanlibarch.api.event.item.ItemAppendTooltipEvent;
import ml.pkom.mcpitanlibarch.api.event.item.ItemUseEvent;
import ml.pkom.mcpitanlibarch.api.event.item.ItemUseOnBlockEvent;
import ml.pkom.mcpitanlibarch.api.item.CompatibleItemSettings;
import ml.pkom.mcpitanlibarch.api.item.ExtendItem;
import ml.pkom.mcpitanlibarch.api.util.TextUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.pitan76.spacecube.Blocks;
import net.pitan76.spacecube.SpaceCube;
import net.pitan76.spacecube.api.tunnel.def.ItemTunnel;
import net.pitan76.spacecube.api.util.SpaceCubeUtil;
import net.pitan76.spacecube.block.SpaceCubeBlock;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.api.data.SCBlockPath;
import net.pitan76.spacecube.api.util.CubeGenerator;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;
import net.pitan76.spacecube.world.SpaceCubeState;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PersonalShrinkingDevice extends ExtendItem {
    public PersonalShrinkingDevice(CompatibleItemSettings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> onRightClick(ItemUseEvent event) {
        Player player = event.getUser();
        World world = event.getWorld();

        // sneaking
        if (player.isSneaking()) return TypedActionResult.pass(event.stack);
        // Only run on the server side
        if (world.isClient()) return TypedActionResult.success(event.stack);



        // Process when the player's world is space cube dimension
        // プレイヤーのワールドがspace cube dimensionの場合の処理
        if (SpaceCube.SPACE_CUBE_DIMENSION_WORLD_KEY.equals(world.getRegistryKey())) {
            ActionResult result = tpPrevCubeOrWorld(world, player);
            SpaceCubeUtil.typedActionResult(result, event.stack);
        }

        return super.onRightClick(event);
    }

    @Override
    public ActionResult onRightClickOnBlock(ItemUseOnBlockEvent event) {
        Player player = event.getPlayer();
        World world = event.getWorld();
        BlockState state = world.getBlockState(event.getHit().getBlockPos());

        // sneaking
        //if (player.isSneaking()) return ActionResult.PASS;
        if (player.isSneaking()) {
            TunnelWallBlockEntity tunnelWallBlockEntity = (TunnelWallBlockEntity) world.getBlockEntity(event.getHit().getBlockPos());
            if (tunnelWallBlockEntity == null) {
                System.out.println("[SpaceCube] Error: tunnelWallBlockEntity is null.");
                return ActionResult.FAIL;
            }

            if (tunnelWallBlockEntity.getTunnelDef() instanceof ItemTunnel) {
                ItemTunnel tunnel = (ItemTunnel) tunnelWallBlockEntity.getTunnelDef();
                //System.out.println("importStack: " + tunnel.getImportStack());
                //System.out.println("exportStack: " + tunnel.getExportStack());
            }

            return ActionResult.PASS;
        }
        // Only run on the server side
        if (world.isClient()) return ActionResult.SUCCESS;

        // Process when SpaceCubeBlock (SpaceCubeBlockの場合の処理)
        if (state.getBlock() instanceof SpaceCubeBlock) {
            // set world of space cube dimension (space cube dimensionのワールドを代入)
            ServerWorld spaceCubeWorld = SpaceCubeUtil.getSpaceCubeWorld((ServerWorld) world);
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

            if (spaceCubeWorld.getRegistryKey() == world.getRegistryKey()) {
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
                    CubeGenerator.generateCube(spaceCubeWorld, scRoomPos, Blocks.SOLID_WALL, size);

                    spacePosWithSCBlockPath.put(scRoomPos, new SCBlockPath(event.getHit().getBlockPos(), world.getRegistryKey()));
                }


                // 座標をディメンションに保存する
                // Save coordinates to dimension
                spaceCubeState.addEntryPos(serverPlayer.getUuid(), serverPlayer.getBlockPos());

                // Teleport the player to the space cube (プレイヤーをspace cubeにテレポート)
                serverPlayer.teleport(scRoomPos.getX(), scRoomPos.getY(), scRoomPos.getZ());

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
                    CubeGenerator.generateCube(spaceCubeWorld, scRoomPos, Blocks.SOLID_WALL, size);

                    spacePosWithSCBlockPath.put(scRoomPos, new SCBlockPath(event.getHit().getBlockPos(), world.getRegistryKey()));
                }


                // 座標をディメンションに保存する
                // Save coordinates to dimension

                // 念のため
                spaceCubeState.removeEntryPosList(serverPlayer.getUuid());

                spaceCubeState.addEntryPos(serverPlayer.getUuid(), serverPlayer.getBlockPos(), world.getRegistryKey());

                spaceCubeState.markDirty();

                // Teleport the player to the space cube dimension (プレイヤーをspace cube dimensionにテレポート)
                serverPlayer.teleport(spaceCubeWorld, scRoomPos.getX(), scRoomPos.getY(), scRoomPos.getZ(), serverPlayer.getYaw(), serverPlayer.getPitch());
            }

            // Play the sound of dimension teleportation (ディメンション移動の音を鳴らす)
            player.playSound(SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 1.0F, 1.0F);

            return ActionResult.SUCCESS;
        }
        if (SpaceCube.SPACE_CUBE_DIMENSION_WORLD_KEY.equals(world.getRegistryKey())) {
            return tpPrevCubeOrWorld(world, player);
        }

        return ActionResult.PASS;
    }

    // Teleport to the previous cube(room) or world (前のキューブ(部屋)またはワールドにテレポート)
    public static ActionResult tpPrevCubeOrWorld(World playerWorld, Player player) {

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player.getEntity();
        UUID uuid = serverPlayer.getUuid();

        SpaceCubeState spaceCubeState = SpaceCubeState.getOrCreate(playerWorld.getServer());

        if (spaceCubeState.existPlayerData(uuid)) {
            if (spaceCubeState.hasEntryPos(uuid) && spaceCubeState.entryPosListSize(uuid) > 1) {
                // same dimension

                BlockPos entryPos = spaceCubeState.getLastEntryPosWithRemove(uuid);
                int x = entryPos.getX();
                int y = entryPos.getY();
                int z = entryPos.getZ();

                serverPlayer.teleport(x, y, z);

            } else {
                // another dimension

                // set world of return world (帰りのワールドを代入)
                RegistryKey<World> worldKey = spaceCubeState.getWorldKey(uuid);

                ServerWorld returnWorld = Objects.requireNonNull(playerWorld.getServer()).getWorld(RegistryKey.of(Registry.WORLD_KEY, worldKey.getValue()));
                if (returnWorld == null) {
                    System.out.println("[SpaceCube] Error: player's world is null.");
                    return ActionResult.PASS;
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
                    x = returnWorld.getSpawnPos().getX();
                    y = returnWorld.getSpawnPos().getY();
                    z = returnWorld.getSpawnPos().getZ();
                }
                // Remove entryPosList
                spaceCubeState.removeEntryPosList(uuid);

                // Teleport the player to the return world (プレイヤーをreturn worldにテレポート)
                serverPlayer.teleport(returnWorld, x, y, z, serverPlayer.getYaw(), serverPlayer.getPitch());
            }
            // Play the sound of dimension teleportation (ディメンション移動の音を鳴らす)
            player.playSound(SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return ActionResult.SUCCESS;
        } else {
            // データがないときの処理
            // Processing if no data
            BlockPos pos = SpaceCubeUtil.getNearestPos(spaceCubeState, serverPlayer.getBlockPos());
            if (pos != null) {
                Map<BlockPos, SCBlockPath> spacePosWithSCBlockPath = spaceCubeState.getSpacePosWithSCBlockPath();
                SCBlockPath scBlockPath = spacePosWithSCBlockPath.get(pos);

                ServerWorld returnWorld = Objects.requireNonNull(playerWorld.getServer()).getWorld(scBlockPath.getDimension());
                if (returnWorld == null) {
                    System.out.println("[SpaceCube] Error: player's world is null.");
                    return ActionResult.PASS;
                }

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
                    x = returnWorld.getSpawnPos().getX();
                    y = returnWorld.getSpawnPos().getY();
                    z = returnWorld.getSpawnPos().getZ();
                }
                serverPlayer.teleport(returnWorld, x, y, z, serverPlayer.getYaw(), serverPlayer.getPitch());

                // Play the sound of dimension teleportation (ディメンション移動の音を鳴らす)
                player.playSound(SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 1.0F, 1.0F);
                return ActionResult.SUCCESS;
            }

        }
        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemAppendTooltipEvent event) {
        super.appendTooltip(event);
        event.getTooltip().add(TextUtil.translatable("tooltip.spacecube.personal_shrinking_device"));
    }
}
