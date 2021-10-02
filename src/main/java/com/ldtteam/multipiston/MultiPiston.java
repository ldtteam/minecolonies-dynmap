package com.ldtteam.multipiston;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.ldtteam.multipiston.ModBlocks.BLOCKS;
import static com.ldtteam.multipiston.ModBlocks.ITEMS;
import static com.ldtteam.multipiston.ModTileEntities.TILE_ENTITIES;

@Mod("multipiston")
public class MultiPiston
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "multipiston";

    public MultiPiston()
    {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILE_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(LifeCycleEvents.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(this.getClass());
    }
}
