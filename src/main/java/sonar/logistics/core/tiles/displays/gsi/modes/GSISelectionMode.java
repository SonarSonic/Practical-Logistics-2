package sonar.logistics.core.tiles.displays.gsi.modes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import sonar.core.api.utils.BlockInteractionType;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayElementContainer;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

import java.util.ArrayList;
import java.util.List;

public class GSISelectionMode implements IGSIMode {

	public DisplayGSI gsi;

	/// element selection mode
	public GSIElementSelection selectionType;
	public List<Integer> selected_identities = new ArrayList<>();
	
	public GSISelectionMode(DisplayGSI gsi) {
		this.gsi = gsi;
	}

	//// ELEMENT SELECTION MODE \\\\

	public void startElementSelectionMode(GSIElementSelection type) {
		selectionType = type;
		selected_identities = new ArrayList<>();
		gsi.mode = gsi.selection_mode;
	}

	public void onElementSelected(int containerID, BlockInteractionType type) {
		if (type == BlockInteractionType.RIGHT) {
			if (selected_identities.contains(containerID)) {
				selected_identities.remove(Integer.valueOf(containerID));
			} else {
				selected_identities.add(containerID);
			}
		}
		if (type == BlockInteractionType.SHIFT_LEFT) {
			finishElementSelectionMode(false);
		}

		if (type == BlockInteractionType.SHIFT_RIGHT) {
			if (!selected_identities.isEmpty()) {
				finishElementSelectionMode(true);
			}
		}
	}

	public void finishElementSelectionMode(boolean sendPacket) {
		if (sendPacket)
			selectionType.finishSelection(gsi, selected_identities);
		selectionType = null;
		selected_identities = new ArrayList<>();
		gsi.mode = gsi.default_mode;
	}

	@Override
	public boolean onClicked(TileAbstractDisplay part, BlockPos pos, DisplayScreenClick click, BlockInteractionType type, EntityPlayer player) {
		for (DisplayElementContainer container : gsi.containers.values()) {
			if (container.canRender() && container.canClickContainer(click.clickX, click.clickY)) {
				gsi.selection_mode.onElementSelected(container.getContainerIdentity(), type);
				break;
			}
		}
		return true;
	}

	@Override
	public void renderMode() {}

	@Override
	public boolean renderElements() {
		return true;
	}
}
