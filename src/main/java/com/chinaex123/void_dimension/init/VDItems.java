package com.chinaex123.void_dimension.init;

import com.chinaex123.void_dimension.VoidDimension;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public interface VDItems {
    DeferredRegister<Item> ITEMS_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, VoidDimension.MOD_ID);

    RegistryObject<Item> NAUGHT_SHARD = ITEMS_REGISTER.register("naught_shard",
            () -> new Item(new Item.Properties().durability(4)));

    // 注册到游戏
    static void register(IEventBus eventBus){
        ITEMS_REGISTER.register(eventBus);
    }
}
