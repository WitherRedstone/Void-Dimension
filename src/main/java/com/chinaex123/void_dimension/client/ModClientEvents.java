package com.chinaex123.void_dimension.client;

import com.chinaex123.void_dimension.VoidDimension;
import com.chinaex123.void_dimension.client.portal.VoidPortalClientEvents;
import com.chinaex123.void_dimension.client.portal.VoidPortalOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * 模组客户端事件统一入口。
 * <p>
 * 所有客户端侧事件在此集中注册，具体逻辑委托给对应的功能类处理。
 */
@EventBusSubscriber(modid = VoidDimension.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents {

    /**
     * 处理客户端刻事件。
     * <p>
     * 委托给虚空传送门客户端事件处理，推进其计时逻辑。
     *
     * @param event 客户端刻事件
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        VoidPortalClientEvents.tick();
    }

    /**
     * 处理玩家退出世界事件。
     * <p>
     * 委托给虚空传送门客户端事件处理，重置其状态。
     *
     * @param event 玩家退出事件
     */
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        VoidPortalClientEvents.reset();
    }

    /**
     * 处理 GUI 图层注册事件。
     * <p>
     * 注册虚空传送门遮罩层。
     *
     * @param event GUI 图层注册事件
     */
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        VoidPortalOverlay.register(event);
    }
}