package com.chinaex123.void_dimension.worldgen.dim;

import com.chinaex123.void_dimension.VoidDimension;
import com.chinaex123.void_dimension.worldgen.biome.VDBiomes;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * 虚空维度定义类。
 * <p>
 * 功能：
 * 1. 定义虚空维度的维度类型（DimensionType）；
 * 2. 定义虚空维度的维度整体（LevelStem），使用超平坦生成器与虚空群系；
 * 3. 提供维度、维度类型、维度整体三个资源键，供其他代码引用。
 */
public class VDDimensions {

    /** 维度整体 */
    public static final ResourceKey<LevelStem> VOID_LEVEL_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(VoidDimension.MOD_ID, "void"));
    /** 维度 */
    public static final ResourceKey<Level> VOID_KEY = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(VoidDimension.MOD_ID, "void"));
    /** 维度类型 */
    public static final ResourceKey<DimensionType> VOID_DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(VoidDimension.MOD_ID, "void"));

    /**
     * 注册虚空维度的维度类型。
     * <p>
     * 关键配置：
     * 1. 时间固定为 6000 tick（正午）；
     * 2. 有天空光照、无天花板、水不会蒸发；
     * 3. 视为自然维度，坐标缩放为 1.0；
     * 4. 床与重生锚均可正常使用；
     * 5. 最低建筑高度 -64，总高度 384，逻辑高度 384；
     * 6. 无限燃烧方块使用主世界标签，环境效果使用主世界效果；
     * 7. 环境光照强度为 0.0；
     * 8. 怪物生成关闭朱玲僵尸化与袭击，光照条件为 0~7，方块光照上限为 0。
     *
     * @param context 数据包引导上下文
     */
    public static void bootstrap(BootstrapContext<DimensionType> context) {
        context.register(VOID_DIMENSION_TYPE, new DimensionType(
                OptionalLong.of(6000L), // 固定时间
                true, // 是否有天空光照
                false, // 是否有天花板
                false, // 水是否会蒸发
                true, // 是否自然维度
                1.0, // 坐标缩放比例
                true, // 床是否能正常使用
                true, // 重生锚是否可用
                -64, // 最低建筑高度
                384, // 总高度
                384, // 逻辑高度
                BlockTags.INFINIBURN_OVERWORLD, // 无限燃烧方块标签
                BuiltinDimensionTypes.OVERWORLD_EFFECTS, // 环境效果位置
                0.0f, // 环境光照强度
                new DimensionType.MonsterSettings(
                        false, // 猪灵是否会僵尸化
                        false, // 是否会发生袭击
                        UniformInt.of(0, 7), // 怪物生成的光照条件
                        0 // 方块光照上限
                )));
    }

    /**
     * 注册虚空维度的维度整体。
     * <p>
     * 使用超平坦生成器：
     * 1. 不设置结构覆盖；
     * 2. 群系使用虚空群系；
     * 3. 地层列表为空（不生成任何地层）；
     * 4. 维度类型使用上面注册的 VOID_DIMENSION_TYPE。
     *
     * @param context 数据包引导上下文
     */
    public static void bootstrapLevelStem(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> dimTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        FlatLevelGeneratorSettings flatSettings = new FlatLevelGeneratorSettings(
                Optional.empty(), // 无结构覆盖
                biomes.getOrThrow(VDBiomes.VOID_BIOME), // 群系
                List.of() // 空地层
        );

        ChunkGenerator generator = new FlatLevelSource(flatSettings);

        context.register(VOID_LEVEL_KEY, new LevelStem(
                dimTypes.getOrThrow(VOID_DIMENSION_TYPE), // 维度类型
                generator));
    }
}