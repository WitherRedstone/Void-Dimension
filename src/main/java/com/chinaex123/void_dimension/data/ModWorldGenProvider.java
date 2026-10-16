package com.chinaex123.void_dimension.data;

import com.chinaex123.void_dimension.worldgen.biome.VDBiomes;
import com.chinaex123.void_dimension.worldgen.dim.VDDimensions;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

public class ModWorldGenProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.BIOME, VDBiomes::bootstrap)
            .add(Registries.DIMENSION_TYPE, VDDimensions::bootstrap)
            .add(Registries.LEVEL_STEM, VDDimensions::bootstrapLevelStem);
}