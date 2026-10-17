package com.chinaex123.void_dimension.data;

import com.chinaex123.void_dimension.VoidDimension;
import com.chinaex123.void_dimension.init.VDBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, VoidDimension.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(VDBlocks.NAUGHT_STONE.get(), cubeAll(VDBlocks.NAUGHT_STONE.get()));
    }
}
