package com.chinaex123.void_dimension.worldgen.biome;

import com.chinaex123.void_dimension.VoidDimension;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;

/**
 * 虚空维度群系定义。
 * <p>
 * 负责构建并注册虚空维度所使用的群系，包括生物生成设置、
 * 地形生成设置以及各类环境属性（天空、雾、水等颜色）。
 */
public class VDBiomes {

    /** 虚空群系的资源键，用于在注册表中定位该群系 */
    public static final ResourceKey<Biome> VOID_BIOME = ResourceKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath(VoidDimension.MOD_ID, "void"));

    /**
     * 构建虚空群系实例。
     *
     * @param context 数据包引导上下文，提供已放置特征与雕刻器的注册表查询
     * @return 构建完成的虚空群系
     */
    public static Biome voidBiome(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnSettings = new MobSpawnSettings.Builder();

        // 怪物生成
        spawnSettings.addSpawn(MobCategory.MONSTER, 100, new MobSpawnSettings.SpawnerData(
                EntityTypes.SPIDER, 4, 4));
        spawnSettings.addSpawn(MobCategory.MONSTER, 95, new MobSpawnSettings.SpawnerData(
                EntityTypes.ZOMBIE, 4, 4));
        spawnSettings.addSpawn(MobCategory.MONSTER, 5, new MobSpawnSettings.SpawnerData(
                EntityTypes.ZOMBIE_VILLAGER, 1, 1));
        spawnSettings.addSpawn(MobCategory.MONSTER, 100, new MobSpawnSettings.SpawnerData(
                EntityTypes.SKELETON, 4, 4));
        spawnSettings.addSpawn(MobCategory.MONSTER, 100, new MobSpawnSettings.SpawnerData(
                EntityTypes.CREEPER, 4, 4));
        spawnSettings.addSpawn(MobCategory.MONSTER, 100, new MobSpawnSettings.SpawnerData(
                EntityTypes.SLIME, 4, 4));
        spawnSettings.addSpawn(MobCategory.MONSTER, 10, new MobSpawnSettings.SpawnerData(
                EntityTypes.ENDERMAN, 1, 4));
        spawnSettings.addSpawn(MobCategory.MONSTER, 5, new MobSpawnSettings.SpawnerData(
                EntityTypes.WITCH, 1, 1));
        // 不生成被动生物
        spawnSettings.creatureGenerationProbability(0.0f);

        // 地形生成设置：使用上下文中的已放置特征与配置雕刻器
        BiomeGenerationSettings.Builder generationSettings =
                new BiomeGenerationSettings.Builder(
                        context.lookup(Registries.PLACED_FEATURE),
                        context.lookup(Registries.CONFIGURED_CARVER));

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false) // 是否降水
                .temperature(0.8f) // 温度
                .downfall(0.4f) // 湿度
                .generationSettings(generationSettings.build()) // 地形生成设置
                .mobSpawnSettings(spawnSettings.build()) // 生物生成设置
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .build())
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7907327)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .build();
    }

    /**
     * 注册虚空群系。
     * <p>
     * 在数据包引导阶段将虚空群系注册到注册表中。
     *
     * @param context 数据包引导上下文
     */
    public static void bootstrap(BootstrapContext<Biome> context) {
        context.register(VOID_BIOME, voidBiome(context));
    }
}