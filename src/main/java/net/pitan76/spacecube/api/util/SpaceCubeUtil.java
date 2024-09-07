package net.pitan76.spacecube.api.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pitan76.mcpitanlib.api.util.ActionResultUtil;
import net.pitan76.mcpitanlib.api.util.WorldUtil;
import net.pitan76.mcpitanlib.api.util.math.PosUtil;
import net.pitan76.spacecube.SpaceCube;
import net.pitan76.spacecube.world.SpaceCubeState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SpaceCubeUtil {

    // 新規のスペースキューブ(部屋)の座標を取得する
    public static BlockPos getNewPos(SpaceCubeState state) {
        // スペースキューブの数を取得する
        int scCount = state.getSpacePosWithSCBlockPath().size() + 1;

        // 4個ごとに座標を変える
        int mod = Math.floorMod(scCount, 4);
        int div = Math.floorDiv(scCount, 4);
        //     □
        //     □
        // □ □ ○ □ □
        //     □
        //     □
        // ○ が 0, 64, 0 として考えて周辺にキューブを設置していくよ
        // ○ is considered to be 0, 64, 0 and cubes are placed around it

        switch (mod) {
            case 0:
                return PosUtil.flooredBlockPos(div * 1024, 64, div * 1024);
            case 1:
                return PosUtil.flooredBlockPos(div * -1024, 64, div * 1024);
            case 2:
                return PosUtil.flooredBlockPos(div * 1024 + 1024, 64, div * 1024);
            case 3:
                return PosUtil.flooredBlockPos(div * 1024, 64, div * 1024 + 1024);

            // まぁたぶんないけど念のために
            default:
                return PosUtil.flooredBlockPos(0, 64, 0);
        }
    }

    // 最も近いスペースキューブの座標を取得する (スペースキューブがない場合は null を返す)
    // Get the coordinates of the nearest space cube (returns null if there is no space cube)
    @Nullable
    public static BlockPos getNearestPos(SpaceCubeState state, BlockPos pos) {
        BlockPos nearestPos = null;
        double nearestDistance = Double.MAX_VALUE;

        for (BlockPos scRoomPos : state.getSpacePosWithSCBlockPath().keySet()) {
            double distance = pos.getSquaredDistance(scRoomPos.getX(), scRoomPos.getY(), scRoomPos.getZ());
            if (distance < nearestDistance) {
                nearestPos = scRoomPos;
                nearestDistance = distance;
            }
        }

        if (nearestPos == null) return null;

        return PosUtil.flooredBlockPos(nearestPos.getX(), 64, nearestPos.getZ());
    }

    @Nullable
    public static BlockPos getNearestPos(ServerWorld world, BlockPos pos) {

        Optional<MinecraftServer> optionalServer = WorldUtil.getServer(world);
        return getNearestPos(SpaceCubeState.getOrCreate(optionalServer.get()), pos);
    }

    public static BlockPos getNewPos(ServerWorld world) {
        Optional<MinecraftServer> optionalServer = WorldUtil.getServer(world);
        return getNewPos(SpaceCubeState.getOrCreate(optionalServer.get()));
    }

    public static int getSpaceCubeCount(SpaceCubeState state) {
        return state.getSpacePosWithSCBlockPath().size();
    }

    public static int getSpaceCubeCount(ServerWorld world) {
        Optional<MinecraftServer> optionalServer = WorldUtil.getServer(world);
        return getSpaceCubeCount(SpaceCubeState.getOrCreate(optionalServer.get()));
    }

    // plan: include MCPitanLib
    public static <T> TypedActionResult<T> typedActionResult(ActionResult result, T t, boolean swingHand) {
        return ActionResultUtil.typedActionResult(result, t, swingHand);
    }

    public static <T> TypedActionResult<T> typedActionResult(ActionResult result, T t) {
        return typedActionResult(result, t, true);
    }

    public static <T> ActionResult actionResult(TypedActionResult<T> result) {
        return ActionResultUtil.actionResult(result);
    }

    // ----

    public static ServerWorld getSpaceCubeWorld(ServerWorld world) {
        Optional<World> optionalWorld = WorldUtil.getWorld(world, SpaceCube.SPACE_CUBE_DIMENSION_WORLD_KEY);
        return (ServerWorld) optionalWorld.get();
    }
}
