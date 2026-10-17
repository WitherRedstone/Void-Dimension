package com.chinaex123.void_dimension.client.portal;

/**
 * 虚空传送门客户端事件处理。
 * <p>
 * 负责推进计时器、在玩家退出世界时重置状态。
 */
public final class VoidPortalClientEvents {
    private VoidPortalClientEvents() {}

    /**
     * 推进客户端状态计时器。
     * <p>
     * 每个客户端 tick 调用一次。
     */
    public static void tick() {
        VoidPortalClientState.tick();
    }

    /**
     * 重置客户端状态。
     * <p>
     * 玩家退出世界 / 断开连接时调用。
     */
    public static void reset() {
        VoidPortalClientState.reset();
    }
}