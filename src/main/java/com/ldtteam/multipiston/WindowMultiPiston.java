package com.ldtteam.multipiston;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.mod.Log;
import com.ldtteam.blockui.views.View;
import com.ldtteam.multipiston.network.MultiPistonChangeMessage;
import com.ldtteam.multipiston.network.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import static com.ldtteam.multipiston.MultiPiston.MOD_ID;
import static com.ldtteam.multipiston.TileEntityMultiPiston.DEFAULT_RANGE;
import static com.ldtteam.multipiston.TileEntityMultiPiston.DEFAULT_SPEED;
import static net.minecraft.core.Direction.*;

/**
 * BuildTool window.
 */
public class WindowMultiPiston extends AbstractWindowSkeleton
{
    /**
     * Resource suffix of the multipiston GUI.
     */
    public static final String MULTI_BLOCK_RESOURCE_SUFFIX = ":gui/windowmultipiston.xml";

    /**
     * Name of the input range field.
     */
    public static final String INPUT_RANGE_NAME = "range";

    /**
     * Id of the speed input field.
     */
    public static final String INPUT_SPEED = "speed";

    /**
     * Pre resource string.
     */
    private static final String RES_STRING = "textures/gui/%s.png";

    /**
     * Green String for selected left click.
     */
    private static final String GREEN_POS = "_green";

    /**
     * Red String for selected right click.
     */
    private static final String RED_POS = "_red";

    /**
     * This button will send a packet to the server telling it to place this hut/decoration.
     */
    public static final String BUTTON_CONFIRM = "confirm";

    /**
     * This button will remove the currently rendered structure.
     */
    public static final String BUTTON_CANCEL = "cancel";

    /**
     * Id of the up button in the GUI.
     */
    public static final String BUTTON_UP = "plus";

    /**
     * Id of the up button in the GUI.
     */
    public static final String BUTTON_DOWN = "minus";

    /**
     * Move the structure preview left.
     */
    public static final String BUTTON_LEFT = "left";

    /**
     * Move the structure preview right.
     */
    public static final String BUTTON_RIGHT = "right";

    /**
     * Move the structure preview forward.
     */
    public static final String BUTTON_FORWARD = "up";

    /**
     * Move the structure preview back.
     */
    public static final String BUTTON_BACKWARD = "down";


    /**
     * Position of the multipiston.
     */
    private final BlockPos pos;

    /**
     * The direction it is facing.
     */
    private Direction facing = UP;

    /**
     * The output direction.
     */
    private Direction output = DOWN;

    /**
     * The input field with the range.
     */
    private final TextField inputRange;

    /**
     * The input field with the range.
     */
    private final TextField inputSpeed;


    /**
     * The constructor called before opening this window.
     *
     * @param pos the position of the TileEntity which this window belogs to.
     */
    public WindowMultiPiston(@Nullable final BlockPos pos)
    {
        super(MOD_ID + MULTI_BLOCK_RESOURCE_SUFFIX);
        this.pos = pos;
        inputRange = findPaneOfTypeByID(INPUT_RANGE_NAME, TextField.class);
        inputSpeed = findPaneOfTypeByID(INPUT_SPEED, TextField.class);
        this.init();
    }

    private void init()
    {
        // Register all necessary buttons with the window.
        registerButton(BUTTON_CONFIRM, this::confirmClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
        registerButton(BUTTON_LEFT, this::moveLeftClicked);
        registerButton(BUTTON_RIGHT, this::moveRightClicked);
        registerButton(BUTTON_UP, this::moveUpClicked);
        registerButton(BUTTON_DOWN, this::moveDownClicked);
        registerButton(BUTTON_BACKWARD, this::moveBackClicked);
        registerButton(BUTTON_FORWARD, this::moveForwardClicked);
    }

    /**
     * Called when the window is opened.
     * Sets up the buttons for either hut mode or decoration mode.
     */
    @Override
    public void onOpened()
    {
        final BlockEntity block = Minecraft.getInstance().level.getBlockEntity(pos);
        if (block instanceof TileEntityMultiPiston)
        {
            inputRange.setText(Integer.toString(((TileEntityMultiPiston) block).getRange()));
            inputSpeed.setText(Integer.toString(((TileEntityMultiPiston) block).getSpeed()));
            final Direction dir = ((TileEntityMultiPiston) block).getDirection();
            final Direction out = ((TileEntityMultiPiston) block).getOutput();
            enable(dir, dir, false);
            enable(out, out, true);
            return;
        }
        close();
    }

    private void enable(final Direction oldFacing, final Direction newFacing, final boolean rightClick)
    {
        switch (oldFacing)
        {
            case DOWN:
                findPaneOfTypeByID(BUTTON_DOWN, ButtonImage.class).setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, BUTTON_DOWN)), false);
                break;
            case NORTH:
                findPaneOfTypeByID(BUTTON_FORWARD, ButtonImage.class)
                    .setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, BUTTON_FORWARD)), false);
                break;
            case SOUTH:
                findPaneOfTypeByID(BUTTON_BACKWARD, ButtonImage.class)
                    .setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, BUTTON_BACKWARD)), false);
                break;
            case EAST:
                findPaneOfTypeByID(BUTTON_RIGHT, ButtonImage.class).setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, BUTTON_RIGHT)), false);
                break;
            case WEST:
                findPaneOfTypeByID(BUTTON_LEFT, ButtonImage.class).setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, BUTTON_LEFT)), false);
                break;
            default:
                findPaneOfTypeByID(BUTTON_UP, ButtonImage.class).setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, BUTTON_UP)), false);
                break;
        }

        final String color = rightClick ? RED_POS : GREEN_POS;
        switch (newFacing)
        {
            case DOWN:
                findPaneOfTypeByID(BUTTON_DOWN, ButtonImage.class)
                    .setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, BUTTON_DOWN + color)), false);
                break;
            case NORTH:
                findPaneOfTypeByID(BUTTON_FORWARD, ButtonImage.class)
                    .setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, BUTTON_FORWARD + color)), false);
                break;
            case SOUTH:
                findPaneOfTypeByID(BUTTON_BACKWARD, ButtonImage.class)
                    .setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, BUTTON_BACKWARD + color)), false);
                break;
            case EAST:
                findPaneOfTypeByID(BUTTON_RIGHT, ButtonImage.class)
                    .setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, BUTTON_RIGHT + color)), false);
                break;
            case WEST:
                findPaneOfTypeByID(BUTTON_LEFT, ButtonImage.class)
                    .setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, BUTTON_LEFT + color)), false);
                break;
            default:
                findPaneOfTypeByID(BUTTON_UP, ButtonImage.class).setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, BUTTON_UP + color)), false);
                break;
        }

        if (rightClick)
        {
            output = newFacing;
        }
        else
        {
            facing = newFacing;
        }
    }

    /*
     * ---------------- Button Handling -----------------
     */

    /**
     * Move the schematic up.
     */
    private void moveUpClicked()
    {
        enable(facing, UP, false);
    }

    /**
     * Move the structure down.
     */
    private void moveDownClicked()
    {
        enable(facing, DOWN, false);
    }

    /**
     * Move the structure left.
     */
    private void moveLeftClicked()
    {
        enable(facing, WEST, false);
    }

    /**
     * Move the structure forward.
     */
    private void moveForwardClicked()
    {
        enable(facing, NORTH, false);
    }

    /**
     * Move the structure back.
     */
    private void moveBackClicked()
    {
        enable(facing, SOUTH, false);
    }

    /**
     * Move the structure right.
     */
    private void moveRightClicked()
    {
        enable(facing, EAST, false);
    }

    /**
     * Send a packet telling the server to place the current structure.
     */
    private void confirmClicked()
    {
        int range = DEFAULT_RANGE;
        int speed = DEFAULT_SPEED;
        try
        {
            range = Integer.parseInt(inputRange.getText());
            speed = Integer.parseInt(inputSpeed.getText());
        }
        catch (final NumberFormatException e)
        {
            Log.getLogger().warn("Unable to parse number for multipiston range or speed, considering default range/speed!", e);
        }

        final BlockEntity block = Minecraft.getInstance().level.getBlockEntity(pos);
        if (block instanceof TileEntityMultiPiston)
        {
            ((TileEntityMultiPiston) block).setSpeed(speed);
            ((TileEntityMultiPiston) block).setRange(range);
            ((TileEntityMultiPiston) block).setOutput(output);
            ((TileEntityMultiPiston) block).setDirection(facing);
        }

        Network.getNetwork().sendToServer(new MultiPistonChangeMessage(pos, facing, output, range, speed));
        close();
    }

    @Override
    public boolean rightClick(final double mx, final double my)
    {
        Pane pane = this.findPaneForClick(mx, my);
        if (pane instanceof View)
        {
            pane = ((View) pane).findPaneForClick(mx, my);
        }
        if (pane instanceof Button && pane.isEnabled())
        {
            final Direction newFacing;
            switch (pane.getID())
            {
                case BUTTON_UP:
                    newFacing = UP;
                    break;
                case BUTTON_DOWN:
                    newFacing = DOWN;
                    break;
                case BUTTON_FORWARD:
                    newFacing = NORTH;
                    break;
                case BUTTON_BACKWARD:
                    newFacing = SOUTH;
                    break;
                case BUTTON_RIGHT:
                    newFacing = EAST;
                    break;
                case BUTTON_LEFT:
                    newFacing = WEST;
                    break;
                default:
                    newFacing = UP;
                    break;
            }
            enable(output, newFacing, true);
            return true;
        }
        return false;
    }

    /**
     * Cancel the current structure.
     */
    private void cancelClicked()
    {
        close();
    }
}
