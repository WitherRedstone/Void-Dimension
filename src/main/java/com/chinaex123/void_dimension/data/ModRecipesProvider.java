package com.chinaex123.void_dimension.data;

import com.chinaex123.void_dimension.init.VDBlocks;
import com.chinaex123.void_dimension.init.VDItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.Tags;

public class ModRecipesProvider extends RecipeProvider {
    public ModRecipesProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    @Override
    protected void buildRecipes() {
        HolderGetter<Item> items = this.items;

        // 归墟基石
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, VDBlocks.NAUGHT_STONE.get().asItem())
                .pattern("BCB")
                .pattern("CAC")
                .pattern("BCB")
                .define('A', Items.CHISELED_POLISHED_BLACKSTONE)
                .define('B', Tags.Items.GEMS_AMETHYST)
                .define('C', Items.ECHO_SHARD)
                .unlockedBy("has_naught_stone", has(Items.CHISELED_POLISHED_BLACKSTONE))
                .save(output);
        // 归墟碎片
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, VDItems.NAUGHT_SHARD.get().asItem())
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
