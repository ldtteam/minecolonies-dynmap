package com.ldtteam.minecoloniesdynmap;

import com.ldtteam.minecoloniesdynmap.integration.DynmapApiListener;
import com.minecolonies.api.colony.event.ColonyInformationChangedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event bus for receiving events from Minecolonies.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MinecoloniesEventBus
{
    private MinecoloniesEventBus()
    {
    }

    @SubscribeEvent
    public static void onColonyNameChanged(ColonyInformationChangedEvent event)
    {
        DynmapApiListener.getInstance().getIntegration().ifPresent(integration -> {
            // integration.updateName();
        });
    }
}
