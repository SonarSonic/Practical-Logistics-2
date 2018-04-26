package sonar.logistics.common.multiparts.displays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.tiles.displays.DisplayType;

public class TileDisplayScreen extends TileAbstractDisplay {

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