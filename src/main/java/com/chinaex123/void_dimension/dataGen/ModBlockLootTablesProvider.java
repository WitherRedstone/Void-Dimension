package com.chinaex123.void_dimension.dataGen;

import com.chinaex123.void_dimension.init.VDBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public class ModBlockLootTablesProvider extends BlockLootSubProvider {
    public ModBlockLootTablesProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
    }

    @Override
    protected void generate() {
        dropSelf(VDBlocks.NAUGHT_STONE.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return VDBlocks.BLOCKS_REGISTER.getEntries().stream()
                .filter(entry -> !entry.getId().getPath().equals("void_portal"))
                .map(Holder::value)::iterator;
    }
}
