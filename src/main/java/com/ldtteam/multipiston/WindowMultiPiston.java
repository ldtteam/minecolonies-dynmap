package com.ldtteam.multipiston;

import com.google.common.collect.*;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.mod.Log;
import com.ldtteam.blockui.views.DropDownList;
import com.ldtteam.multipiston.network.MultiPistonChangeMessage;
import com.ldtteam.multipiston.network.Network;
import com.ldtteam.structurize.api.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
     * Different colors for the input lists.
     */
    private static final List<String> COLORS = ImmutableList.of(
      "Yellow",
      "Orange",
      "Blue",
      "Green",
      "Red",
      "Purple");

    private static final BiMap<Direction, String> COLOR_MAP = HashBiMap.create(6);
    static
    {
        COLOR_MAP.put(UP, "Yellow");
        COLOR_MAP.put(DOWN, "Orange");
        COLOR_MAP.put(NORTH, "Blue");
        COLOR_MAP.put(EAST, "Green");
        COLOR_MAP.put(SOUTH, "Red");
        COLOR_MAP.put(WEST, "Purple");
    }

      /**
       * Resource suffix of the multipiston GUI.
       */
    public static final  String MULTI_BLOCK_RESOURCE_SUFFIX = ":gui/windowmultipiston.xml";

    /**
     * Name of the input range field.
     */
    public static final String INPUT_RANGE_NAME = "range";

    /**
     * Id of the speed input field.
     */
    public static final String INPUT_SPEED = "speed";

    /**
     * This button will send a packet to the server telling it to place this hut/decoration.
     */
    public static final String BUTTON_CONFIRM = "confirm";

    /**
     * Position of the multipiston.
     */
    private final BlockPos pos;

    /**
     * The direction it is facing.
     */
    private Direction input = UP;

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
     * Drop down list for style.
     */
    private DropDownList outputDropdown;

    /**
     * Drop down list for name style.
     */
    private DropDownList inputDropdown;

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
        registerButton(BUTTON_CONFIRM, this::confirmClicked);
        initDropDowns();
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
            initDropDowns();

            inputRange.setText(Integer.toString(((TileEntityMultiPiston) block).getRange()));
            inputSpeed.setText(Integer.toString(((TileEntityMultiPiston) block).getSpeed()));
            input = ((TileEntityMultiPiston) block).getInput();
            output = ((TileEntityMultiPiston) block).getOutput();

            this.inputDropdown.setSelectedIndex(COLORS.indexOf(COLOR_MAP.get(input)));
            this.outputDropdown.setSelectedIndex(COLORS.indexOf(COLOR_MAP.get(output)));


            return;
        }
        close();
    }


    /**
     * Initialise the previous/next and drop down list for style.
     */
    private void initDropDowns()
    {
        outputDropdown = findPaneOfTypeByID("output", DropDownList.class);
        outputDropdown.setHandler(this::toggleOutput);
        outputDropdown.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return COLORS.size();
            }

            @Override
            public String getLabel(final int index)
            {
                return COLORS.get(index);
            }
        });

        inputDropdown = findPaneOfTypeByID("input", DropDownList.class);
        inputDropdown.setHandler(this::toggleInput);
        inputDropdown.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return COLORS.size();
            }

            @Override
            public String getLabel(final int index)
            {
                return COLORS.get(index);
            }
        });
    }

    /**
     * Toggle the dropdownlist with the selected index to change the texture of the colonists.
     *
     * @param dropDownList the toggle dropdown list.
     */
    private void toggleInput(final DropDownList dropDownList)
    {
        Direction tempInput = COLOR_MAP.inverse().get(COLORS.get(dropDownList.getSelectedIndex()));
        if (tempInput.equals(output))
        {
            Utils.playErrorSound(Minecraft.getInstance().player);
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("com.ldtteam.multipiston.equalpos"), false);
            this.inputDropdown.setSelectedIndex(COLORS.indexOf(COLOR_MAP.get(input)));
        }
        else
        {
            input = tempInput;
        }
    }

    /**
     * Toggle the dropdownlist with the selected index to change the texture of the colonists.
     *
     * @param dropDownList the toggle dropdown list.
     */
    private void toggleOutput(final DropDownList dropDownList)
    {
        Direction tempOutput = COLOR_MAP.inverse().get(COLORS.get(dropDownList.getSelectedIndex()));
        if (tempOutput.equals(input))
        {
            Utils.playErrorSound(Minecraft.getInstance().player);
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("com.ldtteam.multipiston.equalpos"), false);
            this.outputDropdown.setSelectedIndex(COLORS.indexOf(COLOR_MAP.get(output)));
        }
        else
        {
            output = tempOutput;
        }
    }

    /*
     * ---------------- Button Handling -----------------
     */

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
            ((TileEntityMultiPiston) block).setInput(input);
        }

        Network.getNetwork().sendToServer(new MultiPistonChangeMessage(pos, input, output, range, speed));
        close();
    }
}
