package com.chinaex123.void_dimension.client.portal;

import com.chinaex123.void_dimension.event.PortalTeleportManager;

/**
 * 虚空传送门客户端状态。
 * <p>
 * 记录玩家当前是否处于传送门内，以及传送门进度计时器，
 * 用于计算并驱动客户端侧的传送门视觉效果强度。
 */
public final class VoidPortalClientState {

    /** 当前 tick 玩家是否处于传送门内 */
    private static boolean insidePortalThisTick = false;

    /** 传送门进度计时器（单位：tick） */
    private static int portalTimer = 0;

    /** 计时器上升速度（进入传送门时每 tick 增加的量） */
    private static final int RISE_SPEED = 1;

    /** 计时器下降速度（离开传送门时每 tick 减少的量） */
    private static final int FALL_SPEED = 4;

    private VoidPortalClientState() {}

    /**
     * 标记当前 tick 玩家处于传送门内。
     * <p>
     * 由传送门检测逻辑每 tick 调用。
     */
    public static void markInsidePortal() {
        insidePortalThisTick = true;
    }

    /**
     * 推进客户端状态计时器。
     * <p>
     * 每个客户端 tick 调用一次：若玩家处于传送门内则按上升速度增加计时器，
     * 否则按下降速度减少计时器，并在处理完毕后清除本 tick 的标记。
     */
    public static void tick() {
        if (insidePortalThisTick) {
            portalTimer = Math.min(portalTimer + RISE_SPEED, PortalTeleportManager.PORTAL_WAIT_TICKS);
        } else {
            portalTimer = Math.max(portalTimer - FALL_SPEED, 0);
        }
        insidePortalThisTick = false;
    }

    /**
     * 获取传送门当前的视觉强度。
     *
     * @return 归一化后的强度值，范围为 0.0 到 1.0
     */
    public static float getIntensity() {
        return portalTimer / (float) PortalTeleportManager.PORTAL_WAIT_TICKS;
    }

    /**
     * 判断传送门是否处于激活状态。
     *
     * @return 若计时器大于 0 则返回 true
     */
    public static boolean isActive() {
        return portalTimer > 0;
    }

    /**
     * 重置客户端状态。
     * <p>
     * 玩家退出世界 / 断开连接时调用，清空计时器与标记，避免残留强度。
     */
    public static void reset() {
        insidePortalThisTick = false;
        portalTimer = 0;
    }
}