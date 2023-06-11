package com.ldtteam.multipiston;

import com.google.common.primitives.Ints;
import com.ldtteam.structurize.api.util.IRotatableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minecraft.core.Direction.*;

/**
 * This Class is about the multipiston TileEntity which takes care of pushing others around (In a non mean way).
 */
public class TileEntityMultiPiston extends BlockEntity implements IRotatableBlockEntity
{
    /**
     * NBT tag constants for multipiston tileEntities.
     */
    public static final String TAG_INPUT            = "input";
    public static final String TAG_RANGE            = "range";
    public static final String TAG_DIRECTION        = "direction";
    public static final String TAG_LENGTH           = "length";
    public static final String TAG_PROGRESS         = "progress";
    public static final String TAG_OUTPUT_DIRECTION = "outputDirection";
    public static final String TAG_SPEED            = "speed";

    /**
     * Volume to play at.
     */
    public static final double VOLUME = 0.5D;

    /**
     * The base pitch, add more to this to change the sound.
     */
    public static final double PITCH = 0.8D;

    /**
     * Max block range.
     */
    private static final int MAX_RANGE = 10;

    /**
     * Max block speed.
     */
    private static final int MAX_SPEED = 3;

    /**
     * Min block speed.
     */
    private static final int MIN_SPEED = 1;

    /**
     * Default gate and bridge range.
     */
    public static final int DEFAULT_RANGE = 3;

    /**
     * Default gate and bridge range.
     */
    public static final int DEFAULT_SPEED = 2;

    /**
     * The last redstone state which got in.
     */
    private boolean on = false;

    /**
     * The direction it should push or pull rom.
     */
    private Direction input = UP;

    /**
     * The output direction.
     */
    private Direction output = DOWN;

    /**
     * The range it should pull to.
     */
    private int range = DEFAULT_RANGE;

    /**
     * The direction it is going to.
     */
    private Direction currentDirection;

    /**
     * The progress it has made.
     */
    private int progress = 0;

    /**
     * Amount of ticks passed.
     */
    private int ticksPassed = 0;

    /**
     * Speed of the multipiston, max 3, min 1.
     */
    private int speed = 2;

    public TileEntityMultiPiston(final BlockPos pos, final BlockState state)
    {
        super(ModTileEntities.multipiston.get(), pos, state);
    }

    /**
     * Handle redstone input.
     *
     * @param signal true if positive.
     */
    public void handleRedstone(final boolean signal)
    {
        if (speed == 0)
        {
            speed = DEFAULT_SPEED;
        }

        if (signal != on && progress == range)
        {
            on = signal;
            if (signal)
            {
                currentDirection = output;
            }
            else
            {
                currentDirection = input;
            }
            progress = 0;
        }
    }

    /**
     * Local tick method.
     */
    public void tick()
    {
        if (level == null || level.isClientSide)
        {
            return;
        }
        if (currentDirection == null && progress < range)
        {
            progress = range;
        }

        if (progress < range)
        {
            if (ticksPassed % (20 / speed) == 0)
            {
                handleTick();
                ticksPassed = 1;
            }
            ticksPassed++;
        }
    }

    /**
     * Handle the tick, to finish the sliding.
     */
    public void handleTick()
    {
        final Direction currentOutPutDirection = currentDirection == input ? output : input;

        if (progress < range)
        {
            final BlockState blockToMove = level.getBlockState(worldPosition.relative(currentDirection, 1));
            if (blockToMove.getBlock() == Blocks.AIR
                  || blockToMove.getPistonPushReaction() == PushReaction.IGNORE
                  || blockToMove.getPistonPushReaction() == PushReaction.DESTROY
                  || blockToMove.getPistonPushReaction() == PushReaction.BLOCK
                  || (blockToMove.getBlock() instanceof EntityBlock && !ForgeRegistries.BLOCKS.getKey(blockToMove.getBlock()).getNamespace().equals("domum_ornamentum"))
                  || blockToMove.getBlock() == Blocks.BEDROCK)
            {
                progress++;
                return;
            }

            for (int i = 0; i < Math.min(range, MAX_RANGE); i++)
            {
                final int blockToGoTo = i - 1 - progress + (i - 1 - progress >= 0 ? 1 : 0);
                final int blockToGoFrom = i + 1 - progress - (i + 1 - progress <= 0 ? 1 : 0);

                final BlockPos posToGo = blockToGoTo > 0 ? worldPosition.relative(currentDirection, blockToGoTo) : worldPosition.relative(currentOutPutDirection, Math.abs(blockToGoTo));
                final BlockPos posToGoFrom = blockToGoFrom > 0 ? worldPosition.relative(currentDirection, blockToGoFrom) : worldPosition.relative(currentOutPutDirection, Math.abs(blockToGoFrom));
                if (level.isEmptyBlock(posToGo) || level.getBlockState(posToGo).liquid())
                {
                    BlockState tempState = level.getBlockState(posToGoFrom);
                    if (blockToMove.getBlock() == tempState.getBlock() && level.hasChunkAt(posToGoFrom) && level.hasChunkAt(posToGo))
                    {
                        pushEntitiesIfNecessary(posToGo, worldPosition);

                        tempState = Block.updateFromNeighbourShapes(tempState, this.level, posToGo);
                        level.setBlock(posToGo, tempState, 67);
                        if (tempState.getBlock() instanceof BucketPickup)
                        {
                            ((BucketPickup) tempState.getBlock()).pickupBlock(level, posToGo, tempState);
                        }
                        this.level.neighborChanged(posToGo, tempState.getBlock(), posToGo);

                        if (tempState.getBlock() instanceof EntityBlock)
                        {
                            final BlockEntity blockEntity = level.getBlockEntity(posToGoFrom);
                            if (blockEntity != null)
                            {
                                final CompoundTag tag = blockEntity.saveWithId();
                                final BlockEntity resultEntity = level.getBlockEntity(posToGo);
                                if (resultEntity != null)
                                {
                                    resultEntity.load(tag);
                                }
                            }
                        }

                        level.removeBlock(posToGoFrom, false);
                    }
                }
            }
            level.playSound(null,
              worldPosition,
              SoundEvents.PISTON_EXTEND,
              SoundSource.BLOCKS,
              (float) VOLUME,
              (float) PITCH);
            progress++;
        }
    }

    private void pushEntitiesIfNecessary(final BlockPos posToGo, final BlockPos pos)
    {
        final List<Entity> entities = level.getEntitiesOfClass(Entity.class, new AABB(posToGo));
        final BlockPos vector = posToGo.subtract(pos);
        final BlockPos posTo = posToGo.relative(getNearest(vector.getX(), vector.getY(), vector.getZ()));
        for (final Entity entity : entities)
        {
            entity.teleportTo(posTo.getX() + 0.5D, posTo.getY() + 0.5D, posTo.getZ() + 0.5D);
        }
    }

    /**
     * Our own rotate method.
     * @param rotationIn the incoming rotation.
     */
    @Override
    public void rotate(final Rotation rotationIn)
    {
        if (output != UP && output != DOWN)
        {
            output = rotationIn.rotate(output);
        }

        if (input != UP && input != DOWN)
        {
            input = rotationIn.rotate(input);
        }
    }

    /**
     * Our own mirror method.
     * @param mirrorIn the incoming mirror.
     */
    @Override
    public void mirror(final Mirror mirrorIn)
    {
        if (output != UP && output != DOWN)
        {
            output = mirrorIn.mirror(output);
        }

        if (input != UP && input != DOWN)
        {
            input = mirrorIn.mirror(input);
        }
    }

    /**
     * Check if the redstone is on.
     *
     * @return true if so.
     */
    public boolean isOn()
    {
        return on;
    }

    /**
     * Get the direction the block is facing.
     *
     * @return the EnumFacing.
     */
    public Direction getInput()
    {
        return input;
    }

    /**
     * Get the output direction the block is facing.
     *
     * @return the EnumFacing.
     */
    public Direction getOutput()
    {
        return output;
    }

    /**
     * Set the direction it should be facing.
     *
     * @param direction the direction.
     */
    public void setInput(final Direction direction)
    {
        this.input = direction;
    }

    /**
     * Set the direction it should output to.
     *
     * @param output the direction.
     */
    public void setOutput(final Direction output)
    {
        this.output = output;
    }

    /**
     * Get the range of blocks it should push.
     *
     * @return the range.
     */
    public int getRange()
    {
        return range;
    }

    /**
     * Set the range it should push.
     *
     * @param range the range.
     */
    public void setRange(final int range)
    {
        this.range = Math.min(range, MAX_RANGE);
        this.progress = range;
    }

    /**
     * Get the speed of the block.
     *
     * @return the speed (min 1 max 3).
     */
    public int getSpeed()
    {
        return speed;
    }

    /**
     * Setter for speed.
     *
     * @param speed the speed to set.
     */
    public void setSpeed(final int speed)
    {
        this.speed = Ints.constrainToRange(speed, MIN_SPEED, MAX_SPEED);
    }

    @Override
    public void load(@NotNull final CompoundTag compound)
    {
        super.load(compound);

        range = compound.getInt(TAG_RANGE);
        this.progress = compound.getInt(TAG_PROGRESS);
        input = values()[compound.getInt(TAG_DIRECTION)];
        on = compound.getBoolean(TAG_INPUT);
        if (compound.getAllKeys().contains(TAG_OUTPUT_DIRECTION))
        {
            output = values()[compound.getInt(TAG_OUTPUT_DIRECTION)];
        }
        else
        {
            output = input.getOpposite();
        }
        speed = compound.getInt(TAG_SPEED);
    }

    @Override
    public void saveAdditional(@NotNull final CompoundTag compound)
    {
        super.saveAdditional(compound);
        compound.putInt(TAG_RANGE, range);
        compound.putInt(TAG_PROGRESS, progress);
        compound.putInt(TAG_DIRECTION, input.ordinal());
        compound.putBoolean(TAG_INPUT, on);
        if (output != null)
        {
            compound.putInt(TAG_OUTPUT_DIRECTION, output.ordinal());
        }
        compound.putInt(TAG_SPEED, speed);
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag)
    {
        this.load(tag);
    }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket pkt)
    {
        this.load(pkt.getTag());
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        CompoundTag nbt = new CompoundTag();
        this.saveAdditional(nbt);
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag()
    {
        return saveWithId();
    }
}
