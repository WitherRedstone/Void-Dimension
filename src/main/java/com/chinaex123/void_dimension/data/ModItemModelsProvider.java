package com.chinaex123.void_dimension.data;

import com.chinaex123.void_dimension.VoidDimension;
import com.chinaex123.void_dimension.init.VDItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelsProvider extends ItemModelProvider {
    public ModItemModelsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, VoidDimension.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(VDItems.NAUGHT_SHARD.get());
    }
}
