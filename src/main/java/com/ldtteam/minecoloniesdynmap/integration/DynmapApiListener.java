package com.ldtteam.minecoloniesdynmap.integration;

import com.ldtteam.minecoloniesdynmap.util.Log;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.MarkerAPI;

import java.util.Optional;

/**
 * Dynmap integration class to connect Minecolonies to Dynmap it's API listening system.
 * This way Dynmap can invoke {@link DynmapApiListener#apiEnabled} in order to
 * start generating markers.
 */
public class DynmapApiListener extends DynmapCommonAPIListener
{
    private static DynmapApiListener instance;

    private DynmapIntegration integration;

    /**
     * Constructor containing a consumer callback which is invoked whenever Dynmap is ready
     * to start creating markers.
     */
    public DynmapApiListener()
    {
        DynmapCommonAPIListener.register(this);
    }

    /**
     * Get the api listener instance.
     *
     * @return the api listener instance.
     */
    public static DynmapApiListener getInstance()
    {
        if (instance == null)
        {
            instance = new DynmapApiListener();
        }
        return instance;
    }

    @Override
    public void apiEnabled(final DynmapCommonAPI dynmapCommonAPI)
    {
        Log.getLogger().info("Dynmap API enabled, registering markers...");
        MarkerAPI markerApi = dynmapCommonAPI.getMarkerAPI();

        integration = new DynmapIntegration(markerApi);
    }

    /**
     * Get the integration, if present.
     *
     * @return the integration instance, or an empty optional.
     */
    public Optional<DynmapIntegration> getIntegration()
    {
        return Optional.ofNullable(integration);
    }
}