package com.ldtteam.multipiston;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This Class is about the MultiBlock which takes care of pushing others around (In a non mean way).
 */
public class MultiBlock extends BaseEntityBlock
{

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 0.0F;

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Constructor for the Substitution block.
     * sets the creative tab, as well as the resistance and the hardness.
     */
    public MultiBlock()
    {
        super(Properties.of(Material.WOOD).strength(BLOCK_HARDNESS, RESISTANCE));
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull final BlockState state, final Level level, @NotNull final BlockPos pos, @NotNull final Player player, @NotNull final InteractionHand hand, @NotNull final BlockHitResult hitResult)
    {
        if (level.isClientSide)
        {
            new WindowMultiBlock(pos).open();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void neighborChanged(@NotNull final BlockState state, final Level level, @NotNull final BlockPos pos, @NotNull final Block block, @NotNull final BlockPos fromPos, final boolean isMoving)
    {
        if(level.isClientSide)
        {
            return;
        }
        final BlockEntity te = level.getBlockEntity(pos);
        if(te instanceof TileEntityMultiPiston)
        {
            ((TileEntityMultiPiston) te).handleRedstone(level.hasNeighborSignal(pos));
        }
    }

    @Override
    public BlockState rotate(final BlockState state, final LevelAccessor world, final BlockPos pos, final Rotation direction)
    {
        ((TileEntityMultiPiston) world.getBlockEntity(pos)).rotate(direction);
        return super.rotate(state, world, pos, direction);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos blockPos, @NotNull final BlockState blockState)
    {
        return new TileEntityMultiPiston(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull final Level level, @NotNull final BlockState state, @NotNull final BlockEntityType<T> type)
    {
        return createTickerHelper(type, ModTileEntities.MULTIBLOCK, TileEntityMultiPiston::tick);
    }
}
