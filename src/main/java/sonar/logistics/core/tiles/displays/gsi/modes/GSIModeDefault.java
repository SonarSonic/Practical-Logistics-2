package sonar.logistics.core.tiles.displays.gsi.modes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import sonar.core.api.utils.BlockInteractionType;
import sonar.logistics.base.guidance.errors.IInfoError;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayElementContainer;
import sonar.logistics.core.tiles.displays.info.elements.base.IClickableElement;
import sonar.logistics.core.tiles.displays.info.elements.base.IDisplayElement;
import sonar.logistics.core.tiles.displays.info.elements.buttons.ButtonEmptyInfo;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

import java.util.List;

public class GSIModeDefault implements IGSIMode {

    public final DisplayGSI gsi;

    public GSIModeDefault(DisplayGSI gsi){
       this.gsi = gsi;
    }

    @Override
    public boolean onClicked(TileAbstractDisplay part, BlockPos pos, DisplayScreenClick click, BlockInteractionType type, EntityPlayer player) {
        if (click.clickedElement != null && gsi.edit_mode.getObject()) {
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

    @Override
    public boolean renderEditContainer() {
        return true;
    }
}
