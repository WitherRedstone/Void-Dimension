package com.chinaex123.void_dimension.dimServer;

import com.chinaex123.void_dimension.register.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class VoidPortal extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    // 东西走向 - X轴
    protected static final VoxelShape X_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);
    // 南北走向 - Z轴
    protected static final VoxelShape Z_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);

    public VoidPortal(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getClockWise().getAxis());
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
    }

    @Override
    public void entityInside(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
        if (entity instanceof ServerPlayer player && !level.isClientSide) {
            if (player.getBoundingBox().intersects(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1.0D, pos.getY() + 1.0D, pos.getZ() + 1.0D)) {
                Server.handlePortalTeleport(player, pos);
            }
        }
    }

    @Override
    public boolean isPortalFrame(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return state.is(ModBlocks.NAUGHT_STONE.get());
    }
}
