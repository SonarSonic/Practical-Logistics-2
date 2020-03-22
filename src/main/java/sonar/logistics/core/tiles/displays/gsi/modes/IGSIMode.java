package sonar.logistics.core.tiles.displays.gsi.modes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import sonar.core.api.utils.BlockInteractionType;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

@Deprecated
public interface IGSIMode {

    /**click logic*/
    boolean onClicked(TileAbstractDisplay part, BlockPos pos,  DisplayScreenClick click, BlockInteractionType type, EntityPlayer player);

    /**for rendering the GSI Mode on the screen*/
    void renderMode();

    /**if default elements should still be renderer even when this mode is activated*/
    boolean renderElements();
}
