package net.pitan76.spacecube.api.util;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.pitan76.spacecube.SpaceCube;
import net.pitan76.spacecube.world.SpaceCubeState;
import org.jetbrains.annotations.Nullable;

public class SpaceCubeUtil {

    // 新規のスペースキューブ(部屋)の座標を取得する
    public static BlockPos getNewPosOld(SpaceCubeState state) {
        // スペースキューブの数を取得する
        int scCount = state.getSpacePosWithSCBlockPath().size() + 1;

        // 6個ごとに座標を変える
        int mod = Math.floorMod(scCount, 6);
        int div = Math.floorDiv(scCount, 6);
        //     □
        //   □ □ □
        // □ □ ○ □ □
        //   □ □ □
        //     □
        // ○ が 0, 64, 0 として考えて周辺にキューブを設置していくよ
        // ○ is considered to be 0, 64, 0 and cubes are placed around it

        switch (mod) {
            case 0:
                return new BlockPos(div * 1024, 64, div * 1024);
            case 1:
                return new BlockPos(div * -1024, 64, div * 1024);
            case 2:
                return new BlockPos(div * 1024 + 1024, 64, div * 1024);
            case 3:
                return new BlockPos(div * 1024, 64, div * 1024 + 1024);
            case 4:
                return new BlockPos(div * -1024 - 1024, 64, div * 1024);
            case 5:
                return new BlockPos(div * -1024, 64, div * 1024 - 1024);

            // まぁたぶんないけど念のために
            default:
                return new BlockPos(0, 64, 0);
        }
    }

    // 新規のスペースキューブ(部屋)の座標を取得する
    public static BlockPos getNewPos(SpaceCubeState state) {
        // スペースキューブの数を取得する
        int scCount = state.getSpacePosWithSCBlockPath().size() + 1;

        



        // 6個ごとに座標を変える
        int mod = Math.floorMod(scCount, 6);
        int div = Math.floorDiv(scCount, 6);
        //     □
        //   □ □ □
        // □ □ ○ □ □
        //   □ □ □
        //     □
        // ○ が 0, 64, 0 として考えて周辺にキューブを設置していくよ
        // ○ is considered to be 0, 64, 0 and cubes are placed around it



        switch (mod) {
            case 0:
                return new BlockPos(div * 1024, 64, div * 1024);
            case 1:
                return new BlockPos(div * -1024, 64, div * 1024);
            case 2:
                return new BlockPos(div * 1024 + 1024, 64, div * 1024);
            case 3:
                return new BlockPos(div * 1024, 64, div * 1024 + 1024);
            case 4:
                return new BlockPos(div * -1024 - 1024, 64, div * 1024);
            case 5:
                return new BlockPos(div * -1024, 64, div * 1024 - 1024);

            // まぁたぶんないけど念のために
            default:
                return new BlockPos(0, 64, 0);
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

        return new BlockPos(nearestPos.getX(), 64, nearestPos.getZ());
    }

    public static BlockPos getNearestPos(ServerWorld world, BlockPos pos) {
        return getNearestPos(SpaceCubeState.getOrCreate(world.getServer()), pos);
    }

    public static BlockPos getNewPos(ServerWorld world) {
        return getNewPos(SpaceCubeState.getOrCreate(world.getServer()));
    }

    public static int getSpaceCubeCount(SpaceCubeState state) {
        return state.getSpacePosWithSCBlockPath().size();
    }

    public static int getSpaceCubeCount(ServerWorld world) {
        return getSpaceCubeCount(SpaceCubeState.getOrCreate(world.getServer()));
    }

    // plan: include MCPitanLib
    public static <T> TypedActionResult<T> typedActionResult(ActionResult result, T t, boolean swingHand) {
        switch (result) {
            case PASS:
                return TypedActionResult.pass(t);
            case SUCCESS:
                return TypedActionResult.success(t, swingHand);
            case FAIL:
                return TypedActionResult.fail(t);
            case CONSUME:
            case CONSUME_PARTIAL:
                return TypedActionResult.consume(t);
        }
        return TypedActionResult.pass(t);
    }

    public static <T> TypedActionResult<T> typedActionResult(ActionResult result, T t) {
        return typedActionResult(result, t, true);
    }

    public static <T> ActionResult actionResult(TypedActionResult<T> result) {
        return result.getResult();
    }

    // ----

    public static ServerWorld getSpaceCubeWorld(ServerWorld world) {
        return world.getServer().getWorld(SpaceCube.SPACE_CUBE_DIMENSION_WORLD_KEY);
    }
}
