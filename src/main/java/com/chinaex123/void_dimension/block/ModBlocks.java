package com.chinaex123.void_dimension.block;

import com.chinaex123.void_dimension.VoidDimension;
import com.chinaex123.void_dimension.dimServer.VoidPortal;
import com.chinaex123.void_dimension.item.ModItems;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS_REGISTER =
            DeferredRegister.createBlocks(VoidDimension.MOD_ID);

    // 注册方块
    public static final DeferredBlock<Block> NAUGHT_STONE = registerBlock("naught_stone",
            properties -> new Block(properties.strength(1.5F, 6.0F)
                    .mapColor(MapColor.STONE)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()), true);

    public static final DeferredBlock<Block> VOID_PORTAL = registerBlock("void_portal",
            properties -> new VoidPortal(properties.noOcclusion()
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 15)
                    .strength(-1.0F, 3600000.0F)), false);

    private static <T extends Block>DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> func, boolean shouldRegisterItem) {
        DeferredBlock<T> block = BLOCKS_REGISTER.registerBlock(name, func);
        if (shouldRegisterItem) ModItems.ITEMS_REGISTER.registerSimpleBlockItem(block);
        return block;
    }

    // 注册到游戏
    public static void register(IEventBus eventBus){
        BLOCKS_REGISTER.register(eventBus);
    }
}
