package com.chinaex123.void_dimension.data;

import com.chinaex123.void_dimension.VoidDimension;
import com.chinaex123.void_dimension.init.VDBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, VoidDimension.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(VDBlocks.NAUGHT_STONE.getKey());

        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(VDBlocks.NAUGHT_STONE.getKey());
    }
}
