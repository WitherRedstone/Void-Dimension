package com.chinaex123.void_dimension.data;

import com.chinaex123.void_dimension.VoidDimension;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

import java.util.Set;

@EventBusSubscriber(modid = VoidDimension.MOD_ID)
public class ModDataGenerator {

    @SubscribeEvent
    public static void gatherRegistryEntries(GatherDataRegistryEntriesEvent event) {
        event.add(RecipeProvider.asBootstrap(ModRecipesProvider::new));
        event.add(Registries.LOOT_TABLE, new ModLootProvider());
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(true, DatapackBuiltinEntriesProvider.forWorldLayer(
                output,
                VoidDimension.MOD_ID + "_worldgen",
                event.getWorldLookupProvider(),
                ModWorldGenProvider.BUILDER,
                Set.of(VoidDimension.MOD_ID)
        ));

        event.createProvider(ModBlockTagsProvider::new);
    }
}