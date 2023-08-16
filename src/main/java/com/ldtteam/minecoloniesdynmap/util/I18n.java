package com.ldtteam.minecoloniesdynmap.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.locating.ModFileLoadingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class acts as a server-side utility method to parse translation keys from the server directly.
 * Because Minecraft doesn't allow this natively, we have to parse the raw language files to be able to do this.
 */
public class I18n
{
    /**
     * Cached list of the translation keys per mod.
     */
    private static final Map<String, Map<String, String>> TRANSLATION_KEYS = new HashMap<>();

    private I18n()
    {
    }

    /**
     * Obtain the translation value for a translation key within a given mod.
     *
     * @param modId          the mod to get the translation from.
     * @param translationKey the translation key.
     * @return the translated value in the en_us locale.
     */
    public static String translate(String modId, String translationKey)
    {
        if (!TRANSLATION_KEYS.containsKey(modId))
        {
            final Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(modId);
            if (modContainer.isEmpty())
            {
                throw new ModFileLoadingException(String.format("Mod with %s is not present in the mod list", modId));
            }

            Path jsonPath = modContainer.get().getModInfo().getOwningFile().getFile().findResource(String.format("assets/%s/lang/en_us.json", modId));
            try (final BufferedReader jsonReader = Files.newBufferedReader(jsonPath))
            {
                TRANSLATION_KEYS.put(modId, new Gson().fromJson(jsonReader, new TypeToken<Map<String, String>>() {}.getType()));
            }
            catch (IOException e)
            {
                Log.getLogger().error(e);
            }
        }

        return TRANSLATION_KEYS.get(modId).getOrDefault(translationKey, translationKey);
    }
}