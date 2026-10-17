package com.chinaex123.void_dimension.init;

import com.chinaex123.void_dimension.VoidDimension;
import com.chinaex123.void_dimension.block.VoidPortal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class VDBlocks {
    public static final DeferredRegister.Blocks BLOCK_REGISTER =
            DeferredRegister.createBlocks(VoidDimension.MOD_ID);

    /* 归墟基石 */
    public static final DeferredBlock<Block> NAUGHT_STONE =
            registerBlocks("naught_stone", () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1.5F, 6.0F)
                    .mapColor(MapColor.STONE)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    /* 虚空传送门 */
    public static final DeferredBlock<Block> VOID_PORTAL =
            BLOCK_REGISTER.register("void_portal", () -> new VoidPortal(BlockBehaviour.Properties.of()
                    .noCollission()
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 15)
                    .strength(-1.0F, 3600000.0F)));


    public static <T extends Block> void registerBlockItems(String name, DeferredBlock<T> block) {
        VDItems.ITEMS_REGISTER.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static <T extends Block> DeferredBlock<T> registerBlocks(String name, Supplier<T> block) {
        DeferredBlock<T> blocks = BLOCK_REGISTER.register(name, block);
        registerBlockItems(name, blocks);
        return blocks;
    }

    public static void register(IEventBus eventBus){
        BLOCK_REGISTER.register(eventBus);
    }
}
