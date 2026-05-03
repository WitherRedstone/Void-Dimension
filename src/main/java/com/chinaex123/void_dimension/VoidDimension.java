package com.chinaex123.void_dimension;

import com.chinaex123.void_dimension.event.VoidPortalBreakEvent;
import com.chinaex123.void_dimension.init.ModBlocks;
import com.chinaex123.void_dimension.init.ModItems;
import com.chinaex123.void_dimension.init.ModCreativeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(VoidDimension.MOD_ID)
public class VoidDimension {
    public static final String MOD_ID = "void_dimension";

    public VoidDimension(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new VoidPortalBreakEvent());
        ModCreativeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {}
}
