package com.chinaex123.void_dimension.dimServer;

import com.chinaex123.void_dimension.register.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
     * 判断被破坏的方块是否为传送门框架的关键方块
     * 只有破坏关键框架方块才会触发整个传送门的破坏
     *
     * @param level 世界对象
     * @param pos 被破坏方块的位置
     * @return 如果为关键框架方块返回 true，否则返回 false
     */
    private boolean isCriticalFrameBlock(Level level, BlockPos pos) {
        // 检查与传送门直接相邻的框架方块
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            if (adjacentState.is(ModBlocks.VOID_PORTAL.get())) {
                try {
                    Direction.Axis portalAxis = adjacentState.getValue(VoidPortal.AXIS);
                    Direction.Axis frameAxis = portalAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;

                    if (isOnPortalFrameEdge(level, pos, adjacentPos, frameAxis)) {
                        return true;
                    }
                } catch (Exception e) {
                    // 忽略异常
                }
            }
        }

        // 适用于框架不完整的情况（如门角已被破坏）
        if (hasNearbyFrameBlocks(level, pos)) {
            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = pos.relative(direction);
                BlockState adjacentState = level.getBlockState(adjacentPos);

                if (adjacentState.is(ModBlocks.NAUGHT_STONE.get())) {
                    for (Direction adjDir : Direction.values()) {
                        BlockPos portalCheckPos = adjacentPos.relative(adjDir);
                        BlockState portalState = level.getBlockState(portalCheckPos);

                        if (portalState.is(ModBlocks.VOID_PORTAL.get())) {
                            try {
                                Direction.Axis portalAxis = portalState.getValue(VoidPortal.AXIS);
                                Direction.Axis frameAxis = portalAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;

                                if (isOnPortalFrameEdgeImproved(level, pos, portalCheckPos, frameAxis)) {
                                    return true;
                                }
                            } catch (Exception e) {
                                // 忽略异常
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * 检查位置是否在传送门框架的边缘上
     *
     * @param level 世界对象
     * @param framePos 待检测的框架方块位置
     * @param portalPos 传送门方块位置
     * @param frameAxis 框架轴向
     * @return 是否为框架边缘
     */
    private boolean isOnPortalFrameEdgeImproved(Level level, BlockPos framePos, BlockPos portalPos, Direction.Axis frameAxis) {
        try {
            // 使用改进的角落查找方法，支持不完整的框架结构
            BlockPos cornerPos = findPortalCornerImproved(level, portalPos, frameAxis);
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

            // 放宽检查条件，允许一定程度的不完整框架
            // 底部边框 (y=0)
            if (relY == 0 && relX >= 0 && relX <= dimensions.width) {
                return true;
            }

            // 顶部边框 (y=height-1)
            if (relY == dimensions.height - 1 && relX >= 0 && relX <= dimensions.width) {
                return true;
            }

            // 左侧边框 (x=0)
            if (relX == 0 && relY >= 0 && relY <= dimensions.height) {
                return true;
            }

            // 右侧边框 (x=width-1)
            return relX == dimensions.width - 1 && relY >= 0 && relY <= dimensions.height;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 查找传送门框架的角落位置（改进版本）
     * 采用多种策略查找角落，确保在框架不完整时也能正确定位
     *
     * @param level 世界对象
     * @param portalPos 传送门位置
     * @param frameAxis 框架轴向
     * @return 角落位置，找不到则返回 null
     */
    private BlockPos findPortalCornerImproved(Level level, BlockPos portalPos, Direction.Axis frameAxis) {
        // 先尝试原有方法
        BlockPos cornerPos = findPortalCorner(level, portalPos, frameAxis);
        if (cornerPos != null && isValidCorner(level, cornerPos, frameAxis)) {
            return cornerPos;
        }

        // 如果原方法失败，尝试从传送门向下找到底部再找角落
        cornerPos = findCornerByScanningDown(level, portalPos, frameAxis);
        if (cornerPos != null && isValidCorner(level, cornerPos, frameAxis)) {
            return cornerPos;
        }

        // 扫描周围寻找可能的角落
        return findCornerByScanningArea(level, portalPos, frameAxis);
    }

    /**
     * 验证角落位置是否有效
     *
     * @param level 世界对象
     * @param cornerPos 角落位置
     * @param frameAxis 框架轴向
     * @return 是否有效
     */
    private boolean isValidCorner(Level level, BlockPos cornerPos, Direction.Axis frameAxis) {
        Direction right = getPositiveDirection(frameAxis);

        // 检查角落右边是否有框架
        boolean hasFrameToRight = isFrameOrPortalBlock(level, cornerPos.relative(right));

        // 检查角落上方是否有框架
        boolean hasFrameAbove = isFrameOrPortalBlock(level, cornerPos.above());

        return hasFrameToRight || hasFrameAbove;
    }

    /**
     * 通过向下扫描查找角落
     * 适用于底部框架完整但侧边不完整的场景
     *
     * @param level 世界对象
     * @param portalPos 传送门位置
     * @param frameAxis 框架轴向
     * @return 角落位置
     */
    private BlockPos findCornerByScanningDown(Level level, BlockPos portalPos, Direction.Axis frameAxis) {
        BlockPos pos = portalPos.immutable();

        // 先向下到底
        while (!isFrameOrPortalBlock(level, pos.below()) && pos.getY() > level.getMinBuildHeight()) {
            pos = pos.below();
        }

        // 向框架负方向移动
        Direction negativeDir = getNegativeDirection(frameAxis);
        while (isFrameOrPortalBlock(level, pos.relative(negativeDir))) {
            pos = pos.relative(negativeDir);
        }

        return pos;
    }

    /**
     * 通过区域扫描查找角落
     * 适用于框架严重损坏的场景
     *
     * @param level 世界对象
     * @param portalPos 传送门位置
     * @param frameAxis 框架轴向
     * @return 角落位置，找不到则返回 null
     */
    private BlockPos findCornerByScanningArea(Level level, BlockPos portalPos, Direction.Axis frameAxis) {
        Direction negativeDir = getNegativeDirection(frameAxis);
        Direction down = Direction.DOWN;

        BlockPos currentPos = portalPos.immutable();

        // 向左/北和向下搜索
        for (int i = 0; i < 10; i++) {
            // 先向左/北
            while (isFrameOrPortalBlock(level, currentPos.relative(negativeDir))) {
                currentPos = currentPos.relative(negativeDir);
            }

            // 再向下
            while (isFrameOrPortalBlock(level, currentPos.below())) {
                currentPos = currentPos.below();
            }

            // 如果当前位置下方是固体方块，可能是角落
            if (isFrameOrPortalBlock(level, currentPos)) {
                return currentPos;
            }
        }

        return null;
    }

    /**
     * 检查附近是否有其他框架方块
     *
     * @param level 世界对象
     * @param pos 检测位置
     * @return 是否有至少 2 个相邻框架方块
     */
    private boolean hasNearbyFrameBlocks(Level level, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).is(ModBlocks.NAUGHT_STONE.get())) {
                count++;
            }
        }
        return count >= 2;
    }

    /**
     * 检查指定位置是否在传送门框架的边缘上
     * 通过计算相对于框架角落的位置来判断是否位于四条边框上
     *
     * @param level 世界对象
     * @param framePos 待检测的框架方块位置
     * @param portalPos 传送门方块位置（用于定位框架）
     * @param frameAxis 框架的轴向（X 轴表示东西走向，Z 轴表示南北走向）
     * @return 如果在框架边缘上返回 true，否则返回 false
     */
    private boolean isOnPortalFrameEdge(Level level, BlockPos framePos, BlockPos portalPos, Direction.Axis frameAxis) {
        try {
            BlockPos cornerPos = findPortalCorner(level, portalPos, frameAxis);
            if (cornerPos == null) {
                return false;
            }

            PortalDimensions dimensions = calculatePortalDimensions(level, cornerPos, frameAxis);

            Direction right = getPositiveDirection(frameAxis);
            BlockPos relativePos = framePos.subtract(cornerPos);

            int relX = getRelativeCoordinate(relativePos, right);
            int relY = relativePos.getY();

            // 底部边框检测
            if (relY == 0 && relX >= 0 && relX < dimensions.width) {
                return true;
            }

            // 顶部边框检测
            if (relY == dimensions.height - 1 && relX >= 0 && relX < dimensions.width) {
                return true;
            }

            // 左侧边框检测
            if (relX == 0 && relY >= 0 && relY < dimensions.height) {
                return true;
            }

            // 右侧边框检测
            return relX == dimensions.width - 1 && relY >= 0 && relY < dimensions.height;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取相对位置在指定方向上的坐标值
     * 根据框架轴向提取对应的坐标分量（X 轴或 Z 轴）
     *
     * @param relativePos 相对位置
     * @param direction 方向（用于确定提取哪个坐标轴）
     * @return 返回对应方向的坐标值，如果不是 EAST 或 SOUTH 则返回 0
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
     * 扩大了搜索范围，确保能正确找到并破坏所有传送门方块
     *
     * @param level 世界对象
     * @param brokenPos 被破坏的方块位置
     */
    private void destroyRelatedPortals(Level level, BlockPos brokenPos) {
        // 检查直接相邻的传送门
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = brokenPos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            if (adjacentState.is(ModBlocks.VOID_PORTAL.get())) {
                try {
                    Direction.Axis portalAxis = adjacentState.getValue(VoidPortal.AXIS);
                    Direction.Axis frameAxis = portalAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;

                    destroyPortalStructure(level, adjacentPos, frameAxis);
                    return; // 找到一个就足够了
                } catch (Exception e) {
                    // 忽略异常
                }
            }
        }

        // 扩大搜索范围到周围 5x5x5 区域
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos checkPos = brokenPos.offset(dx, dy, dz);
                    BlockState checkState = level.getBlockState(checkPos);

                    if (checkState.is(ModBlocks.VOID_PORTAL.get())) {
                        try {
                            Direction.Axis portalAxis = checkState.getValue(VoidPortal.AXIS);
                            Direction.Axis frameAxis = portalAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;

                            destroyPortalStructure(level, checkPos, frameAxis);
                            return;
                        } catch (Exception e) {
                            // 忽略异常
                        }
                    }
                }
            }
        }
    }

    /**
     * 查找传送门框架的角落位置（左下角）
     * 从传送门方块出发，先向框架负方向移动到边缘，再向下移动到底部
     *
     * @param level 世界对象
     * @param portalPos 传送门方块的起始位置
     * @param frameAxis 框架的轴向（X 轴或 Z 轴）
     * @return 框架角落的位置坐标
     */
    private BlockPos findPortalCorner(Level level, BlockPos portalPos, Direction.Axis frameAxis) {
        BlockPos pos = portalPos.immutable();

        Direction negativeDir = getNegativeDirection(frameAxis);
        while (isFrameOrPortalBlock(level, pos.relative(negativeDir)) ||
                isSameAxisPortal(level, pos.relative(negativeDir), frameAxis)) {
            pos = pos.relative(negativeDir);
        }

        while (isFrameOrPortalBlock(level, pos.below()) ||
                isSameAxisPortal(level, pos.below(), frameAxis)) {
            pos = pos.below();
        }

        return pos;
    }

    /**
     * 检查指定位置是否为相同轴向的传送门方块
     * 用于判断传送门方块是否属于同一个框架结构
     *
     * @param level 世界对象
     * @param pos 待检测的位置
     * @param expectedAxis 期望的框架轴向
     * @return 如果是相同轴向的传送门方块返回 true，否则返回 false
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
     * 计算传送门框架的尺寸
     * 从角落位置开始，分别向上和向右统计框架方块数量
     *
     * @param level 世界对象
     * @param cornerPos 框架角落位置（左下角）
     * @param frameAxis 框架轴向
     * @return 包含宽度和高度的 PortalDimensions 对象
     */
    private PortalDimensions calculatePortalDimensions(Level level, BlockPos cornerPos, Direction.Axis frameAxis) {
        int height = countFrameBlocks(level, cornerPos, Direction.UP, frameAxis);
        int width = countFrameBlocks(level, cornerPos, getPositiveDirection(frameAxis), frameAxis);
        return new PortalDimensions(width, height);
    }

    /**
     * 计算指定方向上的框架方块数量
     * 用于统计传送门框架在某个方向上的长度
     *
     * @param level 世界对象
     * @param start 起始位置
     * @param direction 统计方向
     * @param frameAxis 框架轴向（用于判断同轴向传送门）
     * @return 框架方块的总数量（包含起始位置）
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
     * 使用增强的角落查找方法，并提供备用清除方案
     *
     * @param level 世界对象
     * @param portalPos 传送门位置
     * @param frameAxis 框架轴向
     */
    private void destroyPortalStructure(Level level, BlockPos portalPos, Direction.Axis frameAxis) {
        try {
            // 使用改进的方法查找角落
            BlockPos cornerPos = findPortalCornerImproved(level, portalPos, frameAxis);
            if (cornerPos == null) {
                // 如果找不到角落，使用备用方法：直接清除附近的传送门方块
                clearNearbyPortalBlocks(level, portalPos);
                return;
            }

            PortalDimensions dimensions = calculatePortalDimensions(level, cornerPos, frameAxis);
            Direction right = getPositiveDirection(frameAxis);

            // 破坏传送门内部的所有方块
            // 增加容错范围，确保完全清除
            int portalWidth = Math.max(1, dimensions.width - 2);
            int portalHeight = Math.max(1, dimensions.height - 2);

            for (int x = 1; x <= portalWidth + 1; x++) {
                for (int y = 1; y <= portalHeight + 1; y++) {
                    BlockPos destroyPos = cornerPos.relative(right, x).above(y);
                    if (level.getBlockState(destroyPos).is(ModBlocks.VOID_PORTAL.get())) {
                        level.destroyBlock(destroyPos, false); // false 表示不掉落物品
                    }
                }
            }
        } catch (Exception e) {
            // 忽略错误
        }
    }

    /**
     * 清除附近的传送门方块
     * 当无法确定准确的传送门结构时使用此方法
     *
     * @param level 世界对象
     * @param centerPos 中心位置
     */
    private void clearNearbyPortalBlocks(Level level, BlockPos centerPos) {
        // 在 11x11x11 范围内清除所有传送门方块
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                for (int dz = -5; dz <= 5; dz++) {
                    BlockPos checkPos = centerPos.offset(dx, dy, dz);
                    BlockState checkState = level.getBlockState(checkPos);

                    if (checkState.is(ModBlocks.VOID_PORTAL.get())) {
                        level.destroyBlock(checkPos, false);
                    }
                }
            }
        }
    }

    /**
     * 检查指定位置是否为传送门框架方块或传送门方块
     *
     * @param level 世界对象
     * @param pos 待检测的位置
     * @return 如果是框架方块或传送门方块返回 true，否则返回 false
     */
    private boolean isFrameOrPortalBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(ModBlocks.NAUGHT_STONE.get()) || state.is(ModBlocks.VOID_PORTAL.get());
    }

    /**
     * 获取指定轴向的正方向
     * X 轴对应东方，Z 轴对应南方
     *
     * @param axis 框架轴向
     * @return 对应的正方向（EAST 或 SOUTH）
     */
    private Direction getPositiveDirection(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
    }

    /**
     * 获取指定轴向的负方向
     * X 轴对应西方，Z 轴对应北方
     *
     * @param axis 框架轴向
     * @return 对应的负方向（WEST 或 NORTH）
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
