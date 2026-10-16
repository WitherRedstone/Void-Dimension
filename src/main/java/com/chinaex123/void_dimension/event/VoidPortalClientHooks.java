package com.chinaex123.void_dimension.event;

import com.chinaex123.void_dimension.client.portal.VoidPortalClientState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * 虚空传送门客户端钩子。
 * <p>
 * 用于在仅服务端可用的逻辑中安全地通知客户端状态，
 * 通过环境判定与内部类隔离，避免服务端加载时触碰客户端专用类。
 */
public final class VoidPortalClientHooks {

    private VoidPortalClientHooks() {}

    /**
     * 通知客户端玩家进入了传送门。
     * <p>
     * 仅在客户端环境下真正执行，服务端调用时直接跳过，
     * 以保证同一段逻辑在两端都能安全调用。
     */
    public static void onPlayerInsidePortalClient() {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            ClientOnly.markInsidePortal();
        }
    }

    /**
     * 客户端专用实现，单独类隔离，避免服务端类加载时触碰 Minecraft 类。
     */
    private static final class ClientOnly {
        static void markInsidePortal() {
            VoidPortalClientState.markInsidePortal();
        }
    }
}