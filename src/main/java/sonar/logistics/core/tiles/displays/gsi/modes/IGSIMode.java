package sonar.logistics.core.tiles.displays.gsi.modes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.api.utils.BlockInteractionType;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

public interface IGSIMode {

    /**click logic*/
    boolean onClicked(TileAbstractDisplay part, BlockPos pos,  DisplayScreenClick click, BlockInteractionType type, EntityPlayer player);

    /**for rendering the GSI Mode on the screen*/
    void renderMode();

    /**if default elements should still be renderer even when this mode is activated*/
    boolean renderElements();

    /**if this is true, before this mode is used, the GSI will check the EditContainer isn't being clicked*/
    boolean renderEditContainer();
}
