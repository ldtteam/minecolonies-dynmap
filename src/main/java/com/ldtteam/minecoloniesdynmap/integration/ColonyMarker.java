package com.ldtteam.minecoloniesdynmap.integration;

import com.ldtteam.minecoloniesdynmap.area.AreaGenerator;
import com.ldtteam.minecoloniesdynmap.area.ColonyArea;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.constant.CitizenConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.ServerLevelData;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.ldtteam.minecoloniesdynmap.Constants.*;

/**
 * Combination class of all markers that are rendered on a colony.
 * Currently, the following markers are:
 * - Colony area marker, showing borders of the colony.
 * - Townhall marker, showing the position where the townhall is, if any (else it shows the center of the colony).
 */
public class ColonyMarker
{
    private static final Map<Integer, ColonyMarker> MARKERS = new HashMap<>();

    private final IColony    colony;
    private final MarkerSet  colonySet;
    private final Marker     townhallMarker;
    private final AreaMarker areaMarker;

    private ColonyMarker(final IColony colony, final MarkerSet colonySet)
    {
        this.colony = colony;
        this.colonySet = colonySet;
        this.areaMarker = createAreaMarker(colony, colonySet);
        this.townhallMarker = createTownHallMarker(colony, colonySet);
    }

    /**
     * Finds the marker for the given colony, or creates it if not present.
     *
     * @param colony the colony.
     * @return the marker instance.
     */
    @NotNull
    private static AreaMarker createAreaMarker(final IColony colony, final MarkerSet colonySet)
    {
        String colonyId = getColonyAreaId(colony);
        ServerLevelData levelData = (ServerLevelData) colony.getWorld().getLevelData();
        return colonySet.createAreaMarker(
          colonyId,
          "",
          false,
          levelData.getLevelName(),
          new double[0],
          new double[0],
          false
        );
    }

    /**
     * Creates the marker for the town hall.
     *
     * @param colony the colony.
     * @return the marker instance.
     */
    @NotNull
    private static Marker createTownHallMarker(final IColony colony, final MarkerSet colonySet)
    {
        String colonyId = getColonyTownHallId(colony);
        ServerLevelData levelData = (ServerLevelData) colony.getWorld().getLevelData();
        BlockPos originPoint = getColonyOriginPoint(colony);

        return colonySet.createMarker(
          colonyId,
          colony.getName(),
          true,
          levelData.getLevelName(),
          originPoint.getX(),
          originPoint.getY(),
          originPoint.getZ(),
          getColonyIcon(colony, colonySet),
          false
        );
    }

    /**
     * Internal method to get the colony ID known to Dynmap.
     *
     * @param colony the colony to get the ID for.
     * @return the colony ID value.
     */
    private static String getColonyAreaId(final IColony colony)
    {
        return String.format(DYNMAP_COLONY_AREA_MARKER_FORMAT, colony.getID());
    }

    /**
     * Internal method to get the colony town hall ID known to Dynmap.
     *
     * @param colony the colony to get the ID for.
     * @return the colony ID value.
     */
    private static String getColonyTownHallId(final IColony colony)
    {
        return String.format(DYNMAP_COLONY_TOWN_HALL_MARKER_FORMAT, colony.getID());
    }

    /**
     * Get the origin point of the colony, if there is a town hall, that will be used first, afterward the center of the claim area.
     *
     * @param colony the colony.
     * @return the origin position of the colony.
     */
    private static BlockPos getColonyOriginPoint(final IColony colony)
    {
        return colony.hasTownHall() ? colony.getBuildingManager().getTownHall().getID() : colony.getCenter();
    }

    /**
     * Get the marker icon for the given colony, based on population count.
     *
     * @param colony    the colony.
     * @param colonySet the marker set for Minecolonies.
     * @return the origin position of the colony.
     */
    private static MarkerIcon getColonyIcon(final IColony colony, final MarkerSet colonySet)
    {
        int currentCitizens = colony.getCitizenManager().getCitizens().size();
        String icon;
        if (currentCitizens >= CitizenConstants.CITIZEN_LIMIT_VILLAGE)
        {
            icon = "city";
        }
        else if (currentCitizens >= CitizenConstants.CITIZEN_LIMIT_HAMLET)
        {
            icon = "village";
        }
        else if (currentCitizens >= CitizenConstants.CITIZEN_LIMIT_OUTPOST)
        {
            icon = "hamlet";
        }
        else
        {
            icon = "outpost";
        }

        return colonySet.getAllowedMarkerIcons()
                 .stream()
                 .filter(f -> f.getMarkerIconID().equals(DYNMAP_MARKER_ICON_ID.formatted(icon)))
                 .findFirst()
                 .orElse(colonySet.getDefaultMarkerIcon());
    }

    /**
     * Obtain a colony marker from a cached list of markers.
     *
     * @param colony    the colony to load the marker for.
     * @param colonySet the marker set provided by the marker API from Dynmap.
     * @return the marker instance.
     */
    public static ColonyMarker getMarker(final IColony colony, final MarkerSet colonySet)
    {
        ColonyMarker marker = MARKERS.get(colony.getID());
        if (marker == null)
        {
            marker = new ColonyMarker(colony, colonySet);
            MARKERS.put(colony.getID(), marker);
        }
        return marker;
    }

    /**
     * Delete information of all colonies.
     * Used upon shutdown.
     */
    public static void deleteAll()
    {
        for (ColonyMarker marker : MARKERS.values())
        {
            ColonyMarker.deleteMarker(marker.colony);
        }
    }

    /**
     * Delete a marker, given the colony.
     *
     * @param colony the colony.
     */
    public static void deleteMarker(final IColony colony)
    {
        final ColonyMarker marker = MARKERS.get(colony.getID());
        if (marker != null)
        {
            marker.delete();
            MARKERS.remove(colony.getID());
        }
    }

    /**
     * Delete the internal markers for this colony marker.
     */
    private void delete()
    {
        townhallMarker.deleteMarker();
        areaMarker.deleteMarker();
    }

    /**
     * Updates the team color for a given colony.
     * Changes the line and background style for the colony marker.
     */
    public void updateTeamColor()
    {
        Integer color = colony.getTeam().getColor().getColor();
        if (color == null)
        {
            color = Objects.requireNonNull(ChatFormatting.WHITE.getColor());
        }

        // We cannot directly call `Color.getRGB()` because this includes the alpha bytes, which Dynmap it's line style does not expect.
        // Thus, we manually have to extract the RGB bits.
        Color darkerColor = new Color(color).darker();
        int borderColor = (darkerColor.getRed() & 0xff) << 16 | (darkerColor.getGreen() & 0xff) << 8 | darkerColor.getBlue() & 0xff;

        areaMarker.setLineStyle(areaMarker.getLineWeight(), areaMarker.getLineOpacity(), borderColor);
        areaMarker.setFillStyle(areaMarker.getFillOpacity(), color);
    }

    /**
     * Updates the name for the given colony.
     */
    public void updateName()
    {
        townhallMarker.setLabel(colony.getName());
        updateTownHallDescription();
    }

    /**
     * Internally updates the description popover for the townhall of the colony on the map.
     * Includes changes to the name, owner and citizen count.
     */
    private void updateTownHallDescription()
    {
        townhallMarker.setDescription(buildArgumentString(context -> {
            context.put("colony", colony.getName());
            context.put("icon", getColonyIcon(colony, colonySet).getMarkerIconID());
            context.put("mayor", colony.getPermissions().getOwnerName());
            context.put("style", colony.getStructurePack());

            final List<IBuilding> buildings = colony.getBuildingManager().getBuildings().values().stream()
                                                .filter(f -> !Objects.equals(ModBuildings.postBox.get(), f.getBuildingType()) && !Objects.equals(ModBuildings.stash.get(),
                                                  f.getBuildingType()))
                                                .toList();
            context.put("building_count", buildings.size());
            Map<BuildingEntry, Integer> buildingCounts = new HashMap<>();
            for (IBuilding building : buildings)
            {
                BuildingEntry type = building.getBuildingType();
                buildingCounts.put(type, buildingCounts.containsKey(type) ? buildingCounts.get(building.getBuildingType()) + 1 : 1);
            }
            for (Map.Entry<BuildingEntry, Integer> entry : buildingCounts.entrySet())
            {
                final String key = String.format("building_info_%s", entry.getKey().getRegistryName().getPath());
                context.put(key, entry.getValue().toString() + ":" + I18n.get(entry.getKey().getTranslationKey()));
            }

            final List<ICitizenData> citizens = colony.getCitizenManager().getCitizens();
            context.put("citizen_count", citizens.size());
            for (ICitizenData citizen : citizens)
            {
                final String key = String.format("citizen_info_%s", citizen.getId());
                final Optional<String> job = Optional.ofNullable(citizen.getJob())
                                               .map(IJob::getJobRegistryEntry)
                                               .map(JobEntry::getTranslationKey)
                                               .map(I18n::get);
                context.put(key,
                  Base64.getEncoder().encodeToString(citizen.getName().getBytes()) + job.map(j -> ":" + Base64.getEncoder().encodeToString(j.getBytes())).orElse(""));
            }
        }));
    }

    /**
     * Builds the argument string meant to put in the description of a marker.
     *
     * @param consumer the variable collector.
     * @return the resulting string.
     */
    private static String buildArgumentString(Consumer<Map<String, Object>> consumer)
    {
        Map<String, Object> args = new HashMap<>();
        consumer.accept(args);

        String arguments = args.entrySet().stream()
                             .map(m -> m.getKey() + ":" + Base64.getEncoder().encodeToString(m.getValue().toString().getBytes()))
                             .collect(Collectors.joining(";"));

        return String.format("<div><img alt=\"minecolonies;%s\" /><div></div></div>", arguments);
    }

    /**
     * Updates the name for the given colony.
     */
    public void updateCitizenCount()
    {
        updateTownHallDescription();
        townhallMarker.setMarkerIcon(getColonyIcon(colony, colonySet));
    }

    /**
     * Updates the borders for the given colony. This will recalculate the borders by using the {@link AreaGenerator}.
     */
    public void updateBorders()
    {
        Collection<ChunkPos> claimedChunks = ColonyChunkClaimCalculator.getAllClaimedChunks(colony);
        ColonyArea area = AreaGenerator.generateAreaFromChunks(claimedChunks);
        areaMarker.setCornerLocations(area.toXArray(), area.toZArray());

        BlockPos originPosition = getColonyOriginPoint(colony);
        townhallMarker.setLocation(townhallMarker.getWorld(), originPosition.getX(), originPosition.getY(), originPosition.getZ());
    }

    /**
     * Updates the count of buildings in the colony.
     */
    public void updateBuildingCount()
    {
        updateTownHallDescription();
    }
}
