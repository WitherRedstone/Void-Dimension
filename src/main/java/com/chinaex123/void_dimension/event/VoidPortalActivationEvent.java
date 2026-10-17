package com.chinaex123.void_dimension.event;

import com.chinaex123.void_dimension.VoidDimension;
import com.chinaex123.void_dimension.init.VDBlocks;
import com.chinaex123.void_dimension.init.VDItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 虚空传送门激活事件处理。
 */
@Mod.EventBusSubscriber(modid = VoidDimension.MOD_ID)
public class VoidPortalActivationEvent {

    /** 传送门内部最小宽度 */
    private static final int MIN_PORTAL_WIDTH = 2;

    /** 传送门内部最小高度 */
    private static final int MIN_PORTAL_HEIGHT = 3;

    /**
     * 处理玩家右键方块事件。
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
            // 阻止后续交互（Forge 1.20.1 使用 Event.Result 阻止默认行为）
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
            player.swing(InteractionHand.MAIN_HAND, true);
        }
    }

    /**
     * 尝试点亮传送门。
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
     *
     * @param event 服务器刻事件
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            PortalTeleportManager.tick();
        }
    }
}