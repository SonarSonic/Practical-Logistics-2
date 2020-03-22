package sonar.logistics.core.tiles.displays.gsi.modes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import sonar.core.api.utils.BlockInteractionType;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.info.elements.buttons.ButtonEmptyInfo;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

public class GSIModeDefault implements IGSIMode {

    public final DisplayGSI gsi;

    public GSIModeDefault(DisplayGSI gsi){
       this.gsi = gsi;
    }

    @Override
    public boolean onClicked(TileAbstractDisplay part, BlockPos pos, DisplayScreenClick click, BlockInteractionType type, EntityPlayer player) {
        if (click.clickedElement != null) {
            //// NO-SHIFT: OPENS GUI EDIT SCREEN, SHIFT: STARTS RESIZE MODE FOR THE CLICKED ELEMENT \\\\
            if (!(click.clickedElement instanceof ButtonEmptyInfo)) {
                if (!player.isSneaking()) {
                    NBTTagCompound guiTag = new NBTTagCompound();
                    guiTag.setInteger("clicked", click.clickedElement.getElementIdentity());
                    gsi.requestGui(part, gsi.world, pos, player, -1, 1, guiTag);
                } else {
                    gsi.grid_mode.startResizeSelectionMode(click.clickedElement.getHolder().getContainer().getContainerIdentity());
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void renderMode() {}

    @Override
    public boolean renderElements() {
        return true;
    }
}
