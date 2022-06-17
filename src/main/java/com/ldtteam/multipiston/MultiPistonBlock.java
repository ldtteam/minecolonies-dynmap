package com.ldtteam.multipiston;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This Class is about the multipiston which takes care of pushing others around (In a non mean way).
 */
public class MultiPistonBlock extends BaseEntityBlock
{
    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 1F;

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Constructor for the Substitution block.
     * sets the creative tab, as well as the resistance and the hardness.
     */
    public MultiPistonBlock()
    {
        super(Properties.of(Material.STONE).strength(BLOCK_HARDNESS, RESISTANCE).isRedstoneConductor((a,b,c) -> true));
    }

    /**
     * The blocks shape.
     */
    private static final VoxelShape SHAPE = Block.box(0.01D, 0.01D, 0.01D, 15.99D, 15.99D, 15.99D);

    @NotNull
    @Override
    public InteractionResult use(@NotNull final BlockState state, final Level level, @NotNull final BlockPos pos, @NotNull final Player player, @NotNull final InteractionHand hand, @NotNull final BlockHitResult hitResult)
    {
        if (level.isClientSide)
        {
            new WindowMultiPiston(pos).open();
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
        return createTickerHelper(type, ModTileEntities.multipiston.get(), (l, pos, s, te) -> te.tick());
    }

    @NotNull
    @Override
    public RenderShape getRenderShape(@NotNull BlockState state)
    {
        return RenderShape.MODEL;
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull final BlockState state, @NotNull final BlockGetter getter, @NotNull final BlockPos pos, @NotNull final CollisionContext context)
    {
        return SHAPE;
    }
}
