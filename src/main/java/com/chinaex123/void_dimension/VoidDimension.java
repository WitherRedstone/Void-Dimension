package com.chinaex123.void_dimension;

import com.chinaex123.void_dimension.init.VDBlocks;
import com.chinaex123.void_dimension.init.VDItems;
import com.chinaex123.void_dimension.init.VDCreativeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(VoidDimension.MOD_ID)
public class VoidDimension {
    public static final String MOD_ID = "void_dimension";

    public VoidDimension(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        VDCreativeTabs.register(modEventBus);
        VDItems.register(modEventBus);
        VDBlocks.register(modEventBus);
    }
}
