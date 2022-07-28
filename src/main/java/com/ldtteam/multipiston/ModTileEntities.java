package com.ldtteam.multipiston;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.ldtteam.multipiston.MultiPiston.MOD_ID;

public final class ModTileEntities
{
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);

    private ModTileEntities() { /* prevent construction */ }

    public static RegistryObject<BlockEntityType<TileEntityMultiPiston>>
      multipiston = TILE_ENTITIES.register("multipistonte", () -> BlockEntityType.Builder.of(TileEntityMultiPiston::new, ModBlocks.multipiston.get()).build(null));
}
