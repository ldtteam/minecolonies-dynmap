package com.ldtteam.minecoloniesdynmap.integration;

import com.ldtteam.minecoloniesdynmap.area.AreaGenerator;
import com.ldtteam.minecoloniesdynmap.util.Log;
import com.minecolonies.api.colony.IColony;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.ldtteam.minecoloniesdynmap.Constants.*;

/**
 * Dynmap integration class to connect Minecolonies to Dynmap it's API listening system.
 * This way Dynmap can invoke {@link DynmapIntegration#apiEnabled} in order to
 * start generating markers.
 */
public class DynmapIntegration extends DynmapCommonAPIListener
{
    private static DynmapIntegration instance;

    private MarkerSet colonySet;

    /**
     * Constructor containing a consumer callback which is invoked whenever Dynmap is ready
     * to start creating markers.
     */
    public DynmapIntegration()
    {
        DynmapCommonAPIListener.register(this);
    }

    /**
     * Start the Dynmap singleton.
     */
    public static void startInstance()
    {
        instance = new DynmapIntegration();
    }

    /**
     * Get the api listener instance.
     *
     * @return the api listener instance.
     */
    public static Optional<DynmapIntegration> getInstance()
    {
        if (instance.colonySet == null)
        {
            return Optional.empty();
        }
        return Optional.of(instance);
    }

    @Override
    public void apiEnabled(final DynmapCommonAPI dynmapCommonAPI)
    {
        Log.getLogger().info("Dynmap API enabled, registering markers...");
        MarkerAPI markerApi = dynmapCommonAPI.getMarkerAPI();

        final MarkerIcon outpostIcon = registerIcon(markerApi, "outpost");
        final MarkerIcon hamletIcon = registerIcon(markerApi, "hamlet");
        final MarkerIcon villageIcon = registerIcon(markerApi, "village");
        final MarkerIcon cityIcon = registerIcon(markerApi, "city");

        Set<MarkerIcon> icons = new HashSet<>();
        icons.add(outpostIcon);
        icons.add(hamletIcon);
        icons.add(villageIcon);
        icons.add(cityIcon);

        try (DynmapWebFiles fileWriter = new DynmapWebFiles())
        {
            fileWriter.writeWebFile("minecolonies.css", DynmapWebFiles.FileType.CSS, true);
            fileWriter.writeWebFile("minecolonies.css.map", DynmapWebFiles.FileType.CSS, false);
            fileWriter.writeWebFile("minecolonies.js", DynmapWebFiles.FileType.JS, true);
        }

        this.colonySet = markerApi.createMarkerSet(DYNMAP_COLONY_MARKER_SET_ID, DYNMAP_COLONY_MARKER_SET_NAME, icons, false);
        this.colonySet.setDefaultMarkerIcon(outpostIcon);
    }

    @Override
    public void apiDisabled(final DynmapCommonAPI api)
    {
        ColonyMarker.deleteAll();
        colonySet.deleteMarkerSet();
    }

    /**
     * Registers a new icon into the marker API.
     *
     * @param markerApi the marker API instance.
     * @param name      the name of the new icon.
     * @return the created marker icon.
     */
    private MarkerIcon registerIcon(MarkerAPI markerApi, String name)
    {
        MarkerIcon icon = markerApi.getMarkerIcon(DYNMAP_MARKER_ICON_ID.formatted(name.toLowerCase()));
        final InputStream inputStream = this.getClass().getResourceAsStream(String.format("/assets/textures/colony_%s.png", name.toLowerCase()));
        if (icon == null)
        {
            icon =
              markerApi.createMarkerIcon(DYNMAP_MARKER_ICON_ID.formatted(name.toLowerCase()), name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase(), inputStream);
        }
        else
        {
            icon.setMarkerIconImage(inputStream);
        }
        return icon;
    }

    /**
     * Creates a colony marker on the map, based on the provided colony.
     *
     * @param colony the colony.
     */
    public void createColony(final IColony colony)
    {
        final ColonyMarker colonyMarker = ColonyMarker.getMarker(colony, colonySet);
        colonyMarker.updateName();
        colonyMarker.updateTeamColor();
        colonyMarker.updateCitizenCount();
        colonyMarker.updateBorders();
    }

    /**
     * Updates the name for the given colony.
     *
     * @param colony the colony.
     */
    public void updateName(final IColony colony)
    {
        final ColonyMarker colonyMarker = ColonyMarker.getMarker(colony, colonySet);
        colonyMarker.updateName();
    }

    /**
     * Updates the team color for a given colony.
     * Changes the line and background style for the colony marker.
     *
     * @param colony the colony.
     */
    public void updateTeamColor(final IColony colony)
    {
        final ColonyMarker colonyMarker = ColonyMarker.getMarker(colony, colonySet);
        colonyMarker.updateTeamColor();
    }

    /**
     * Updates the name for the given colony.
     *
     * @param colony the colony.
     */
    public void updateCitizenCount(final IColony colony)
    {
        final ColonyMarker colonyMarker = ColonyMarker.getMarker(colony, colonySet);
        colonyMarker.updateCitizenCount();
    }

    /**
     * Updates the borders for the given colony. This will recalculate the borders by using the {@link AreaGenerator}.
     *
     * @param colony the colony.
     */
    public void updateBuildings(final IColony colony)
    {
        final ColonyMarker colonyMarker = ColonyMarker.getMarker(colony, colonySet);
        colonyMarker.updateBuildingCount();
        colonyMarker.updateBorders();
    }

    /**
     * Deletes a colony marker from Dynmap.
     *
     * @param colony the colony.
     */
    public void deleteColony(final IColony colony)
    {
        ColonyMarker.deleteMarker(colony);
    }
}