package com.chinaex123.void_dimension.dimServer;

import com.chinaex123.void_dimension.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
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

    /**
     * 创建方块状态定义，注册传送门方块使用的所有属性
     * 此方法在方块初始化时被调用，用于声明 AXIS 属性以便方块状态可以存储朝向信息
     *
     * @param builder 状态定义构建器，用于添加方块属性
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    /**
     * 根据放置上下文设置传送门的初始状态
     * 传送门的轴向会被设置为玩家朝向顺时针旋转 90 度后的方向
     *
     * @param context 方块放置上下文，包含玩家朝向等信息
     * @return 设置好轴向的传送门方块状态
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getClockWise().getAxis());
    }

    /**
     * 获取传送门方块的碰撞箱形状
     * 传送门没有碰撞箱，玩家可以自由穿过
     *
     * @param state 方块状态
     * @param world 世界访问器
     * @param pos 方块位置
     * @param context 碰撞上下文
     * @return 空的碰撞箱
     */
    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.empty();
    }

    /**
     * 获取传送门方块的碰撞箱形状
     * 根据传送门的轴向返回对应的 X 轴或 Z 轴方向的碰撞箱
     *
     * @param state 方块状态
     * @param world 世界访问器
     * @param pos 方块位置
     * @param context 碰撞上下文
     * @return 对应轴向的碰撞箱形状
     */
    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
    }

    /**
     * 当实体在方块内部时调用
     */
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean intersects) {
        if (intersects && entity instanceof ServerPlayer player && !level.isClientSide()) {
            Server.handlePortalTeleport(player, pos);
        }
    }

    /**
     * 判断指定方块是否为传送门框架的一部分
     * 此方法用于 Minecraft 原版传送门机制的兼容性检测
     *
     * @param state 方块状态
     * @param level 世界访问器
     * @param pos 方块位置
     * @return 如果是传送门框架方块返回 true，否则返回 false
     */
    @Override
    public boolean isPortalFrame(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return state.is(ModBlocks.NAUGHT_STONE.get());
    }
}
