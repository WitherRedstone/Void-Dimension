package com.chinaex123.void_dimension.register;

import com.chinaex123.void_dimension.VoidDimension;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS_REGISTER =
            DeferredRegister.create(ForgeRegistries.ITEMS, VoidDimension.MOD_ID);

    public static final RegistryObject<Item> NAUGHT_SHARD =
            ITEMS_REGISTER.register("naught_shard", () -> new Item(new Item.Properties()
                    .durability(4)));

    // 注册到游戏
    public static void register(IEventBus eventBus){
        ITEMS_REGISTER.register(eventBus);
    }
}
