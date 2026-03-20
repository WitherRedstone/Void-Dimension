package com.chinaex123.void_dimension.dataGen;

import com.chinaex123.void_dimension.VoidDimension;
import com.chinaex123.void_dimension.block.ModBlocks;
import com.chinaex123.void_dimension.item.ModItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipesProvider extends RecipeProvider {
    private final HolderLookup.Provider provider;
    private final RecipeOutput output;

    public ModRecipesProvider(HolderLookup.Provider provider, RecipeOutput output) {
        super(provider, output);
        this.provider = provider;
        this.output = output;
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(packOutput, lookupProvider);
        }

        @Override
        protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new ModRecipesProvider(provider, output);
        }

        @Override
        public @NotNull String getName() {
            return VoidDimension.MOD_ID;
        }
    }

    @Override
    protected void buildRecipes() {
        HolderGetter<Item> itemRegistryLookup = this.registries.lookupOrThrow(Registries.ITEM);

        // 归墟基石
        ShapedRecipeBuilder.shaped(itemRegistryLookup, RecipeCategory.MISC, ModBlocks.NAUGHT_STONE.get().asItem())
                .pattern("BCB")
                .pattern("CAC")
                .pattern("BCB")
                .define('A', Items.CHISELED_POLISHED_BLACKSTONE)
                .define('B', Tags.Items.GEMS_AMETHYST)
                .define('C', Items.ECHO_SHARD)
                .unlockedBy("has_naught_stone", has(Items.CHISELED_POLISHED_BLACKSTONE))
                .save(output);
        // 归墟碎片
        ShapedRecipeBuilder.shaped(itemRegistryLookup, RecipeCategory.MISC, ModItems.NAUGHT_SHARD.get().asItem())
                .pattern("BCB")
                .pattern("CAC")
                .pattern("BCB")
                .define('A', Tags.Items.GEMS_AMETHYST)
                .define('B', Tags.Items.GEMS_DIAMOND)
                .define('C', Items.ECHO_SHARD)
                .unlockedBy("has_naught_shard", has(Tags.Items.GEMS_AMETHYST))
                .save(output);

    }
}
