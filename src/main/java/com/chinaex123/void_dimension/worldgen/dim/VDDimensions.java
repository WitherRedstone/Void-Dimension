package com.chinaex123.void_dimension.worldgen.dim;

import com.chinaex123.void_dimension.VoidDimension;
import com.chinaex123.void_dimension.worldgen.biome.VDBiomes;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

import java.util.List;
import java.util.Optional;

/**
 * 虚空维度定义。
 * <p>
 * 负责注册虚空维度的维度类型与维度整体（LevelStem），
 * 使用超平坦生成器与虚空群系构建该维度。
 */
public class VDDimensions {

    /** 维度整体 */
    public static final ResourceKey<LevelStem> VOID_LEVEL_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            Identifier.fromNamespaceAndPath(VoidDimension.MOD_ID, "void"));
    /** 维度本身 */
    public static final ResourceKey<Level> VOID_KEY = ResourceKey.create(Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(VoidDimension.MOD_ID, "void"));
    /** 维度类型 */
    public static final ResourceKey<DimensionType> VOID_DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            Identifier.fromNamespaceAndPath(VoidDimension.MOD_ID, "void_dimension_type"));

    /**
     * 注册虚空维度的维度类型。
     * <p>
     * 定义该维度的时间、光照、高度、怪物生成条件、天空盒与环境属性等参数。
     *
     * @param context 数据包引导上下文
     */
    public static void bootstrap(BootstrapContext<DimensionType> context) {
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);
        context.register(VOID_DIMENSION_TYPE, new DimensionType(
                true, // 是否固定时间
                true, // 是否有天空光照
                false, // 是否有天花板
                false, // 是否有末影龙战斗
                1.0, // 坐标缩放比例
                -64, // 最低建筑高度
                384, // 总高度
                384, // 逻辑高度
                blocks.getOrThrow(BlockTags.INFINIBURN_OVERWORLD), // 无限燃烧方块标签
                0.0f, // 环境光照强度
                new DimensionType.MonsterSettings(
                        UniformInt.of(0, 7), // 怪物生成的光照条件
                        0 // 方块光照上限
                ),
                DimensionType.Skybox.OVERWORLD, // 天空盒类型
                CardinalLighting.Type.DEFAULT, // 基本光照类型
                EnvironmentAttributeMap.builder().build(), // 环境属性
                HolderSet.direct(List.of()), // 时间线
                Optional.empty() // 默认时钟
        ));
    }

    /**
     * 注册虚空维度的维度整体。
     * <p>
     * 使用超平坦生成器，仅包含虚空群系，不生成任何地层与结构覆盖。
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