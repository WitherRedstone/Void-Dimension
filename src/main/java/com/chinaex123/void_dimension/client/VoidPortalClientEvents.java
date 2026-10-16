package com.chinaex123.void_dimension.client;

import com.chinaex123.void_dimension.VoidDimension;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * 虚空传送门客户端事件处理。
 * <p>
 * 负责推进计时器、在玩家退出世界时重置状态。
 */
@EventBusSubscriber(modid = VoidDimension.MOD_ID, value = Dist.CLIENT)
public class VoidPortalClientEvents {

    /**
     * 处理客户端刻事件。
     * <p>
     * 每个客户端 tick 推进一次客户端状态计时器。
     *
     * @param event 客户端刻事件
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        VoidPortalClientState.tick();
    }

    /**
     * 处理玩家退出世界事件。
     * <p>
     * 玩家退出世界 / 断开连接时重置状态，避免残留强度。
     *
     * @param event 玩家退出事件
     */
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        VoidPortalClientState.reset();
    }
}