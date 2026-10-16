package com.chinaex123.void_dimension.event;

import com.chinaex123.void_dimension.VoidDimension;
import com.chinaex123.void_dimension.init.VDBlocks;
import com.chinaex123.void_dimension.init.VDItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 虚空传送门激活事件处理。
 * <p>
 * 负责处理玩家用虚空碎片右键虚空石框架点亮传送门的逻辑，
 * 并在每个服务器 tick 推进传送队列。
 */
@EventBusSubscriber(modid = VoidDimension.MOD_ID)
public class VoidPortalActivationEvent {

    /** 传送门内部最小宽度 */
    private static final int MIN_PORTAL_WIDTH = 2;

    /** 传送门内部最小高度 */
    private static final int MIN_PORTAL_HEIGHT = 3;

    /**
     * 处理玩家右键方块事件。
     * <p>
     * 当玩家主手持有虚空碎片并右键虚空石时，尝试点亮传送门。
     * 若成功点亮，则取消原版交互、播放挥动手臂动画，
     * 并在非创造模式下消耗一点耐久。
     *
     * @param event 玩家右键方块事件
     */
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide()) return;

        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!heldItem.is(VDItems.NAUGHT_SHARD.get())) return;

        BlockPos clickedPos = event.getPos();
        if (!level.getBlockState(clickedPos).is(VDBlocks.NAUGHT_STONE.get())) return;

        if (attemptToLightPortal(level, clickedPos, player, heldItem)) {
            event.setCanceled(true);
            player.swing(InteractionHand.MAIN_HAND, true);
        }
    }

    /**
     * 尝试点亮传送门。
     * <p>
     * 依次沿 X 轴与 Z 轴尝试点亮，任一成功即播放激活效果，
     * 并在非创造模式下消耗手持物品的耐久。
     *
     * @param level    所在维度
     * @param pos      点击的框架方块位置
     * @param player   操作的玩家
     * @param heldItem 手持物品
     * @return 若成功点亮传送门则返回 true
     */
    private static boolean attemptToLightPortal(Level level, BlockPos pos, Player player, ItemStack heldItem) {
        if (tryLightPortalInAxis(level, pos, Direction.Axis.X)
                || tryLightPortalInAxis(level, pos, Direction.Axis.Z)) {

            PortalEffectHelper.playActivationEffects(level, pos);

            if (!player.isCreative()) {
                heldItem.setDamageValue(heldItem.getDamageValue() + 1);
                if (heldItem.getDamageValue() >= heldItem.getMaxDamage()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 尝试沿指定轴向点亮传送门。
     * <p>
     * 先定位框架角落并计算尺寸，校验尺寸与结构是否有效，
     * 全部通过后在框架内部填充传送门方块。
     *
     * @param level 所在维度
     * @param pos   点击的框架方块位置
     * @param axis  尝试的框架轴向
     * @return 若沿该轴向成功点亮传送门则返回 true
     */
    private static boolean tryLightPortalInAxis(Level level, BlockPos pos, Direction.Axis axis) {
        BlockPos minCorner = PortalFrameHelper.findPortalCorner(level, pos, axis);
        if (minCorner == null) return false;

        PortalFrameHelper.PortalDimensions dimensions =
                PortalFrameHelper.calculatePortalDimensions(level, minCorner, axis);

        if (!PortalStructureHelper.isValidPortalSize(dimensions, MIN_PORTAL_WIDTH, MIN_PORTAL_HEIGHT)) {
            return false;
        }
        if (!PortalStructureHelper.isPortalStructureValid(level, minCorner, dimensions, axis)) {
            return false;
        }

        PortalStructureHelper.createPortalStructure(level, minCorner, dimensions, axis);
        return true;
    }

    /**
     * 处理服务器刻事件。
     * <p>
     * 每个服务器 tick 推进一次传送队列。
     *
     * @param event 服务器刻事件
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        PortalTeleportManager.tick();
    }
}