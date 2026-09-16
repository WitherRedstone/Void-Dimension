package com.chinaex123.void_dimension.data;

import com.chinaex123.void_dimension.VoidDimension;
import com.chinaex123.void_dimension.worldgen.biome.VDBiomes;
import com.chinaex123.void_dimension.worldgen.dim.VDDimensions;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.BIOME, VDBiomes::bootstrap)
            .add(Registries.DIMENSION_TYPE, VDDimensions::bootstrap)
            .add(Registries.LEVEL_STEM, VDDimensions::bootstrapLevelStem);

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(VoidDimension.MOD_ID));
    }
}
