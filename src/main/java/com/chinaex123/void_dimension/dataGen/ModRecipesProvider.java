package com.chinaex123.void_dimension.dataGen;

import com.chinaex123.void_dimension.register.ModBlocks;
import com.chinaex123.void_dimension.register.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipesProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {

        // 归墟基石
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                        ModBlocks.NAUGHT_STONE.get())
                .pattern("BCB")
                .pattern("CAC")
                .pattern("BCB")
                .define('A', Items.CHISELED_POLISHED_BLACKSTONE)
                .define('B', Tags.Items.GEMS_AMETHYST)
                .define('C', Items.ECHO_SHARD)
                .unlockedBy("has_naught_stone", has(Items.CHISELED_POLISHED_BLACKSTONE))
                .save(recipeOutput);
        // 归墟碎片
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                        ModItems.NAUGHT_SHARD.get())
                .pattern("BCB")
                .pattern("CAC")
                .pattern("BCB")
                .define('A', Tags.Items.GEMS_AMETHYST)
                .define('B', Tags.Items.GEMS_DIAMOND)
                .define('C', Items.ECHO_SHARD)
                .unlockedBy("has_naught_shard", has(Tags.Items.GEMS_AMETHYST))
                .save(recipeOutput);

    }
}
