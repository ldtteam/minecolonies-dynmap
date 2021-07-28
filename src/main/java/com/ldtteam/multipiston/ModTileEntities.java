package com.ldtteam.multiblock;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

import static com.ldtteam.multiblock.MultiPiston.MOD_ID;

@ObjectHolder(MOD_ID)
public final class ModTileEntities
{
    private ModTileEntities() { /* prevent construction */ }

    private static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MOD_ID);

    public static DeferredRegister<BlockEntityType<?>> getRegistry()
    {
        return TILE_ENTITIES;
    }

    @ObjectHolder("multiblock")
    public static BlockEntityType<? extends TileEntityMultiBlock> MULTIBLOCK;

    //rename to multipiston
    static
    {
        getRegistry().register("multiblock",
          () -> BlockEntityType.Builder.of(TileEntityMultiBlock::new, ModBlocks.multiBlock.get()).build(null));
    }
}
