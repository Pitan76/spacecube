package net.pitan76.spacecube.api.util;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CubeGenerator {
    // TODO: Y座標の最低は固定したい

    // 中心からの距離を指定して、中心が空洞のキューブを生成するみたいな感じです。だから実際には(2倍 - 1)のサイズになる
    // Generate a cube with a hollow center by specifying the distance from the center.　So it's actually twice the size

    // ちなみに空気の部分の距離なだけなので壁のブロックは含まない計算で指定してや
    // By the way, it's just the distance of the air part, so specify it with a calculation that doesn't include the wall block
    public static void generateCube(World world, BlockPos centerPos, Block block, int size) {
        generateCube(world, centerPos, block, size, size, size);
    }

    // なんかwidthとかheightとかdepthとかの引数名にしてますが、中心からの距離なので不適切かな？笑
    public static void generateCube(World world, BlockPos centerPos, Block block, int width, int height, int depth) {
        // 中は空洞にする
        // Make the inside hollow

        // 中心の座標xyzをそれぞれ代入してみる
        // Assign the center coordinates xyz to each
        int x = centerPos.getX();
        int y = centerPos.getY();
        int z = centerPos.getZ();

        // これはループして空洞の外側のブロックを配置していくやつ
        // This is a loop that places blocks on the outside of the hollow
        for (int i = x - width; i <= x + width; i++) {
            for (int j = y - height; j <= y + height; j++) {
                for (int k = z - depth; k <= z + depth; k++) {
                    // 壁の座標のとき (When it's a wall coordinate)
                    if (i == x - width || i == x + width || j == y - height || j == y + height || k == z - depth || k == z + depth) {
                        // ブロックおく (Place block)
                        world.setBlockState(new BlockPos(i, j, k), block.getDefaultState());
                    }
                }
            }
        }
    }
}
