package com.chinaex123.void_dimension.event;

import com.chinaex123.void_dimension.VoidDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * 传送队列管理器。
 */
public final class PortalTeleportManager {

    private PortalTeleportManager() {}

    /** 虚空维度的资源键，用于定位目标维度 */
    public static final ResourceKey<Level> VOID_DIMENSION_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(VoidDimension.MOD_ID, "void")
    );

    /** 传送等待时间（tick） */
    public static final int PORTAL_WAIT_TICKS = 80;

    /** 传送完成后设置的冷却（tick） */
    private static final int PORTAL_COOLDOWN_AFTER = 200;

    /** 待传送玩家队列：UUID → 待传送数据 */
    private static final Map<UUID, PendingTeleport> PENDING = new HashMap<>();

    private record PendingTeleport(
            ServerPlayer player,
            BlockPos portalPos,
            ResourceKey<Level> targetDimension,
            BlockPos targetPos,
            int ticksLeft
    ) {}

    /**
     * 开始一次传送门传送。
     *
     * @param player    进入传送门的玩家
     * @param portalPos 进入的传送门位置
     */
    public static void beginPortalTeleport(ServerPlayer player, BlockPos portalPos) {
        UUID uuid = player.getUUID();

        if (PENDING.containsKey(uuid)) return;
        if (player.getPortalCooldown() > 0) return;

        Level currentLevel = player.level();
        ResourceKey<Level> currentDimension = currentLevel.dimension();
        ResourceKey<Level> targetDimension =
                (currentDimension == Level.OVERWORLD) ? VOID_DIMENSION_KEY : Level.OVERWORLD;

        ServerLevel targetLevel = player.level().getServer().getLevel(targetDimension);
        if (targetLevel == null) return;

        BlockPos targetPos = PortalDimensionHelper.findOrCreatePortalInDimension(
                targetLevel,
                portalPos.getX(), portalPos.getY(), portalPos.getZ(),
                player.getY()
        );

        player.level().playSound(null, portalPos,
                SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 1.0f, 1.0f);

        PENDING.put(uuid, new PendingTeleport(
                player,
                portalPos.immutable(),
                targetDimension,
                targetPos,
                PORTAL_WAIT_TICKS
        ));
    }

    /**
     * 推进传送队列。
     */
    public static void tick() {
        if (PENDING.isEmpty()) return;

        Iterator<Map.Entry<UUID, PendingTeleport>> iterator = PENDING.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PendingTeleport> entry = iterator.next();
            PendingTeleport pending = entry.getValue();
            ServerPlayer player = pending.player();

            if (player.hasDisconnected() || player.isRemoved()) {
                iterator.remove();
                continue;
            }

            int left = pending.ticksLeft() - 1;
            if (left > 0) {
                entry.setValue(new PendingTeleport(
                        player,
                        pending.portalPos(),
                        pending.targetDimension(),
                        pending.targetPos(),
                        left
                ));
                continue;
            }

            iterator.remove();
            executeDelayedTeleport(player, pending.targetDimension(), pending.targetPos());
        }
    }

    /**
     * 执行实际传送。
     *
     * @param player          待传送玩家
     * @param targetDimension 目标维度
     * @param targetPos       目标传送门位置
     */
    private static void executeDelayedTeleport(ServerPlayer player, ResourceKey<Level> targetDimension, BlockPos targetPos) {
        ServerLevel targetLevel = player.level().getServer().getLevel(targetDimension);
        if (targetLevel == null) return;

        player.teleportTo(
                Objects.requireNonNull(player.server.getLevel(targetDimension)),
                targetPos.getX() + 0.5,
                targetPos.getY() + 1,
                targetPos.getZ() + 0.5,
                player.getYRot(),
                player.getXRot()
        );

        player.setPortalCooldown(PORTAL_COOLDOWN_AFTER);
        PortalEffectHelper.playArrivalEffects(player, targetPos);
    }

    /**
     * 判断玩家是否正在等待传送。
     *
     * @param player 待查询的玩家
     * @return 若玩家在待传送队列中则返回 true
     */
    public static boolean isPending(ServerPlayer player) {
        return PENDING.containsKey(player.getUUID());
    }

    /**
     * 取消某玩家的传送等待。
     *
     * @param player 待取消的玩家
     */
    public static void cancel(ServerPlayer player) {
        PENDING.remove(player.getUUID());
    }

    /**
     * 清空所有等待。
     */
    public static void clearAll() {
        PENDING.clear();
    }
}