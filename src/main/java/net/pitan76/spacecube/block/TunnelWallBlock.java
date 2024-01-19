package net.pitan76.spacecube.block;

import ml.pkom.mcpitanlibarch.api.block.CompatibleBlockSettings;
import ml.pkom.mcpitanlibarch.api.block.ExtendBlock;
import ml.pkom.mcpitanlibarch.api.block.ExtendBlockEntityProvider;
import ml.pkom.mcpitanlibarch.api.event.block.BlockUseEvent;
import ml.pkom.mcpitanlibarch.api.event.block.TileCreateEvent;
import ml.pkom.mcpitanlibarch.api.util.TextUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.pitan76.spacecube.Blocks;
import net.pitan76.spacecube.api.data.TunnelSideData;
import net.pitan76.spacecube.api.tunnel.TunnelType;
import net.pitan76.spacecube.blockentity.SpaceCubeBlockEntity;
import net.pitan76.spacecube.blockentity.TunnelWallBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TunnelWallBlock extends WallBlock implements ExtendBlockEntityProvider {

    // 今のところは使っていない (Not used for now)
    public static final DirectionProperty TUNNEL_SIDE = DirectionProperty.of("tunnel_side", Direction.values());

    // トンネルの接続サイド (Connected side of the tunnel)
    public static final DirectionProperty CONNECTED_SIDE = DirectionProperty.of("connected_side", Direction.values());

    // Redstone Power
    public static final BooleanProperty POWERED = Properties.POWERED;

    public TunnelWallBlock(CompatibleBlockSettings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(CONNECTED_SIDE, Direction.UP).with(TUNNEL_SIDE, Direction.UP).with(POWERED, false));
    }

    public static TunnelType getTunnelType(World world, BlockPos pos) {
        TunnelWallBlockEntity blockEntity = (TunnelWallBlockEntity) world.getBlockEntity(pos);
        return blockEntity == null ? TunnelType.NONE : blockEntity.getTunnelType();
    }

    @Override
    public ActionResult onRightClick(BlockUseEvent event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();

        if (world.isClient()) return ActionResult.SUCCESS;

        BlockState state = world.getBlockState(pos);

        if (event.getPlayer().isSneaking()) {
            // トンネルをはがす
            if (world.getBlockEntity(pos) instanceof TunnelWallBlockEntity) {
                TunnelWallBlockEntity tunnelWallBlockEntity = (TunnelWallBlockEntity) world.getBlockEntity(pos);
                Item item = tunnelWallBlockEntity.getTunnelItem();
                if (item != null) {
                    event.getPlayer().giveStack(new ItemStack(item, 1));
                }

                if (tunnelWallBlockEntity.existSpaceCubeBlockEntity()) {
                    SpaceCubeBlockEntity spaceCubeBlockEntity = tunnelWallBlockEntity.getSpaceCubeBlockEntity();

                    TunnelType tunnelType = tunnelWallBlockEntity.getTunnelType();
                    Direction dir = state.get(CONNECTED_SIDE);

                    if (spaceCubeBlockEntity.hasTunnel(tunnelType, dir))
                        spaceCubeBlockEntity.removeTunnel(tunnelType, dir);
                }
            }
            world.setBlockState(pos, Blocks.SOLID_WALL.getDefaultState());

            return ActionResult.SUCCESS;
        }

        if (world.getBlockEntity(pos) instanceof TunnelWallBlockEntity) {
            TunnelWallBlockEntity tunnelWallBlockEntity = (TunnelWallBlockEntity) world.getBlockEntity(pos);

            if (tunnelWallBlockEntity.existSpaceCubeBlockEntity()) {
                SpaceCubeBlockEntity spaceCubeBlockEntity = tunnelWallBlockEntity.getSpaceCubeBlockEntity();

                // トンネルの接続サイドを変更する Change the connected side of the tunnel

                if (state.contains(CONNECTED_SIDE)) {
                    Direction dir = state.get(CONNECTED_SIDE);

                    TunnelSideData tunnelSide = spaceCubeBlockEntity.getTunnelSide(tunnelWallBlockEntity.getTunnelType());

                    // すべての接続サイドが使われている場合 (If all connected sides are used)
                    if (spaceCubeBlockEntity.tunnelIsFull(tunnelWallBlockEntity.getTunnelType())) {
                        event.getPlayer().sendMessage(TextUtil.translatable("message.spacecube.tunnel_full"));
                        return ActionResult.FAIL;
                    }

                    Direction nextDir = tunnelSide.getNextDir(dir);
                    world.setBlockState(pos, world.getBlockState(pos).with(CONNECTED_SIDE, nextDir));
                    if (!tunnelSide.hasTunnel(nextDir)) {
                        tunnelSide.removeTunnel(dir);
                        tunnelSide.addTunnel(nextDir, pos);
                    }
                }
            }
        }

        return super.onRightClick(event);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TUNNEL_SIDE);
        builder.add(CONNECTED_SIDE);
        builder.add(POWERED);
        super.appendProperties(builder);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(TileCreateEvent event) {
        return new TunnelWallBlockEntity(event);
    }
}
