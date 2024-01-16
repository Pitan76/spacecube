package net.pitan76.spacecube.api;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.pitan76.spacecube.SpaceCube;
import net.pitan76.spacecube.world.SpaceCubeState;

public class SpaceCubeUtil {

    // 新規のスペースキューブ(部屋)の座標を取得する
    public static BlockPos getNewPos(SpaceCubeState state) {
        // スペースキューブの数を取得する
        int scCount = state.getSpacePosWithSCBlockPath().size() + 1;

        // 8個ごとに座標を変える
        int mod = Math.floorMod(scCount, 8);
        int div = Math.floorDiv(scCount, 8);

        // □ □ □
        // □ ○ □
        // □ □ □
        // ○ が 0, 64, 0 として考えて周辺にキューブを設置していくよ
        // ○ is considered to be 0, 64, 0 and cubes are placed around it

        return switch (mod) {
            case 0 -> new BlockPos(div * 1024, 64, div * 1024);
            case 1 -> new BlockPos(div * 1024 + mod * 1024, 64, div * 1024);
            case 2 -> new BlockPos(div * 1024 + mod * 1024, 64, div * 1024 + mod * 1024);
            case 3 -> new BlockPos(div * 1024, 64, div * 1024 + mod * 1024);
            case 4 -> new BlockPos(div * -1024, 64, div * 1024);
            case 5 -> new BlockPos(div * -1024 + (mod - 4) * -1024, 64, div * 1024);
            case 6 -> new BlockPos(div * -1024 + (mod - 4) * -1024, 64, div * 1024 + (mod - 4) * -1024);
            case 7 -> new BlockPos(div * -1024, 64, div * 1024 + (mod - 4) * -1024);

            // まぁたぶんないけど念のために
            default -> new BlockPos(0, 64, 0);
        };
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
        return switch (result) {
            case PASS -> TypedActionResult.pass(t);
            case SUCCESS -> TypedActionResult.success(t, swingHand);
            case FAIL -> TypedActionResult.fail(t);
            case CONSUME, CONSUME_PARTIAL -> TypedActionResult.consume(t);
        };
    }

    public static <T> TypedActionResult<T> typedActionResult(ActionResult result, T t) {
        return typedActionResult(result, t, true);
    }

    public static <T> ActionResult actionResult(TypedActionResult<T> result) {
        return result.getResult();
    }

    public static ServerWorld getSpaceCubeWorld(ServerWorld world) {
        return world.getServer().getWorld(SpaceCube.SPACE_CUBE_DIMENSION_WORLD_KEY);
    }
}
