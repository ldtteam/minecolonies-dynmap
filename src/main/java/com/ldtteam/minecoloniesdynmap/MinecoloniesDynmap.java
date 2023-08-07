package com.ldtteam.minecoloniesdynmap;

import com.ldtteam.minecoloniesdynmap.integration.DynmapIntegration;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class MinecoloniesDynmap
{
    public MinecoloniesDynmap()
    {
        DynmapIntegration.startInstance();
    }
}