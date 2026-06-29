package com.chinaex123.void_dimension.init;

import com.chinaex123.void_dimension.VoidDimension;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class VDCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VoidDimension.MOD_ID);

    public static final Supplier<CreativeModeTab> VOID_DIMENSION_TAB =
            CREATIVE_MODE_TAB.register("void_dimension_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(VDBlocks.NAUGHT_STONE.get()))
                    .title(Component.translatable("itemGroup.void_dimension"))
                    .displayItems((parameters, output) -> {

                        output.accept(VDBlocks.NAUGHT_STONE.get()); // 归墟基石
                        output.accept(VDItems.NAUGHT_SHARD.get()); // 湮灭碎片

                    }).build());

    // 注册到NeoForge事件总线里
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
