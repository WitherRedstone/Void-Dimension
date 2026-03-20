package com.chinaex123.void_dimension;

import com.chinaex123.void_dimension.dimServer.VoidPortalBreakEvent;
import com.chinaex123.void_dimension.register.ModBlocks;
import com.chinaex123.void_dimension.register.ModItems;
import com.chinaex123.void_dimension.register.ModCreativeTabs;
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
        // 为模组加载注册 commonSetup 方法
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        // 注册传送门破坏事件处理器
        MinecraftForge.EVENT_BUS.register(new VoidPortalBreakEvent());

        // 将物品注册到游戏
        ModCreativeTabs.register(modEventBus); // 创造模式物品栏

        ModItems.register(modEventBus); // 注册物品
        ModBlocks.register(modEventBus); // 注册物品
    }

    private void commonSetup(final FMLCommonSetupEvent event) {}
}
