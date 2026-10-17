package com.chinaex123.void_dimension.block;

import com.chinaex123.void_dimension.event.PortalDimensionHelper;
import com.chinaex123.void_dimension.event.PortalTeleportManager;
import com.chinaex123.void_dimension.event.VoidPortalClientHooks;
import com.chinaex123.void_dimension.init.VDBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * 虚空传送门方块。
 * <p>
 * 实现原版 Portal 接口，作为通往虚空维度的传送门：
 * 定义水平轴向属性、无碰撞的传送门形状，
 * 并在实体进入时触发服务端传送逻辑与客户端视觉效果。
 */
public class VoidPortal extends Block implements Portal {

    /** 传送门朝向轴（水平轴） */
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    /** X 轴向传送门的碰撞/渲染形状 */
    protected static final VoxelShape X_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    /** Z 轴向传送门的碰撞/渲染形状 */
    protected static final VoxelShape Z_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);

    /**
     * 构造函数。
     *
     * @param properties 方块属性
     */
    public VoidPortal(Properties properties) {
        super(properties.noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.X));
    }

    /**
     * 定义方块状态。
     *
     * @param builder 状态构建器
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    /**
     * 根据放置时的朝向确定传送门轴。
     *
     * @param context 放置上下文
     * @return 设置了轴向的方块状态
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getClockWise().getAxis());
    }

    /**
     * 获取碰撞形状。
     * <p>
     * 传送门无碰撞，实体可直接穿过。
     *
     * @param state   方块状态
     * @param world   方块获取器
     * @param pos     方块位置
     * @param context 碰撞上下文
     * @return 空形状
     */
    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.empty();
    }

    /**
     * 获取渲染形状。
     * <p>
     * 根据轴向返回对应的细长形状。
     *
     * @param state   方块状态
     * @param world   方块获取器
     * @param pos     方块位置
     * @param context 碰撞上下文
     * @return 对应轴向的形状
     */
    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
    }

    /**
     * 实体进入传送门时的处理。
     * <p>
     * 服务端触发传送流程；客户端播放传送门视觉效果。
     *
     * @param state      方块状态
     * @param level      世界
     * @param pos        方块位置
     * @param entity     进入的实体
     * @param applier    方块效果施加器
     * @param intersects 实体是否与方块相交
     */
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                             InsideBlockEffectApplier applier, boolean intersects) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            PortalTeleportManager.beginPortalTeleport(player, pos);
        }

        if (level.isClientSide()) {
            VoidPortalClientHooks.onPlayerInsidePortalClient();
        }
    }

    /**
     * 获取本地过渡效果。
     *
     * @return 混乱（CONFUSION）过渡效果
     */
    @Override
    public @NonNull Transition getLocalTransition() {
        return Transition.CONFUSION;
    }

    /**
     * 获取传送门过渡时间。
     *
     * @param level  服务端世界
     * @param entity 实体
     * @return 过渡所需刻数（80 刻）
     */
    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        return 80;
    }

    /**
     * 获取传送目的地。
     * <p>
     * 委托给维度辅助类，计算目标维度与落点。
     *
     * @param level    服务端世界
     * @param entity   实体
     * @param entryPos 进入传送门的位置
     * @return 传送过渡对象
     */
    @Override
    public TeleportTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos entryPos) {
        return PortalDimensionHelper.createTeleportTransition(level, entity, entryPos);
    }

    /**
     * 判断给定方块是否为传送门框架。
     *
     * @param state 方块状态
     * @param level 方块获取器
     * @param pos   方块位置
     * @return 若为虚无石则返回 true
     */
    public boolean isPortalFrame(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return state.is(VDBlocks.NAUGHT_STONE.get());
    }
}