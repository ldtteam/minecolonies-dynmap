package com.ldtteam.minecoloniesdynmap;

import com.ldtteam.minecoloniesdynmap.integration.DynmapIntegration;
import com.minecolonies.api.colony.buildings.event.BuildingConstructionEvent;
import com.minecolonies.api.colony.citizens.event.CitizenAddedEvent;
import com.minecolonies.api.colony.event.ColonyCreatedEvent;
import com.minecolonies.api.colony.event.ColonyDeletedEvent;
import com.minecolonies.api.colony.event.ColonyInformationChangedEvent;
import com.minecolonies.api.colony.managers.events.ColonyManagerLoadedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

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
    public static void onColonyManagerLoaded(ColonyManagerLoadedEvent event)
    {
        event.getColonyManager().getAllColonies().forEach(colony -> run(integration -> integration.createColony(colony)));
    }

    /**
     * Util method that wraps the optional chain for getting the integration.
     *
     * @param callback the code to execute if the Dynmap integration is active.
     */
    private static void run(Consumer<DynmapIntegration> callback)
    {
        DynmapIntegration.getInstance().ifPresent(callback);
    }

    @SubscribeEvent
    public static void onColonyCreated(ColonyCreatedEvent event)
    {
        run(integration -> integration.createColony(event.getColony()));
    }

    @SubscribeEvent
    public static void onColonyDeleted(ColonyDeletedEvent event)
    {
        run(integration -> integration.deleteColony(event.getColony()));
    }

    @SubscribeEvent
    public static void onColonyInformationChanged(ColonyInformationChangedEvent event)
    {
        if (event.getType() == ColonyInformationChangedEvent.Type.NAME)
        {
            run(integration -> integration.updateName(event.getColony()));
        }
        else if (event.getType() == ColonyInformationChangedEvent.Type.TEAM_COLOR)
        {
            run(integration -> integration.updateTeamColor(event.getColony()));
        }
    }

    @SubscribeEvent
    public static void onColonyBuildingConstruction(BuildingConstructionEvent event)
    {
        if (event.getEventType() == BuildingConstructionEvent.EventType.BUILT
              || event.getEventType() == BuildingConstructionEvent.EventType.UPGRADED
              || event.getEventType() == BuildingConstructionEvent.EventType.REMOVED)
        {
            run(integration -> integration.updateBuildings(event.getColony()));
        }
    }

    @SubscribeEvent
    public static void onCitizenAdded(CitizenAddedEvent event)
    {
        run(integration -> integration.updateCitizenCount(event.getColony()));
    }
}