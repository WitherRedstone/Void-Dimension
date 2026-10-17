package com.chinaex123.void_dimension.worldgen.biome;

import com.chinaex123.void_dimension.VoidDimension;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
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
            ResourceLocation.fromNamespaceAndPath(VoidDimension.MOD_ID, "void"));

    /**
     * 构建虚空群系实例。
     *
     * @param context 数据包引导上下文，提供已放置特征与雕刻器的注册表查询
     * @return 构建完成的虚空群系
     */
    public static Biome voidBiome(BootstapContext<Biome> context) {
        MobSpawnSettings.Builder spawnSettings = new MobSpawnSettings.Builder();

        // 怪物生成
        spawnSettings.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                EntityType.SPIDER, 100, 4, 4));
        spawnSettings.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                EntityType.ZOMBIE, 95, 4, 4));
        spawnSettings.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                EntityType.ZOMBIE_VILLAGER, 5, 1, 1));
        spawnSettings.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                EntityType.SKELETON, 100, 4, 4));
        spawnSettings.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                EntityType.CREEPER, 100, 4, 4));
        spawnSettings.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                EntityType.SLIME, 100, 4, 4));
        spawnSettings.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                EntityType.ENDERMAN, 10, 1, 4));
        spawnSettings.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                EntityType.WITCH, 5, 1, 1));
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
                        .skyColor(7907327) // 天空颜色
                        .fogColor(12638463) // 雾颜色
                        .waterColor(4159204) // 水颜色
                        .waterFogColor(329011) // 水雾颜色
                        .build())
                .build();
    }

    /**
     * 注册虚空群系。
     * <p>
     * 在数据包引导阶段将虚空群系注册到注册表中。
     *
     * @param context 数据包引导上下文
     */
    public static void bootstrap(BootstapContext<Biome> context) {
        context.register(VOID_BIOME, voidBiome(context));
    }
}