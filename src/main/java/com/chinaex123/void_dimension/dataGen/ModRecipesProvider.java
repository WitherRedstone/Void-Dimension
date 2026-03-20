package com.chinaex123.void_dimension.dataGen;

import com.chinaex123.void_dimension.register.ModBlocks;
import com.chinaex123.void_dimension.register.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.function.Consumer;

public class ModRecipesProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipesProvider(PackOutput p0utput) {
        super(p0utput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {

        // 归墟基石
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                        ModBlocks.NAUGHT_STONE.get())
                .pattern("BCB")
                .pattern("CAC")
                .pattern("BCB")
                .define('A', Blocks.CHISELED_POLISHED_BLACKSTONE)
                .define('B', Tags.Items.GEMS_AMETHYST)
                .define('C', Items.ECHO_SHARD)
                .unlockedBy("has_naught_stone", has(Blocks.CHISELED_POLISHED_BLACKSTONE))
                .save(writer);
        
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
                .save(writer);

    }
}
