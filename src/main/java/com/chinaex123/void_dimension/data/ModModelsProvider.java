package com.chinaex123.void_dimension.data;

import com.chinaex123.void_dimension.VoidDimension;
import com.chinaex123.void_dimension.init.VDBlocks;
import com.chinaex123.void_dimension.init.VDItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.stream.Stream;

public class ModModelsProvider extends ModelProvider {
    public ModModelsProvider(PackOutput output) {
        super(output, VoidDimension.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(VDItems.NAUGHT_SHARD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        blockModels.createTrivialCube(VDBlocks.NAUGHT_STONE.get());
    }

    @Override
    protected @NonNull Stream<? extends Holder<Item>> getKnownItems() {
        return VDItems.ITEMS_REGISTER.getEntries().stream();
    }

    @Override
    protected @NonNull Stream<? extends Holder<Block>> getKnownBlocks() {
        return VDBlocks.BLOCKS_REGISTER.getEntries().stream()
                .filter(holder -> !holder.is(VDBlocks.VOID_PORTAL.getKey()));
    }
}
