package com.chinaex123.void_dimension.dimServer;

import com.chinaex123.void_dimension.register.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public class VoidPortalBreakEvent {
    /**
     * 监听方块破坏事件，当传送门框架关键方块或传送门方块被破坏时破坏整个传送门
     */
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Level level = event.getPlayer().level();

        // 只在服务端处理
        if (level.isClientSide()) {
            return;
        }

        BlockPos brokenPos = event.getPos();
        BlockState brokenState = event.getState();

        // 如果破坏的是传送门框架方块（虚空石）
        if (brokenState.is(ModBlocks.NAUGHT_STONE.get())) {
            // 检查是否破坏了关键框架方块
            if (isCriticalFrameBlock(level, brokenPos)) {
                // 查找并破坏相关的传送门
                destroyRelatedPortals(level, brokenPos);
            }
        }
        // 如果破坏的是传送门方块本身
        else if (brokenState.is(ModBlocks.VOID_PORTAL.get())) {
            try {
                // 获取被破坏传送门的朝向
                Direction.Axis portalAxis = brokenState.getValue(VoidPortal.AXIS);
                // 根据传送门朝向确定框架方向
                Direction.Axis frameAxis = portalAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;

                // 破坏整个传送门结构
                destroyPortalStructure(level, brokenPos, frameAxis);
            } catch (Exception e) {
                // 忽略异常
            }
        }
    }

    /**
     * 判断是否为关键框架方块（只有破坏这些方块才会触发传送门破坏）
     */
    private boolean isCriticalFrameBlock(Level level, BlockPos pos) {
        // 检查6个方向相邻的位置是否有传送门
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            // 如果相邻位置是传送门方块
            if (adjacentState.is(ModBlocks.VOID_PORTAL.get())) {
                try {
                    // 获取传送门朝向
                    Direction.Axis portalAxis = adjacentState.getValue(VoidPortal.AXIS);

                    // 根据传送门朝向确定框架方向
                    Direction.Axis frameAxis = portalAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;

                    // 检查这个位置是否在传送门框架的关键位置上
                    if (isOnPortalFrameEdge(level, pos, adjacentPos, frameAxis)) {
                        return true;
                    }
                } catch (Exception e) {
                    // 忽略异常
                }
            }
        }
        return false;
    }

    /**
     * 检查位置是否在传送门框架的边缘上
     */
    private boolean isOnPortalFrameEdge(Level level, BlockPos framePos, BlockPos portalPos, Direction.Axis frameAxis) {
        try {
            // 找到传送门框架的角落位置
            BlockPos cornerPos = findPortalCorner(level, portalPos, frameAxis);
            if (cornerPos == null) {
                return false;
            }

            // 计算传送门尺寸
            PortalDimensions dimensions = calculatePortalDimensions(level, cornerPos, frameAxis);

            // 计算相对位置
            Direction right = getPositiveDirection(frameAxis);
            BlockPos relativePos = framePos.subtract(cornerPos);

            int relX = getRelativeCoordinate(relativePos, right);
            int relY = relativePos.getY();

            // 检查是否在框架边缘上
            // 底部边框 (y=0)
            if (relY == 0 && relX >= 0 && relX < dimensions.width) {
                return true;
            }

            // 顶部边框 (y=height-1)
            if (relY == dimensions.height - 1 && relX >= 0 && relX < dimensions.width) {
                return true;
            }

            // 左侧边框 (x=0)
            if (relX == 0 && relY >= 0 && relY < dimensions.height) {
                return true;
            }

            // 右侧边框 (x=width-1)
            return relX == dimensions.width - 1 && relY >= 0 && relY < dimensions.height;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取相对坐标
     */
    private int getRelativeCoordinate(BlockPos relativePos, Direction direction) {
        return switch (direction) {
            case EAST -> relativePos.getX();
            case SOUTH -> relativePos.getZ();
            default -> 0;
        };
    }

    /**
     * 查找并破坏相关的传送门
     */
    private void destroyRelatedPortals(Level level, BlockPos brokenPos) {
        // 检查6个方向相邻的位置
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = brokenPos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            // 如果相邻位置是传送门方块
            if (adjacentState.is(ModBlocks.VOID_PORTAL.get())) {
                try {
                    // 获取传送门朝向
                    Direction.Axis portalAxis = adjacentState.getValue(VoidPortal.AXIS);

                    // 根据传送门朝向确定框架方向
                    Direction.Axis frameAxis = portalAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;

                    // 破坏整个传送门结构
                    destroyPortalStructure(level, adjacentPos, frameAxis);
                } catch (Exception e) {
                    // 忽略异常
                }
            }
        }
    }

    /**
     * 查找传送门框架的角落位置
     */
    private BlockPos findPortalCorner(Level level, BlockPos portalPos, Direction.Axis frameAxis) {
        BlockPos pos = portalPos.immutable();

        // 向框架的负方向移动到边缘
        Direction negativeDir = getNegativeDirection(frameAxis);
        while (isFrameOrPortalBlock(level, pos.relative(negativeDir)) ||
                isSameAxisPortal(level, pos.relative(negativeDir), frameAxis)) {
            pos = pos.relative(negativeDir);
        }

        // 向下移动到框架底部
        while (isFrameOrPortalBlock(level, pos.below()) ||
                isSameAxisPortal(level, pos.below(), frameAxis)) {
            pos = pos.below();
        }

        return pos;
    }

    /**
     * 检查是否为相同轴向的传送门方块
     */
    private boolean isSameAxisPortal(Level level, BlockPos pos, Direction.Axis expectedAxis) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(ModBlocks.VOID_PORTAL.get())) {
            return false;
        }

        try {
            Direction.Axis portalAxis = state.getValue(VoidPortal.AXIS);
            Direction.Axis frameAxis = portalAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
            return frameAxis == expectedAxis;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 计算传送门框架尺寸
     */
    private PortalDimensions calculatePortalDimensions(Level level, BlockPos cornerPos, Direction.Axis frameAxis) {
        int height = countFrameBlocks(level, cornerPos, Direction.UP, frameAxis);
        int width = countFrameBlocks(level, cornerPos, getPositiveDirection(frameAxis), frameAxis);
        return new PortalDimensions(width, height);
    }

    /**
     * 计算指定方向上的框架方块数量
     */
    private int countFrameBlocks(Level level, BlockPos start, Direction direction, Direction.Axis frameAxis) {
        int count = 1;
        BlockPos currentPos = start.relative(direction);

        while (isFrameOrPortalBlock(level, currentPos) ||
                isSameAxisPortal(level, currentPos, frameAxis)) {
            count++;
            currentPos = currentPos.relative(direction);
        }
        return count;
    }

    /**
     * 破坏传送门结构
     */
    private void destroyPortalStructure(Level level, BlockPos portalPos, Direction.Axis frameAxis) {
        try {
            BlockPos cornerPos = findPortalCorner(level, portalPos, frameAxis);
            if (cornerPos == null) {
                return;
            }

            PortalDimensions dimensions = calculatePortalDimensions(level, cornerPos, frameAxis);
            Direction right = getPositiveDirection(frameAxis);

            // 破坏传送门内部的所有方块
            int portalWidth = dimensions.width - 2;
            int portalHeight = dimensions.height - 2;

            for (int x = 1; x <= portalWidth; x++) {
                for (int y = 1; y <= portalHeight; y++) {
                    BlockPos destroyPos = cornerPos.relative(right, x).above(y);
                    if (level.getBlockState(destroyPos).is(ModBlocks.VOID_PORTAL.get())) {
                        level.destroyBlock(destroyPos, false); // false表示不掉落物品
                    }
                }
            }
        } catch (Exception e) {
            // 忽略错误
        }
    }

    /**
     * 检查是否为传送门框架方块或传送门方块
     */
    private boolean isFrameOrPortalBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(ModBlocks.NAUGHT_STONE.get()) || state.is(ModBlocks.VOID_PORTAL.get());
    }

    /**
     * 获取指定轴向的正方向
     */
    private Direction getPositiveDirection(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
    }

    /**
     * 获取指定轴向的负方向
     */
    private Direction getNegativeDirection(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.WEST : Direction.NORTH;
    }

    /**
     * 传送门尺寸数据类
     */
    private record PortalDimensions(int width, int height) {
    }
}
