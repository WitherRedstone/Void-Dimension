package com.chinaex123.void_dimension.event;

import com.chinaex123.void_dimension.block.VoidPortal;
import com.chinaex123.void_dimension.init.VDBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 虚空传送门框架辅助工具。
 * <p>
 * 提供方向推导、框架/传送门判定、角落查找、尺寸计算与边缘判定等方法。
 */
public final class PortalFrameHelper {

    private PortalFrameHelper() {}


    /**
     * 获取指定轴向的正方向。
     *
     * @param axis 轴向
     * @return X 轴返回 EAST，Z 轴返回 SOUTH
     */
    public static Direction getPositiveDirection(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
    }

    /**
     * 获取指定轴向的负方向。
     *
     * @param axis 轴向
     * @return X 轴返回 WEST，Z 轴返回 NORTH
     */
    public static Direction getNegativeDirection(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.WEST : Direction.NORTH;
    }

    /**
     * 由传送门轴向推导框架轴向（两者互相垂直）。
     *
     * @param portalAxis 传送门轴向
     * @return 与传送门轴向垂直的框架轴向
     */
    public static Direction.Axis frameAxisOf(Direction.Axis portalAxis) {
        return portalAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
    }

    /**
     * 获取相对位置在指定方向上的坐标值。
     *
     * @param relativePos 相对位置
     * @param direction   方向
     * @return 对应方向上的坐标值，未知方向返回 0
     */
    public static int getRelativeCoordinate(BlockPos relativePos, Direction direction) {
        return switch (direction) {
            case EAST -> relativePos.getX();
            case SOUTH -> relativePos.getZ();
            default -> 0;
        };
    }

    /**
     * 判断指定位置是否为框架方块或传送门方块。
     *
     * @param level 所在维度
     * @param pos   待判定的位置
     * @return 若为虚空石或传送门方块则返回 true
     */
    public static boolean isFrameOrPortalBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(VDBlocks.NAUGHT_STONE.get()) || state.is(VDBlocks.VOID_PORTAL.get());
    }

    /**
     * 判断指定位置是否为框架方块（虚空石）。
     *
     * @param level 所在维度
     * @param pos   待判定的位置
     * @return 若为虚空石则返回 true
     */
    public static boolean isFrameBlock(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(VDBlocks.NAUGHT_STONE.get());
    }

    /**
     * 判断指定位置是否为相同轴向的传送门方块。
     *
     * @param level        所在维度
     * @param pos          待判定的位置
     * @param expectedAxis 期望的框架轴向
     * @return 若为同轴向传送门方块则返回 true
     */
    public static boolean isSameAxisPortal(Level level, BlockPos pos, Direction.Axis expectedAxis) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(VDBlocks.VOID_PORTAL.get())) {
            return false;
        }
        try {
            Direction.Axis portalAxis = state.getValue(VoidPortal.AXIS);
            return frameAxisOf(portalAxis) == expectedAxis;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 查找传送门框架左下角（基础版本，仅识别框架方块）。
     * <p>
     * 用于点灯时的严格结构检测。
     *
     * @param level     所在维度
     * @param portalPos 传送门位置
     * @param frameAxis 框架轴向
     * @return 框架左下角的位置
     */
    public static BlockPos findPortalCorner(Level level, BlockPos portalPos, Direction.Axis frameAxis) {
        BlockPos pos = portalPos.immutable();
        Direction negativeDir = getNegativeDirection(frameAxis);

        while (isFrameBlock(level, pos.relative(negativeDir))) {
            pos = pos.relative(negativeDir);
        }
        while (isFrameBlock(level, pos.below())) {
            pos = pos.below();
        }
        return pos;
    }

    /**
     * 查找传送门框架左下角（破坏事件版本，同时识别同轴向传送门方块）。
     *
     * @param level     所在维度
     * @param portalPos 传送门位置
     * @param frameAxis 框架轴向
     * @return 框架左下角的位置
     */
    public static BlockPos findPortalCornerWithPortal(Level level, BlockPos portalPos, Direction.Axis frameAxis) {
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
     * 改进版角落查找。
     * <p>
     * 依次尝试常规查找、向下扫描、区域扫描，返回首个有效的角落位置。
     *
     * @param level     所在维度
     * @param portalPos 传送门位置
     * @param frameAxis 框架轴向
     * @return 有效的框架左下角位置，均失败时返回区域扫描结果（可能为 null）
     */
    public static BlockPos findPortalCornerImproved(Level level, BlockPos portalPos, Direction.Axis frameAxis) {
        BlockPos cornerPos = findPortalCornerWithPortal(level, portalPos, frameAxis);
        if (cornerPos != null && isValidCorner(level, cornerPos, frameAxis)) {
            return cornerPos;
        }

        cornerPos = findCornerByScanningDown(level, portalPos, frameAxis);
        if (cornerPos != null && isValidCorner(level, cornerPos, frameAxis)) {
            return cornerPos;
        }

        return findCornerByScanningArea(level, portalPos, frameAxis);
    }

    /**
     * 验证角落是否有效。
     * <p>
     * 要求角落的右侧或上方至少存在一个框架 / 传送门方块。
     *
     * @param level     所在维度
     * @param cornerPos 待验证的角落位置
     * @param frameAxis 框架轴向
     * @return 若角落有效则返回 true
     */
    public static boolean isValidCorner(Level level, BlockPos cornerPos, Direction.Axis frameAxis) {
        Direction right = getPositiveDirection(frameAxis);
        boolean hasFrameToRight = isFrameOrPortalBlock(level, cornerPos.relative(right));
        boolean hasFrameAbove = isFrameOrPortalBlock(level, cornerPos.above());
        return hasFrameToRight || hasFrameAbove;
    }

    /**
     * 向下扫描查找角落。
     * <p>
     * 先向下移动到框架底部，再沿负方向移动到框架左边缘。
     *
     * @param level     所在维度
     * @param portalPos 传送门位置
     * @param frameAxis 框架轴向
     * @return 扫描得到的角落位置
     */
    public static BlockPos findCornerByScanningDown(Level level, BlockPos portalPos, Direction.Axis frameAxis) {
        BlockPos pos = portalPos.immutable();

        while (!isFrameOrPortalBlock(level, pos.below()) && pos.getY() > level.getMinY()) {
            pos = pos.below();
        }

        Direction negativeDir = getNegativeDirection(frameAxis);
        while (isFrameOrPortalBlock(level, pos.relative(negativeDir))) {
            pos = pos.relative(negativeDir);
        }
        return pos;
    }

    /**
     * 区域扫描查找角落。
     * <p>
     * 交替沿负方向与向下移动，最多迭代 10 次，用于处理结构不规则的框架。
     *
     * @param level     所在维度
     * @param portalPos 传送门位置
     * @param frameAxis 框架轴向
     * @return 扫描得到的角落位置，未找到时返回 null
     */
    public static BlockPos findCornerByScanningArea(Level level, BlockPos portalPos, Direction.Axis frameAxis) {
        Direction negativeDir = getNegativeDirection(frameAxis);
        BlockPos currentPos = portalPos.immutable();

        for (int i = 0; i < 10; i++) {
            while (isFrameOrPortalBlock(level, currentPos.relative(negativeDir))) {
                currentPos = currentPos.relative(negativeDir);
            }
            while (isFrameOrPortalBlock(level, currentPos.below())) {
                currentPos = currentPos.below();
            }
            if (isFrameOrPortalBlock(level, currentPos)) {
                return currentPos;
            }
        }
        return null;
    }

    /**
     * 计算框架尺寸（破坏事件版本，含同轴向传送门）。
     *
     * @param level     所在维度
     * @param cornerPos 框架左下角位置
     * @param frameAxis 框架轴向
     * @return 框架的宽高数据
     */
    public static PortalDimensions calculatePortalDimensions(Level level, BlockPos cornerPos, Direction.Axis frameAxis) {
        int height = countFrameBlocks(level, cornerPos, Direction.UP, frameAxis);
        int width = countFrameBlocks(level, cornerPos, getPositiveDirection(frameAxis), frameAxis);
        return new PortalDimensions(width, height);
    }

    /**
     * 统计指定方向上的框架 / 传送门方块数量（含起始位置）。
     *
     * @param level     所在维度
     * @param start     起始位置
     * @param direction 统计方向
     * @param frameAxis 框架轴向
     * @return 连续方块的数量
     */
    public static int countFrameBlocks(Level level, BlockPos start, Direction direction, Direction.Axis frameAxis) {
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
     * 基础版边缘判定（严格，用小于）。
     * <p>
     * 判断指定方块是否恰好位于框架边缘（上下左右四条边之一）。
     *
     * @param level     所在维度
     * @param framePos  待判定的方块位置
     * @param portalPos 传送门位置
     * @param frameAxis 框架轴向
     * @return 若位于框架边缘则返回 true
     */
    public static boolean isOnPortalFrameEdge(Level level, BlockPos framePos, BlockPos portalPos, Direction.Axis frameAxis) {
        try {
            BlockPos cornerPos = findPortalCornerWithPortal(level, portalPos, frameAxis);
            if (cornerPos == null) return false;

            PortalDimensions dimensions = calculatePortalDimensions(level, cornerPos, frameAxis);
            Direction right = getPositiveDirection(frameAxis);
            BlockPos relativePos = framePos.subtract(cornerPos);

            int relX = getRelativeCoordinate(relativePos, right);
            int relY = relativePos.getY();

            if (relY == 0 && relX >= 0 && relX < dimensions.width()) return true;
            if (relY == dimensions.height() - 1 && relX >= 0 && relX < dimensions.width()) return true;
            if (relX == 0 && relY >= 0 && relY < dimensions.height()) return true;
            return relX == dimensions.width() - 1 && relY >= 0 && relY < dimensions.height();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 改进版边缘判定（放宽，用小于等于，支持不完整框架）。
     * <p>
     * 相比基础版，允许坐标等于宽高值，从而兼容尺寸计算不够精确的情况。
     *
     * @param level     所在维度
     * @param framePos  待判定的方块位置
     * @param portalPos 传送门位置
     * @param frameAxis 框架轴向
     * @return 若位于框架边缘则返回 true
     */
    public static boolean isOnPortalFrameEdgeImproved(Level level, BlockPos framePos, BlockPos portalPos, Direction.Axis frameAxis) {
        try {
            BlockPos cornerPos = findPortalCornerImproved(level, portalPos, frameAxis);
            if (cornerPos == null) return false;

            PortalDimensions dimensions = calculatePortalDimensions(level, cornerPos, frameAxis);
            Direction right = getPositiveDirection(frameAxis);
            BlockPos relativePos = framePos.subtract(cornerPos);

            int relX = getRelativeCoordinate(relativePos, right);
            int relY = relativePos.getY();

            if (relY == 0 && relX >= 0 && relX <= dimensions.width()) return true;
            if (relY == dimensions.height() - 1 && relX >= 0 && relX <= dimensions.width()) return true;
            if (relX == 0 && relY >= 0 && relY <= dimensions.height()) return true;
            return relX == dimensions.width() - 1 && relY >= 0 && relY <= dimensions.height();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断被破坏的方块是否为传送门框架的关键方块。
     * <p>
     * 只有破坏关键框架方块才会触发整个传送门的破坏。
     * 处理两种情况：
     * 1. 常规情况：被破坏方块旁边就有传送门方块，直接判断它是否位于框架边缘；
     * 2. 框架不完整情况：被破坏方块附近有其他框架方块，则扩大搜索范围再判断。
     *
     * @param level 世界对象
     * @param pos   被破坏方块的位置
     * @return 如果为关键框架方块返回 true，否则返回 false
     */
    public static boolean isCriticalFrameBlock(Level level, BlockPos pos) {
        // 检查与传送门直接相邻的框架方块
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            if (adjacentState.is(VDBlocks.VOID_PORTAL.get())) {
                try {
                    Direction.Axis portalAxis = adjacentState.getValue(VoidPortal.AXIS);
                    Direction.Axis frameAxis = frameAxisOf(portalAxis);

                    if (isOnPortalFrameEdge(level, pos, adjacentPos, frameAxis)) {
                        return true;
                    }
                } catch (Exception e) {
                    // 忽略异常
                }
            }
        }

        // 适用于框架不完整的情况
        if (hasNearbyFrameBlocks(level, pos)) {
            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = pos.relative(direction);
                BlockState adjacentState = level.getBlockState(adjacentPos);

                if (adjacentState.is(VDBlocks.NAUGHT_STONE.get())) {
                    for (Direction adjDir : Direction.values()) {
                        BlockPos portalCheckPos = adjacentPos.relative(adjDir);
                        BlockState portalState = level.getBlockState(portalCheckPos);

                        if (portalState.is(VDBlocks.VOID_PORTAL.get())) {
                            try {
                                Direction.Axis portalAxis = portalState.getValue(VoidPortal.AXIS);
                                Direction.Axis frameAxis = frameAxisOf(portalAxis);

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
     * 检查附近是否有其他框架方块。
     *
     * @param level 世界对象
     * @param pos   检测位置
     * @return 是否有至少 2 个相邻框架方块
     */
    public static boolean hasNearbyFrameBlocks(Level level, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).is(VDBlocks.NAUGHT_STONE.get())) {
                count++;
            }
        }
        return count >= 2;
    }

    /**
     * 传送门尺寸数据类。
     *
     * @param width  框架宽度
     * @param height 框架高度
     */
    public record PortalDimensions(int width, int height) {}
}