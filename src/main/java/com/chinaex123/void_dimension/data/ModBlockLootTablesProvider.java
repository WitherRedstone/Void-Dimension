package com.chinaex123.void_dimension.data;

import com.chinaex123.void_dimension.init.VDBlocks;
import net.minecraft.core.Holder;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public class ModBlockLootTablesProvider extends BlockLootSubProvider {
    public ModBlockLootTablesProvider(LootTableSubProvider.Context context) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), context);
    }

    @Override
    protected void generate() {
        dropSelf(VDBlocks.NAUGHT_STONE.get());
    }

    @Override
    protected @NonNull Iterable<Block> getKnownBlocks() {
        return VDBlocks.BLOCKS_REGISTER.getEntries().stream()
                .filter(entry -> !entry.getId().getPath().equals("void_portal"))
                .map(Holder::value)::iterator;
    }
}
