package com.chinaex123.void_dimension;

import com.chinaex123.void_dimension.event.VoidPortalBreakEvent;
import com.chinaex123.void_dimension.init.VDBlocks;
import com.chinaex123.void_dimension.init.VDCreativeTabs;
import com.chinaex123.void_dimension.init.VDItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(VoidDimension.MOD_ID)
public class VoidDimension {
    public static final String MOD_ID = "void_dimension";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VoidDimension(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(new VoidPortalBreakEvent());
        VDCreativeTabs.register(modEventBus);
        VDItems.register(modEventBus);
        VDBlocks.register(modEventBus);
    }
}
