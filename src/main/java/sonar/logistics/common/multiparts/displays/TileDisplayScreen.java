package sonar.logistics.common.multiparts.displays;

import net.minecraft.util.EnumFacing;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.tiles.DisplayType;
import sonar.logistics.api.displays.tiles.ISmallDisplay;

public class TileDisplayScreen extends TileAbstractDisplay implements ISmallDisplay {

	public DisplayGSI container;

	//// IInfoDisplay \\\\

	@Override
	public DisplayGSI getGSI() {
		/*
		if (container == null) {
			container = new DisplayGSI(this, getWorld(), getInfoContainerID());
		}
		*/
		return container;
	}

	@Override
	public void setGSI(DisplayGSI gsi) {
		this.container = gsi;
	}

	@Override
	public int getInfoContainerID() {
		return getIdentity();
	}

	@Override
	public DisplayType getDisplayType() {
		return DisplayType.SMALL;
	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.INTERNAL;
	}
}