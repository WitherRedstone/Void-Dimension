package com.chinaex123.void_dimension.init;

import com.chinaex123.void_dimension.VoidDimension;
import com.chinaex123.void_dimension.block.VoidPortal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public interface VDBlocks {
    DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, VoidDimension.MOD_ID);

    /* 归墟基石 */
    RegistryObject<Block> NAUGHT_STONE = registerBlocks("naught_stone",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1.5F, 6.0F)
                    .mapColor(MapColor.STONE)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    /* 虚空传送门 */
    RegistryObject<Block> VOID_PORTAL = BLOCK_REGISTER.register("void_portal",
            () -> new VoidPortal(BlockBehaviour.Properties.of()
                    .noCollission()
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 15)
                    .strength(-1.0F, 3600000.0F)));

    static <T extends Block> void registerBlockItems(String name, RegistryObject<T> block) {
        VDItems.ITEMS_REGISTER.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    static <T extends Block> RegistryObject<T> registerBlocks(String name, Supplier<T> block) {
        RegistryObject<T> blocks = BLOCK_REGISTER.register(name, block);
        registerBlockItems(name, blocks);
        return blocks;
    }

    static void register(IEventBus eventBus){
        BLOCK_REGISTER.register(eventBus);
    }
}
