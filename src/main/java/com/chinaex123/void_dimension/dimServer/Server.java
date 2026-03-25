package com.chinaex123.void_dimension.dimServer;

import com.chinaex123.void_dimension.block.ModBlocks;
import com.chinaex123.void_dimension.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Objects;
import java.util.Set;

@EventBusSubscriber(modid = "void_dimension")
public class Server {

    // 虚空维度资源键
    private static final ResourceKey<Level> VOID_DIMENSION_KEY = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath("void_dimension", "void")
    );

    // 传送门最小尺寸要求（内部空间）
    private static final int MIN_PORTAL_WIDTH = 2;
    private static final int MIN_PORTAL_HEIGHT = 3;

    /**
     * 玩家右键点击方块事件处理
     * 当玩家使用虚空碎片右键点击虚空石时尝试创建传送门
     */
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = player.level();

        // 只在服务端处理
        if (level.isClientSide()) {
            return;
        }

        // 检查玩家是否手持虚空碎片
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!heldItem.is(ModItems.NAUGHT_SHARD.get())) {
            return;
        }

        BlockPos clickedPos = event.getPos();

        // 检查点击的是否是虚空石方块
        if (!level.getBlockState(clickedPos).is(ModBlocks.NAUGHT_STONE.get())) {
            return;
        }

        // 尝试点亮传送门
        if (attemptToLightPortal(level, clickedPos, player, heldItem)) {
            event.setCanceled(true);

            // 播放右键摆臂动画（客户端）
            if (!level.isClientSide()) {
                player.swing(InteractionHand.MAIN_HAND, true);
            }
        }
    }

    /**
     * 尝试点亮传送门的核心方法
     * @param level 世界对象
     * @param pos 点击位置
     * @param player 玩家对象
     * @param heldItem 手持物品
     * @return 是否成功点亮传送门
     */
    private static boolean attemptToLightPortal(Level level, BlockPos pos, Player player, ItemStack heldItem) {
        // 先尝试东西方向，再尝试南北方向
        if (tryLightPortalInAxis(level, pos, Direction.Axis.X) ||
                tryLightPortalInAxis(level, pos, Direction.Axis.Z)) {

            // 添加传送门激活的动作效果
            playPortalActivationEffects(level, pos, player);

            // 消耗耐久度（创造模式不消耗）
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
     * 播放传送门激活效果
     * @param level 世界对象
     * @param pos 激活位置
     * @param player 玩家对象
     */
    private static void playPortalActivationEffects(Level level, BlockPos pos, Player player) {
        // 播放激活音效
        level.playSound(null, pos,
                SoundEvents.END_PORTAL_SPAWN,
                SoundSource.BLOCKS,
                1.0f, 1.0f);

        // 播放粒子效果
        spawnPortalActivationParticles(level, pos);
    }

    /**
     * 生成传送门激活粒子效果
     * @param level 世界对象
     * @param pos 中心位置
     */
    private static void spawnPortalActivationParticles(Level level, BlockPos pos) {
        // 确保在服务端执行
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // 获取传送门方块的状态来确定传送门的方向
        BlockState portalState = level.getBlockState(pos);
        Direction.Axis portalAxis = portalState.hasProperty(VoidPortal.AXIS) ?
                portalState.getValue(VoidPortal.AXIS) : Direction.Axis.Z;

        // 计算传送门中心点
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 0.5;
        double centerZ = pos.getZ() + 0.5;

        // 生成向内收缩的粒子效果
        for (int i = 0; i < 50; i++) { // 增加粒子数量
            // 在传送门周围较大的范围内生成粒子起点
            double startX = centerX + (serverLevel.getRandom().nextDouble() - 0.5) * 6.0;
            double startY = centerY + (serverLevel.getRandom().nextDouble() - 0.5) * 6.0;
            double startZ = centerZ + (serverLevel.getRandom().nextDouble() - 0.5) * 6.0;

            // 计算指向传送门中心的向量
            double deltaX = centerX - startX;
            double deltaY = centerY - startY;
            double deltaZ = centerZ - startZ;

            // 标准化向量并应用收缩速度
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            if (distance > 0) {
                double speedFactor = 0.1 + serverLevel.getRandom().nextDouble() * 0.2; // 随机速度
                deltaX = (deltaX / distance) * speedFactor;
                deltaY = (deltaY / distance) * speedFactor;
                deltaZ = (deltaZ / distance) * speedFactor;
            }

            // 发送收缩粒子
            serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    startX, startY, startZ, // 起始位置
                    1, // 每次发送1个粒子
                    deltaX, deltaY, deltaZ, // 速度向量（指向中心）
                    1.0 // 粒子生命周期缩放
            );
        }

        // 在传送门内部生成漩涡效果
        for (int i = 0; i < 30; i++) {
            // 在传送门平面内生成粒子
            double offsetX, offsetZ;
            if (portalAxis == Direction.Axis.X) {
                // 东西向传送门，在Z轴方向扩散
                offsetX = (serverLevel.getRandom().nextDouble() - 0.5) * 0.8;
                offsetZ = (serverLevel.getRandom().nextDouble() - 0.5) * 2.0;
            } else {
                // 南北向传送门，在X轴方向扩散
                offsetX = (serverLevel.getRandom().nextDouble() - 0.5) * 2.0;
                offsetZ = (serverLevel.getRandom().nextDouble() - 0.5) * 0.8;
            }

            double particleX = centerX + offsetX;
            double particleY = centerY + (serverLevel.getRandom().nextDouble() - 0.5) * 2.0;
            double particleZ = centerZ + offsetZ;

            // 向下螺旋运动
            serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    particleX, particleY, particleZ,
                    1,
                    0.0, -0.1, 0.0, // 向下运动
                    0.5 // 较慢的速度
            );
        }

        // 添加中心吸引点的强烈效果
        serverLevel.sendParticles(
                ParticleTypes.ENCHANTED_HIT,
                centerX, centerY, centerZ,
                20, // 更多粒子
                0.3, 0.3, 0.3, // 小范围扩散
                0.0 // 几乎静止
        );
    }

    /**
     * 在指定轴向上检测并创建传送门
     * @param level 世界对象
     * @param pos 检测起始位置
     * @param axis 检测轴向（X表示东西框架，Z表示南北框架）
     * @return 是否成功创建传送门
     */
    private static boolean tryLightPortalInAxis(Level level, BlockPos pos, Direction.Axis axis) {
        // 找到传送门框架的最小角（左下角）
        BlockPos minCorner = findPortalMinCorner(level, pos, axis);
        if (minCorner == null) {
            return false;
        }

        // 计算传送门框架尺寸
        PortalDimensions dimensions = calculatePortalDimensions(level, minCorner, axis);

        // 验证传送门尺寸是否符合要求
        if (!isValidPortalSize(dimensions)) {
            return false;
        }

        // 验证框架完整性和内部空间
        if (!isPortalStructureValid(level, minCorner, dimensions, axis)) {
            return false;
        }

        // 创建传送门（传入框架轴向，方法内会转换为正确的传送门轴向）
        createPortalStructure(level, minCorner, dimensions, axis);
        return true;
    }


    /**
     * 查找传送门框架的最小角（左下角位置）
     * @param level 世界对象
     * @param start 起始搜索位置
     * @param axis 搜索轴向
     * @return 最小角位置，如果找不到则返回null
     */
    private static BlockPos findPortalMinCorner(Level level, BlockPos start, Direction.Axis axis) {
        BlockPos pos = start.immutable();

        // 向左/北方向移动到框架边缘
        Direction negativeDir = getNegativeDirection(axis);
        while (isFrameBlock(level, pos.relative(negativeDir))) {
            pos = pos.relative(negativeDir);
        }

        // 向下移动到框架底部
        while (isFrameBlock(level, pos.below())) {
            pos = pos.below();
        }

        return pos;
    }

    /**
     * 计算传送门框架的尺寸
     */
    private static PortalDimensions calculatePortalDimensions(Level level, BlockPos minCorner, Direction.Axis axis) {
        int height = countFrameBlocks(level, minCorner, Direction.UP);
        int width = countFrameBlocks(level, minCorner, getPositiveDirection(axis));
        return new PortalDimensions(width, height);
    }

    /**
     * 计算指定方向上的框架方块数量
     */
    private static int countFrameBlocks(Level level, BlockPos start, Direction direction) {
        int count = 1;
        BlockPos currentPos = start.relative(direction);

        while (isFrameBlock(level, currentPos)) {
            count++;
            currentPos = currentPos.relative(direction);
        }
        return count;
    }

    /**
     * 验证传送门尺寸是否有效
     */
    private static boolean isValidPortalSize(PortalDimensions dimensions) {
        // 内部空间至少需要 2x3
        return dimensions.width >= MIN_PORTAL_WIDTH + 2 &&
                dimensions.height >= MIN_PORTAL_HEIGHT + 2;
    }

    /**
     * 验证传送门结构是否完整有效
     */
    private static boolean isPortalStructureValid(Level level, BlockPos minCorner,
                                                  PortalDimensions dimensions, Direction.Axis axis) {
        // 验证框架完整性
        if (!isFrameComplete(level, minCorner, dimensions, axis)) {
            return false;
        }

        // 验证内部空间是否为空
        return isInteriorEmpty(level, minCorner, dimensions, axis);
    }

    /**
     * 验证传送门框架是否完整
     */
    private static boolean isFrameComplete(Level level, BlockPos minCorner,
                                           PortalDimensions dimensions, Direction.Axis axis) {
        Direction right = getPositiveDirection(axis);
        Direction left = getNegativeDirection(axis);

        // 检查底部边框
        if (!isHorizontalFrameComplete(level, minCorner, dimensions.width, right)) {
            return false;
        }

        // 检查顶部边框
        BlockPos topCorner = minCorner.above(dimensions.height - 1);
        if (!isHorizontalFrameComplete(level, topCorner, dimensions.width, right)) {
            return false;
        }

        // 检查左侧边框
        if (!isVerticalFrameComplete(level, minCorner, dimensions.height)) {
            return false;
        }

        // 检查右侧边框
        BlockPos rightCorner = minCorner.relative(right, dimensions.width - 1);
        return isVerticalFrameComplete(level, rightCorner, dimensions.height);
    }

    /**
     * 检查水平方向框架是否完整
     */
    private static boolean isHorizontalFrameComplete(Level level, BlockPos start, int width, Direction direction) {
        for (int i = 0; i < width; i++) {
            if (!isFrameBlock(level, start.relative(direction, i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查垂直方向框架是否完整
     */
    private static boolean isVerticalFrameComplete(Level level, BlockPos start, int height) {
        for (int i = 0; i < height; i++) {
            if (!isFrameBlock(level, start.above(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证传送门内部空间是否为空
     */
    private static boolean isInteriorEmpty(Level level, BlockPos minCorner,
                                           PortalDimensions dimensions, Direction.Axis axis) {
        Direction right = getPositiveDirection(axis);
        int interiorWidth = dimensions.width - 2;
        int interiorHeight = dimensions.height - 2;

        for (int x = 1; x <= interiorWidth; x++) {
            for (int y = 1; y <= interiorHeight; y++) {
                BlockPos checkPos = minCorner.relative(right, x).above(y);
                if (!level.isEmptyBlock(checkPos)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 创建传送门结构
     */
    private static void createPortalStructure(Level level, BlockPos minCorner,
                                              PortalDimensions dimensions, Direction.Axis axis) {
        Block portalBlock = ModBlocks.VOID_PORTAL.get();

        // 关键修正：传送门的轴向应该与框架的短边方向一致
        // 如果框架是东西方向延伸（X轴），传送门应该是南北走向（Z轴）
        // 如果框架是南北方向延伸（Z轴），传送门应该是东西走向（X轴）
        Direction.Axis portalAxis = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;

        // 计算传送门的起始位置和尺寸
        int portalWidth = dimensions.width - 2;  // 内部宽度
        int portalHeight = dimensions.height - 2; // 内部高度

        Direction right = getPositiveDirection(axis);

        // 填充传送门内部空间，并设置正确的轴向
        for (int x = 1; x <= portalWidth; x++) {
            for (int y = 1; y <= portalHeight; y++) {
                BlockPos portalPos = minCorner.relative(right, x).above(y);
                // 设置正确的传送门轴向
                BlockState portalState = portalBlock.defaultBlockState().setValue(VoidPortal.AXIS, portalAxis);
                level.setBlock(portalPos, portalState, 3);
            }
        }
    }


    /**
     * 获取指定轴向的正方向
     */
    private static Direction getPositiveDirection(Direction.Axis axis) {
        // 修正：X轴应该是东向，Z轴应该是南向
        return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
    }

    /**
     * 获取指定轴向的负方向
     */
    private static Direction getNegativeDirection(Direction.Axis axis) {
        // 修正：X轴应该是西向，Z轴应该是北向
        return axis == Direction.Axis.X ? Direction.WEST : Direction.NORTH;
    }

    /**
     * 检查指定位置是否为传送门框架方块
     */
    private static boolean isFrameBlock(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.NAUGHT_STONE.get());
    }

    /**
     * 传送玩家到虚空维度并创建返回传送门
     * @param player 要传送的玩家
     */
    public static void teleportToVoidDimension(ServerPlayer player) {
        // 记录原始维度
        ResourceKey<Level> originalDimension = player.level().dimension();

        // 执行传送
        player.teleportTo(
                (ServerLevel) Objects.requireNonNull(player.level().getServer().getLevel(VOID_DIMENSION_KEY)),
                player.getX(),
                player.getY(),
                player.getZ(),
                Set.of(),
                player.getYRot(),
                player.getXRot(),
                false
        );

        // 延迟创建返回传送门，确保传送完成
        player.level().getServer().execute(() -> {
            createReturnPortalAtCurrentPosition(player, originalDimension);
        });
    }

    /**
     * 在当前位置创建返回传送门
     */
    private static void createReturnPortalAtCurrentPosition(ServerPlayer player, ResourceKey<Level> returnDimension) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        // 在玩家前方3格处创建传送门
        BlockPos portalPos = playerPos.relative(player.getDirection(), 3).above();

        // 确保有足够的空间
        if (isAreaClearForPortal(level, portalPos)) {
            buildSimpleReturnPortal(level, portalPos);
        } else {
            // 如果前方没有空间，尝试在玩家脚下创建
            portalPos = playerPos;
            if (isAreaClearForPortal(level, portalPos)) {
                buildSimpleReturnPortal(level, portalPos);
            }
        }
    }

    /**
     * 检查区域是否适合建造传送门
     */
    private static boolean isAreaClearForPortal(Level level, BlockPos pos) {
        // 检查2x5区域是否清空（包括底部和顶部）
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 5; y++) {
                if (!level.isEmptyBlock(pos.offset(x, y, 0))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 建造完整的返回传送门（2x4大小，包括底部边框）
     */
    private static void buildSimpleReturnPortal(Level level, BlockPos pos) {
        Block frameBlock = ModBlocks.NAUGHT_STONE.get();
        Block portalBlock = ModBlocks.VOID_PORTAL.get();

        // 清理空间确保传送门能正确生成
        for (int y = 0; y < 5; y++) {
            level.setBlock(pos.above(y), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(pos.east().above(y), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        }

        // 建造完整的框架结构（2x4大小）
        // 底部边框
        level.setBlock(pos, frameBlock.defaultBlockState(), 3);
        level.setBlock(pos.east(), frameBlock.defaultBlockState(), 3);

        // 左侧垂直边框
        for (int y = 1; y < 4; y++) {
            level.setBlock(pos.above(y), frameBlock.defaultBlockState(), 3);
        }

        // 右侧垂直边框
        for (int y = 1; y < 4; y++) {
            level.setBlock(pos.east().above(y), frameBlock.defaultBlockState(), 3);
        }

        // 顶部边框
        level.setBlock(pos.above(4), frameBlock.defaultBlockState(), 3);
        level.setBlock(pos.east().above(4), frameBlock.defaultBlockState(), 3);

        // 填充传送门内部（2x2大小），设置正确的传送门方向（Z轴，南北向）
        BlockState portalState = portalBlock.defaultBlockState().setValue(VoidPortal.AXIS, Direction.Axis.Z);
        level.setBlock(pos.above(1), portalState, 3);
        level.setBlock(pos.above(2), portalState, 3);
        level.setBlock(pos.east().above(1), portalState, 3);
        level.setBlock(pos.east().above(2), portalState, 3);

    }

    /**
     * 传送门尺寸数据类
     */
    private record PortalDimensions(int width, int height) { }

    /**
     * 处理传送门传送逻辑（带延迟和动画）
     * @param player 玩家对象
     * @param portalPos 传送门位置
     */
    public static void handlePortalTeleport(ServerPlayer player, BlockPos portalPos) {
        // 增加冷却时间检查，防止频繁传送
        if (player.getPortalCooldown() > 20) { // 至少1秒冷却
            return;
        }

        Level currentLevel = player.level();
        ResourceKey<Level> currentDimension = currentLevel.dimension();
        ResourceKey<Level> targetDimension;

        // 确定目标维度
        if (currentDimension == Level.OVERWORLD) {
            targetDimension = VOID_DIMENSION_KEY;
        } else {
            targetDimension = Level.OVERWORLD;
        }

        // 计算目标坐标
        double targetX = portalPos.getX();
        double targetY = portalPos.getY();
        double targetZ = portalPos.getZ();

        // 在目标维度寻找或创建传送门，同时传递原始 Y 坐标
        BlockPos targetPos = findOrCreatePortalInDimension(
                Objects.requireNonNull(player.level().getServer().getLevel(targetDimension)),
                targetX, targetY, targetZ,
                player.getY()
        );

        // 开始传送过程
        startPortalTeleportProcess(player, portalPos, targetDimension, targetPos);
    }

    /**
     * 开始传送过程（包含动画和延迟）
     * @param player 玩家对象
     * @param portalPos 起始传送门位置
     * @param targetDimension 目标维度
     * @param targetPos 目标位置
     */
    private static void startPortalTeleportProcess(ServerPlayer player, BlockPos portalPos,
                                                   ResourceKey<Level> targetDimension, BlockPos targetPos) {
        // 设置玩家状态为正在传送
        player.setPortalCooldown(200); // 10秒总冷却

        // 播放初始传送音效
        player.level().playSound(null, portalPos,
                net.minecraft.sounds.SoundEvents.PORTAL_TRIGGER,
                net.minecraft.sounds.SoundSource.BLOCKS,
                1.0f, 1.0f);

        // 延迟 2.5 秒后执行传送
        player.level().getServer().execute(() -> {
            // 等待 50 个 tick（2.5 秒）
            try {
                Thread.sleep(2500); // 2.5 秒延迟
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            executeDelayedTeleport(player, targetDimension, targetPos, portalPos);
        });
    }

    /**
     * 执行延迟传送
     */
    private static void executeDelayedTeleport(ServerPlayer player, ResourceKey<Level> targetDimension,
                                               BlockPos targetPos, BlockPos sourcePos) {
        // 执行实际传送
        player.teleportTo(
                (ServerLevel) Objects.requireNonNull(player.level().getServer().getLevel(targetDimension)),
                targetPos.getX() + 0.5,
                targetPos.getY() + 1,
                targetPos.getZ() + 0.5,
                Set.of(),
                player.getYRot(),
                player.getXRot(),
                false
        );

        // 播放到达效果
        playArrivalEffects(player, targetPos);
    }

    /**
     * 在目标维度寻找或创建传送门
     * @param level 目标世界
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @param originalY 原始Y坐标（玩家进入传送门时的高度）
     * @return 传送门位置
     */
    private static BlockPos findOrCreatePortalInDimension(Level level, double x, double y, double z, double originalY) {
        BlockPos targetPos = BlockPos.containing(x, y, z);

        // 首先在附近寻找现有的传送门
        BlockPos existingPortal = findNearbyPortal(level, targetPos, 16);
        if (existingPortal != null) {
            return existingPortal;
        }

        // 如果没找到，创建新的传送门，并使用原始Y坐标作为参考
        return createPortalInDimension(level, targetPos, originalY);
    }

    /**
     * 在指定范围内寻找传送门
     * @param level 世界对象
     * @param center 中心位置
     * @param radius 搜索半径
     * @return 传送门位置，如果没找到则返回null
     */
    private static BlockPos findNearbyPortal(Level level, BlockPos center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -radius/2; dy <= radius/2; dy++) {
                    BlockPos checkPos = center.offset(dx, dy, dz);
                    if (level.getBlockState(checkPos).is(ModBlocks.VOID_PORTAL.get())) {
                        return checkPos;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 在指定位置创建传送门
     * @param level 世界对象
     * @param pos 目标位置
     * @param originalY 原始Y坐标
     * @return 传送门位置
     */
    private static BlockPos createPortalInDimension(Level level, BlockPos pos, double originalY) {
        // 寻找合适的地面位置，但考虑原始Y坐标
        BlockPos groundPos = findSuitableGroundWithHeight(level, pos, originalY);

        // 创建传送门结构
        buildNetherStylePortal(level, groundPos);

        return groundPos.above(); // 返回传送门内部位置
    }
    /**
     * 寻找合适的地面位置（考虑原始高度）
     * @param level 世界对象
     * @param pos 参考位置
     * @param originalY 原始Y坐标
     * @return 合适的地面位置
     */
    private static BlockPos findSuitableGroundWithHeight(Level level, BlockPos pos, double originalY) {
        BlockPos.MutableBlockPos mutablePos = pos.mutable();

        // 直接使用原始 Y 坐标
        int targetY = (int) Math.floor(originalY);

        // 确保在有效范围内
        targetY = Math.max(level.getMinY() + 3, targetY);
        targetY = Math.min(level.getMaxY() - 5, targetY);

        // 直接返回目标位置，让buildNetherStylePortal方法处理平台创建
        return new BlockPos(pos.getX(), targetY, pos.getZ());
    }

    /**
     * 寻找合适的地面位置（旧版本兼容）
     * @param level 世界对象
     * @param pos 参考位置
     * @return 合适的地面位置
     */
    private static BlockPos findSuitableGround(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = pos.mutable();

        // 向下寻找固体方块
        while (mutablePos.getY() > level.getMinY() &&
                !level.getBlockState(mutablePos).isFaceSturdy(level, mutablePos, net.minecraft.core.Direction.UP)) {
            mutablePos.move(0, -1, 0);
        }

        // 如果找到固体方块，向上移动一格
        if (level.getBlockState(mutablePos).isFaceSturdy(level, mutablePos, net.minecraft.core.Direction.UP)) {
            mutablePos.move(0, 1, 0);
        }

        return mutablePos;
    }

    /**
     * 建造传送门（4x5大小）
     * @param level 世界对象
     * @param pos 传送门位置
     */
    private static void buildNetherStylePortal(Level level, BlockPos pos) {
        Block frameBlock = ModBlocks.NAUGHT_STONE.get();
        Block portalBlock = ModBlocks.VOID_PORTAL.get();

        // 清理空间（4x5区域）
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 5; y++) {
                level.setBlock(pos.offset(x, y, 0), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            }
        }

        // 建造框架
        // 底部边框
        for (int x = 0; x < 4; x++) {
            level.setBlock(pos.offset(x, 0, 0), frameBlock.defaultBlockState(), 3);
        }

        // 底部边框向外扩展（前侧）
        for (int x = 0; x < 4; x++) {
            level.setBlock(pos.offset(x, 0, -1), frameBlock.defaultBlockState(), 3);
        }

        // 底部边框向外扩展（后侧）
        for (int x = 0; x < 4; x++) {
            level.setBlock(pos.offset(x, 0, 1), frameBlock.defaultBlockState(), 3);
        }

        // 顶部边框
        for (int x = 0; x < 4; x++) {
            level.setBlock(pos.offset(x, 4, 0), frameBlock.defaultBlockState(), 3);
        }

        // 左右侧边框
        for (int y = 1; y < 4; y++) {
            level.setBlock(pos.offset(0, y, 0), frameBlock.defaultBlockState(), 3);
            level.setBlock(pos.offset(3, y, 0), frameBlock.defaultBlockState(), 3);
        }

        // 填充传送门内部（2x3），设置正确的传送门方向（Z轴，南北向）
        for (int x = 1; x < 3; x++) {
            for (int y = 1; y < 4; y++) {
                BlockState portalState = portalBlock.defaultBlockState().setValue(VoidPortal.AXIS, Direction.Axis.Z);
                level.setBlock(pos.offset(x, y, 0), portalState, 3);
            }
        }
    }

    /**
     * 传送玩家到指定维度
     * @param player 要传送的玩家
     * @param targetDimension 目标维度
     */
    public static void teleportToDimension(ServerPlayer player, ResourceKey<Level> targetDimension) {
        player.teleportTo(
                (ServerLevel) Objects.requireNonNull(player.level().getServer().getLevel(targetDimension)),
                player.getX(),
                player.getY(),
                player.getZ(),
                Set.of(),
                player.getYRot(),
                player.getXRot(),
                false
        );
    }

    /**
     * 播放到达效果
     */
    private static void playArrivalEffects(ServerPlayer player, BlockPos targetPos) {
        Level level = player.level();

        // 播放到达音效
        level.playSound(null, targetPos,
                net.minecraft.sounds.SoundEvents.PORTAL_TRAVEL,
                net.minecraft.sounds.SoundSource.BLOCKS,
                1.0f, 1.2f);
    }
}
