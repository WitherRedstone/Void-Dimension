package com.chinaex123.void_dimension.register;

import com.chinaex123.void_dimension.VoidDimension;
import com.chinaex123.void_dimension.dimServer.VoidPortal;
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

public class ModBlocks {
    // 创建方块注册器实例
    public static final DeferredRegister<Block> BLOCK_REGISTER =
            DeferredRegister.create(ForgeRegistries.BLOCKS, VoidDimension.MOD_ID);

    // 注册方块
    public static final RegistryObject<Block> NAUGHT_STONE =
            registerBlocks("naught_stone", () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1.5F, 6.0F)
                    .mapColor(MapColor.STONE)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> VOID_PORTAL =
            BLOCK_REGISTER.register("void_portal", () -> new VoidPortal(BlockBehaviour.Properties.of()
                    .noCollission()
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 15)
                    .strength(-1.0F, 3600000.0F)));


    // 注册一个方块物品到物品注册表中
    public static <T extends Block> void registerBlockItems(String name, RegistryObject<T> block) {
        ModItems.ITEMS_REGISTER.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    // 注册方块的同时注册方块物品
    public static <T extends Block> RegistryObject<T> registerBlocks(String name, Supplier<T> block) {
        RegistryObject<T> blocks = BLOCK_REGISTER.register(name, block);
        registerBlockItems(name, blocks);
        return blocks;
    }

    // 注册到游戏
    public static void register(IEventBus eventBus){
        BLOCK_REGISTER.register(eventBus);
    }
}
