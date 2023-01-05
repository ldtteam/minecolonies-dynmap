package com.ldtteam.multipiston;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;
import java.util.function.Supplier;

import static com.ldtteam.multipiston.MultiPiston.MOD_ID;

public class ModBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final     DeferredRegister<Item>  ITEMS  = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<MultiPistonBlock> multipiston = register("multipistonblock", MultiPistonBlock::new);

    /**
     * Utility shorthand to register blocks using the deferred registry
     * @param name the registry name of the block
     * @param block a factory / constructor to create the block on demand
     * @param <B> the block subclass for the factory response
     * @return the block entry saved to the registry
     */
    public static <B extends Block> RegistryObject<B> register(String name, Supplier<B> block)
    {
        RegistryObject<B> registered = BLOCKS.register(name.toLowerCase(Locale.ENGLISH), block);
        ITEMS.register(name.toLowerCase(Locale.ENGLISH), () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }
}
