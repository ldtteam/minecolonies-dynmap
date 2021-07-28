package com.ldtteam.multiblock;

import com.ldtteam.multiblock.network.Network;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class LifeCycleEvents
{
    @SubscribeEvent
    public static void onModInit(final FMLCommonSetupEvent event)
    {
        Network.getNetwork().registerCommonMessages();
    }
}
