package com.chinaex123.void_dimension.client;

import com.chinaex123.void_dimension.VoidDimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * 虚空传送门屏幕叠加层。
 * <p>
 * 在玩家进入传送门时，于屏幕最上层绘制一层逐渐增强的传送门贴图，
 * 用于模拟传送门的视觉覆盖效果。绘制强度由客户端状态计时器决定。
 */
@EventBusSubscriber(modid = VoidDimension.MOD_ID, value = Dist.CLIENT)
public class VoidPortalOverlay {

    /** 方块纹理图集的位置标识 */
    private static final Identifier BLOCKS_ATLAS = Identifier.withDefaultNamespace("textures/atlas/blocks.png");

    /** 传送门叠加层使用的贴图 */
    private static final Identifier PORTAL_SPRITE = Identifier.withDefaultNamespace("block/nether_portal");

    /**
     * 处理注册 GUI 图层事件。
     * <p>
     * 将传送门叠加层注册到所有图层之上，确保覆盖在其余界面之上绘制。
     *
     * @param event GUI 图层注册事件
     */
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(VoidDimension.MOD_ID, "portal_overlay"),
                (graphics, deltaTracker) -> render(graphics)
        );
    }

    /**
     * 渲染传送门叠加层。
     * <p>
     * 根据当前传送门强度决定叠加层透明度，并将传送门贴图铺满整个屏幕。
     * 若玩家不存在或强度为 0，则不进行绘制。
     *
     * @param graphics GUI 图形绘制上下文
     */
    private static void render(GuiGraphicsExtractor graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        float intensity = VoidPortalClientState.getIntensity();
        if (intensity <= 0.0f) return;

        var textureManager = mc.getTextureManager();
        if (!(textureManager.getTexture(BLOCKS_ATLAS) instanceof TextureAtlas atlas)) return;

        TextureAtlasSprite sprite = atlas.getSprite(PORTAL_SPRITE);

        int w = graphics.guiWidth();
        int h = graphics.guiHeight();

        int alpha = (int) (intensity * 0.75f * 255);
        int color = (alpha << 24) | 0xFFFFFF;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 0, 0, w, h, color);
    }
}