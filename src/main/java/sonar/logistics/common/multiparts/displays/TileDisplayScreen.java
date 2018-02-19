package sonar.logistics.common.multiparts.displays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.tiles.displays.DisplayType;

public class TileDisplayScreen extends TileAbstractDisplay {

	private DisplayGSI container;

	//// IInfoDisplay \\\\

	@Override
	public DisplayGSI getGSI() {
		if (container == null) {
			container = new DisplayGSI(this, getInfoContainerID());
		}
		return container;
	}

	@Override
	public int getInfoContainerID() {
		return getIdentity();
	}

	@Override
	public DisplayType getDisplayType() {
		return DisplayType.SMALL;
	}

	//// SAVING \\\\

	@Override
	public NBTTagCompound writeData(NBTTagCompound tag, SyncType type) {
		super.writeData(tag, type);
		getGSI().writeData(tag, type.isType(SyncType.SPECIAL) ? SyncType.SAVE : type);
		return tag;
	}

	@Override
	public void readData(NBTTagCompound tag, SyncType type) {
		super.readData(tag, type);
		getGSI().readData(tag, type.isType(SyncType.SPECIAL) ? SyncType.SAVE : type);
	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.INTERNAL;
	}
}