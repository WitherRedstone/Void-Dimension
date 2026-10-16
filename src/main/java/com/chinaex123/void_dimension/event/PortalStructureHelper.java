package com.chinaex123.void_dimension.event;

import com.chinaex123.void_dimension.block.VoidPortal;
import com.chinaex123.void_dimension.init.VDBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.chinaex123.void_dimension.event.PortalFrameHelper.PortalDimensions;

/**
 * 传送门结构工具类。
 * <p>
 * 负责传送门结构的校验、生成与破坏，包括尺寸判定、框架完整性检查、
 * 内部空间检查、建造下界风格传送门与简易返回传送门，以及清除相关传送门方块。
 */
public final class PortalStructureHelper {

    private PortalStructureHelper() {}

    /**
     * 判断传送门尺寸是否满足最小要求。
     * <p>
     * 传入的最小宽高指内部空间的尺寸，因此框架实际尺寸需要额外加 2（两侧框架）。
     *
     * @param dimensions 框架尺寸
     * @param minWidth   内部最小宽度
     * @param minHeight  内部最小高度
     * @return 若尺寸达标则返回 true
     */
    public static boolean isValidPortalSize(PortalDimensions dimensions, int minWidth, int minHeight) {
        return dimensions.width() >= minWidth + 2 && dimensions.height() >= minHeight + 2;
    }

    /**
     * 判断传送门结构是否有效。
     * <p>
     * 要求框架完整且内部为空。
     *
     * @param level     所在维度
     * @param minCorner 框架左下角
     * @param dimensions 框架尺寸
     * @param axis      框架轴向
     * @return 若结构有效则返回 true
     */
    public static boolean isPortalStructureValid(Level level, BlockPos minCorner,
                                                 PortalDimensions dimensions, Direction.Axis axis) {
        return isFrameComplete(level, minCorner, dimensions, axis)
                && isInteriorEmpty(level, minCorner, dimensions, axis);
    }

    /**
     * 判断框架是否完整。
     * <p>
     * 依次检查底部横边、顶部横边、左侧竖边与右侧竖边是否全部由框架方块组成。
     *
     * @param level      所在维度
     * @param minCorner  框架左下角
     * @param dimensions 框架尺寸
     * @param axis       框架轴向
     * @return 若框架完整则返回 true
     */
    public static boolean isFrameComplete(Level level, BlockPos minCorner,
                                          PortalDimensions dimensions, Direction.Axis axis) {
        Direction right = PortalFrameHelper.getPositiveDirection(axis);

        if (!isHorizontalFrameComplete(level, minCorner, dimensions.width(), right)) return false;

        BlockPos topCorner = minCorner.above(dimensions.height() - 1);
        if (!isHorizontalFrameComplete(level, topCorner, dimensions.width(), right)) return false;

        if (!isVerticalFrameComplete(level, minCorner, dimensions.height())) return false;

        BlockPos rightCorner = minCorner.relative(right, dimensions.width() - 1);
        return isVerticalFrameComplete(level, rightCorner, dimensions.height());
    }

    /**
     * 判断某条水平边是否全部由框架方块组成。
     *
     * @param level     所在维度
     * @param start     起始位置
     * @param width     边的长度
     * @param direction 延伸方向
     * @return 若全部为框架方块则返回 true
     */
    private static boolean isHorizontalFrameComplete(Level level, BlockPos start, int width, Direction direction) {
        for (int i = 0; i < width; i++) {
            if (!PortalFrameHelper.isFrameBlock(level, start.relative(direction, i))) return false;
        }
        return true;
    }

    /**
     * 判断某条竖直边是否全部由框架方块组成。
     *
     * @param level  所在维度
     * @param start  起始位置
     * @param height 边的长度
     * @return 若全部为框架方块则返回 true
     */
    private static boolean isVerticalFrameComplete(Level level, BlockPos start, int height) {
        for (int i = 0; i < height; i++) {
            if (!PortalFrameHelper.isFrameBlock(level, start.above(i))) return false;
        }
        return true;
    }

    /**
     * 判断传送门内部空间是否为空。
     * <p>
     * 遍历框架内部（去除四周边框）的每一个方块，要求全部为空。
     *
     * @param level      所在维度
     * @param minCorner  框架左下角
     * @param dimensions 框架尺寸
     * @param axis       框架轴向
     * @return 若内部全空则返回 true
     */
    public static boolean isInteriorEmpty(Level level, BlockPos minCorner,
                                          PortalDimensions dimensions, Direction.Axis axis) {
        Direction right = PortalFrameHelper.getPositiveDirection(axis);
        int interiorWidth = dimensions.width() - 2;
        int interiorHeight = dimensions.height() - 2;

        for (int x = 1; x <= interiorWidth; x++) {
            for (int y = 1; y <= interiorHeight; y++) {
                if (!level.isEmptyBlock(minCorner.relative(right, x).above(y))) return false;
            }
        }
        return true;
    }

    /**
     * 在已有框架内填充传送门方块。
     * <p>
     * 根据框架尺寸计算内部区域，并在其中放置对应轴向的传送门方块。
     *
     * @param level      所在维度
     * @param minCorner  框架左下角
     * @param dimensions 框架尺寸
     * @param axis       框架轴向
     */
    public static void createPortalStructure(Level level, BlockPos minCorner,
                                             PortalDimensions dimensions, Direction.Axis axis) {
        Block portalBlock = VDBlocks.VOID_PORTAL.get();
        Direction.Axis portalAxis = PortalFrameHelper.frameAxisOf(axis);

        int portalWidth = dimensions.width() - 2;
        int portalHeight = dimensions.height() - 2;
        Direction right = PortalFrameHelper.getPositiveDirection(axis);

        for (int x = 1; x <= portalWidth; x++) {
            for (int y = 1; y <= portalHeight; y++) {
                BlockPos portalPos = minCorner.relative(right, x).above(y);
                BlockState portalState = portalBlock.defaultBlockState().setValue(VoidPortal.AXIS, portalAxis);
                level.setBlock(portalPos, portalState, 3);
            }
        }
    }

    /**
     * 建造下界风格传送门（4x5 含内部 2x3）。
     * <p>
     * 先清空目标区域，再搭建框架并填充传送门方块，整体位于 Z 轴平面。
     *
     * @param level 所在维度
     * @param pos   传送门左下角位置
     */
    public static void buildNetherStylePortal(Level level, BlockPos pos) {
        Block frameBlock = VDBlocks.NAUGHT_STONE.get();
        Block portalBlock = VDBlocks.VOID_PORTAL.get();

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 5; y++) {
                level.setBlock(pos.offset(x, y, 0), Blocks.AIR.defaultBlockState(), 3);
            }
        }

        for (int x = 0; x < 4; x++) {
            level.setBlock(pos.offset(x, 0, 0), frameBlock.defaultBlockState(), 3);
        }
        for (int x = 0; x < 4; x++) {
            level.setBlock(pos.offset(x, 0, -1), frameBlock.defaultBlockState(), 3);
        }
        for (int x = 0; x < 4; x++) {
            level.setBlock(pos.offset(x, 0, 1), frameBlock.defaultBlockState(), 3);
        }
        for (int x = 0; x < 4; x++) {
            level.setBlock(pos.offset(x, 4, 0), frameBlock.defaultBlockState(), 3);
        }
        for (int y = 1; y < 4; y++) {
            level.setBlock(pos.offset(0, y, 0), frameBlock.defaultBlockState(), 3);
            level.setBlock(pos.offset(3, y, 0), frameBlock.defaultBlockState(), 3);
        }

        for (int x = 1; x < 3; x++) {
            for (int y = 1; y < 4; y++) {
                BlockState portalState = portalBlock.defaultBlockState().setValue(VoidPortal.AXIS, Direction.Axis.Z);
                level.setBlock(pos.offset(x, y, 0), portalState, 3);
            }
        }
    }

    /**
     * 建造简易返回传送门。
     * <p>
     * 结构为宽 2 格、高 5 格，内部为 2x2 的传送门区域，朝向沿 Z 轴。
     *
     * @param level 所在维度
     * @param pos   传送门左下角位置
     */
    public static void buildSimpleReturnPortal(Level level, BlockPos pos) {
        Block frameBlock = VDBlocks.NAUGHT_STONE.get();
        Block portalBlock = VDBlocks.VOID_PORTAL.get();

        for (int y = 0; y < 5; y++) {
            level.setBlock(pos.above(y), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(pos.east().above(y), Blocks.AIR.defaultBlockState(), 3);
        }

        level.setBlock(pos, frameBlock.defaultBlockState(), 3);
        level.setBlock(pos.east(), frameBlock.defaultBlockState(), 3);

        for (int y = 1; y < 4; y++) {
            level.setBlock(pos.above(y), frameBlock.defaultBlockState(), 3);
            level.setBlock(pos.east().above(y), frameBlock.defaultBlockState(), 3);
        }

        level.setBlock(pos.above(4), frameBlock.defaultBlockState(), 3);
        level.setBlock(pos.east().above(4), frameBlock.defaultBlockState(), 3);

        BlockState portalState = portalBlock.defaultBlockState().setValue(VoidPortal.AXIS, Direction.Axis.Z);
        level.setBlock(pos.above(1), portalState, 3);
        level.setBlock(pos.above(2), portalState, 3);
        level.setBlock(pos.east().above(1), portalState, 3);
        level.setBlock(pos.east().above(2), portalState, 3);
    }

    /**
     * 破坏整个传送门结构。
     * <p>
     * 先定位框架左下角并计算尺寸，然后销毁内部所有传送门方块。
     * 若无法定位角落，则退回到清除附近的传送门方块。
     *
     * @param level     所在维度
     * @param portalPos 传送门内任一位置
     * @param frameAxis 框架轴向
     */
    public static void destroyPortalStructure(Level level, BlockPos portalPos, Direction.Axis frameAxis) {
        try {
            BlockPos cornerPos = PortalFrameHelper.findPortalCornerImproved(level, portalPos, frameAxis);
            if (cornerPos == null) {
                clearNearbyPortalBlocks(level, portalPos);
                return;
            }

            PortalDimensions dimensions = PortalFrameHelper.calculatePortalDimensions(level, cornerPos, frameAxis);
            Direction right = PortalFrameHelper.getPositiveDirection(frameAxis);

            int portalWidth = Math.max(1, dimensions.width() - 2);
            int portalHeight = Math.max(1, dimensions.height() - 2);

            for (int x = 1; x <= portalWidth + 1; x++) {
                for (int y = 1; y <= portalHeight + 1; y++) {
                    BlockPos destroyPos = cornerPos.relative(right, x).above(y);
                    if (level.getBlockState(destroyPos).is(VDBlocks.VOID_PORTAL.get())) {
                        level.destroyBlock(destroyPos, false);
                    }
                }
            }
        } catch (Exception e) {
            // 忽略
        }
    }

    /**
     * 在 11x11x11 范围内清除所有传送门方块。
     * <p>
     * 以指定位置为中心，遍历周围 5 格范围内的所有方块，
     * 销毁其中所有的传送门方块。
     *
     * @param level     所在维度
     * @param centerPos 中心位置
     */
    public static void clearNearbyPortalBlocks(Level level, BlockPos centerPos) {
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                for (int dz = -5; dz <= 5; dz++) {
                    BlockPos checkPos = centerPos.offset(dx, dy, dz);
                    if (level.getBlockState(checkPos).is(VDBlocks.VOID_PORTAL.get())) {
                        level.destroyBlock(checkPos, false);
                    }
                }
            }
        }
    }

    /**
     * 查找并破坏与被破坏方块相关的传送门。
     * <p>
     * 先在相邻方块中查找传送门，找到后销毁对应结构；
     * 若相邻处没有，则在 5x5x5 范围内扩大搜索并销毁找到的第一座传送门。
     *
     * @param level     所在维度
     * @param brokenPos 被破坏方块的位置
     */
    public static void destroyRelatedPortals(Level level, BlockPos brokenPos) {
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = brokenPos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            if (adjacentState.is(VDBlocks.VOID_PORTAL.get())) {
                try {
                    Direction.Axis portalAxis = adjacentState.getValue(VoidPortal.AXIS);
                    Direction.Axis frameAxis = PortalFrameHelper.frameAxisOf(portalAxis);
                    destroyPortalStructure(level, adjacentPos, frameAxis);
                    return;
                } catch (Exception e) {
                    // 忽略
                }
            }
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos checkPos = brokenPos.offset(dx, dy, dz);
                    BlockState checkState = level.getBlockState(checkPos);

                    if (checkState.is(VDBlocks.VOID_PORTAL.get())) {
                        try {
                            Direction.Axis portalAxis = checkState.getValue(VoidPortal.AXIS);
                            Direction.Axis frameAxis = PortalFrameHelper.frameAxisOf(portalAxis);
                            destroyPortalStructure(level, checkPos, frameAxis);
                            return;
                        } catch (Exception e) {
                            // 忽略
                        }
                    }
                }
            }
        }
    }
}