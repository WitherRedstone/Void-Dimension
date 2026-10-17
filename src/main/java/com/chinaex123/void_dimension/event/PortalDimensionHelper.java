package com.chinaex123.void_dimension.event;

import com.chinaex123.void_dimension.init.VDBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * 跨维度定位 / 传送工具类。
 * <p>
 * 提供在目标维度查找或创建传送门，以及执行玩家跨维度传送的静态方法。
 */
public final class PortalDimensionHelper {

    private PortalDimensionHelper() {}

    /**
     * 查找或创建目标维度的传送门。
     * <p>
     * 先在目标坐标附近搜索已有传送门，若不存在则在合适的地面高度上新建一座。
     *
     * @param level     目标维度
     * @param x         目标 X 坐标
     * @param y         目标 Y 坐标
     * @param z         目标 Z 坐标
     * @param originalY 原始 Y 坐标，用于决定新建传送门的高度
     * @return 目标传送门的位置
     */
    public static BlockPos findOrCreatePortalInDimension(Level level, double x, double y, double z, double originalY) {
        BlockPos targetPos = BlockPos.containing(x, y, z);
        BlockPos existingPortal = findNearbyPortal(level, targetPos, 16);
        if (existingPortal != null) return existingPortal;
        return createPortalInDimension(level, targetPos, originalY);
    }

    /**
     * 在指定中心点附近搜索传送门方块。
     *
     * @param level  所在维度
     * @param center 搜索中心
     * @param radius 水平搜索半径（垂直方向为半径的一半）
     * @return 找到的传送门位置，未找到则返回 null
     */
    public static BlockPos findNearbyPortal(Level level, BlockPos center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -radius / 2; dy <= radius / 2; dy++) {
                    BlockPos checkPos = center.offset(dx, dy, dz);
                    if (level.getBlockState(checkPos).is(VDBlocks.VOID_PORTAL.get())) {
                        return checkPos;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 在目标维度新建一座传送门。
     * <p>
     * 先根据原始 Y 坐标确定合适的地面高度，再在该位置构建传送门结构。
     *
     * @param level     目标维度
     * @param pos       目标水平坐标
     * @param originalY 原始 Y 坐标
     * @return 新建传送门的位置（地面之上）
     */
    private static BlockPos createPortalInDimension(Level level, BlockPos pos, double originalY) {
        BlockPos groundPos = findSuitableGroundWithHeight(level, pos, originalY);
        PortalStructureHelper.buildNetherStylePortal(level, groundPos);
        return groundPos.above();
    }

    /**
     * 根据原始 Y 坐标计算合适的地面高度。
     * <p>
     * 结果会被限制在维度高度范围内，避免超出世界边界。
     *
     * @param level     所在维度
     * @param pos       水平坐标
     * @param originalY 原始 Y 坐标
     * @return 计算后的地面位置
     */
    private static BlockPos findSuitableGroundWithHeight(Level level, BlockPos pos, double originalY) {
        int targetY = (int) Math.floor(originalY);
        targetY = Math.max(level.getMinBuildHeight() + 3, targetY);
        targetY = Math.min(level.getMaxBuildHeight() - 5, targetY);
        return new BlockPos(pos.getX(), targetY, pos.getZ());
    }

    /**
     * 传送玩家到虚空维度。
     * <p>
     * 传送完成后在目标位置附近尝试创建一座返回传送门，
     * 以便玩家能够返回原维度。
     *
     * @param player 被传送的玩家
     */
    public static void teleportToVoidDimension(ServerPlayer player) {
        ResourceKey<Level> originalDimension = player.level().dimension();

        player.teleportTo(
                Objects.requireNonNull(player.server.getLevel(PortalTeleportManager.VOID_DIMENSION_KEY)),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot()
        );

        Objects.requireNonNull(player.getServer()).execute(() ->
                createReturnPortalAtCurrentPosition(player, originalDimension));
    }

    /**
     * 在玩家当前位置附近创建返回传送门。
     * <p>
     * 优先尝试在玩家面向方向前方放置传送门，若空间不足则退回到玩家所在位置。
     *
     * @param player          玩家
     * @param returnDimension 返回的目标维度（仅作记录用途）
     */
    private static void createReturnPortalAtCurrentPosition(ServerPlayer player, ResourceKey<Level> returnDimension) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();
        BlockPos portalPos = playerPos.relative(player.getDirection(), 3).above();

        if (isAreaClearForPortal(level, portalPos)) {
            PortalStructureHelper.buildSimpleReturnPortal(level, portalPos);
        } else {
            portalPos = playerPos;
            if (isAreaClearForPortal(level, portalPos)) {
                PortalStructureHelper.buildSimpleReturnPortal(level, portalPos);
            }
        }
    }

    /**
     * 判断指定位置是否有足够空间放置传送门。
     * <p>
     * 检查以该位置为起点、宽 2 格、高 5 格的区域是否全部为空。
     *
     * @param level 所在维度
     * @param pos   待检查的位置
     * @return 若空间足够则返回 true，否则返回 false
     */
    private static boolean isAreaClearForPortal(Level level, BlockPos pos) {
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 5; y++) {
                if (!level.isEmptyBlock(pos.offset(x, y, 0))) return false;
            }
        }
        return true;
    }

    /**
     * 传送玩家到指定维度。
     * <p>
     * 保持玩家当前的坐标与朝向，仅切换所在维度。
     *
     * @param player          被传送的玩家
     * @param targetDimension 目标维度
     */
    public static void teleportToDimension(ServerPlayer player, ResourceKey<Level> targetDimension) {
        player.teleportTo(
                Objects.requireNonNull(player.server.getLevel(targetDimension)),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot()
        );
    }
}