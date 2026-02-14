package com.chinaex123.void_dimension.register;

import com.chinaex123.void_dimension.VoidDimension;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS_REGISTER =
            DeferredRegister.createItems(VoidDimension.MOD_ID);

    public static final DeferredItem<Item> NAUGHT_SHARD =
            ITEMS_REGISTER.register("naught_shard", () -> new Item(new Item.Properties()
                    .durability(4)));

    // 注册到游戏
    public static void register(IEventBus eventBus){
        ITEMS_REGISTER.register(eventBus);
    }
}
