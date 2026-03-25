package com.chinaex123.void_dimension.item;

import com.chinaex123.void_dimension.VoidDimension;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public class ModItems {
    public static final DeferredRegister.Items ITEMS_REGISTER = DeferredRegister.createItems(VoidDimension.MOD_ID);

    public static final DeferredItem<@NotNull Item> NAUGHT_SHARD = ITEMS_REGISTER.registerSimpleItem("naught_shard",
            props -> props.durability(4));

    public static void register(IEventBus eventBus) {
        ITEMS_REGISTER.register(eventBus);
    }
}
