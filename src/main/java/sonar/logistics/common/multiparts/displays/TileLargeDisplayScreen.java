package sonar.logistics.common.multiparts.displays;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.BOOLEAN;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.PL2;
import sonar.logistics.api.cabling.CableConnectionType;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.networking.displays.ConnectedDisplayChange;
import sonar.logistics.networking.displays.DisplayHandler;

public class TileLargeDisplayScreen extends TileAbstractDisplay implements ILargeDisplay {

	// public int registryID = -1;
	public ConnectedDisplay overrideDisplay = null;
	public SyncTagType.BOOLEAN shouldRender = (BOOLEAN) new SyncTagType.BOOLEAN(3); // if this is the top left screen
	public SyncTagType.INT connected_display_ID = (INT) new SyncTagType.INT(4).setDefault(-1); // id of the connected display.
	public SyncTagType.BOOLEAN isLocked = (BOOLEAN) new SyncTagType.BOOLEAN(5); // if this screen was locked, must also be stored in screens as sometimes they connect before the connected display is loaded

	{
		syncList.addParts(shouldRender, connected_display_ID, isLocked);
	}

	//// IInfoDisplay \\\\

	@Override
	public int getInfoContainerID() {
		ConnectedDisplay display = getConnectedDisplay();
		return display == null ? -1 : display.getInfoContainerID();
	}

	@Override
	public DisplayGSI getGSI() {
		ConnectedDisplay display = getConnectedDisplay();
		return display == null ? null : getConnectedDisplay().getGSI();
	}

	@Override
	public DisplayType getDisplayType() {
		return DisplayType.LARGE;
	}

	@Override
	public ConnectableType getConnectableType() {
		return ConnectableType.SCREEN;
	}

	//// ILargeDisplay \\\\

	@Override
	public int getRegistryID() {
		return connected_display_ID.getObject();
	}

	@Override
	public void setRegistryID(int id) {
		if (connected_display_ID.getObject() != id) {
			connected_display_ID.setObject(id);
		}
	}

	@Override
	public ConnectedDisplay getConnectedDisplay() {
		if (isClient() && overrideDisplay != null) {
			return overrideDisplay;
		}
		if (this.getRegistryID() != -1) {
			return PL2.proxy.getInfoManager(isClient()).getConnectedDisplay(getRegistryID());
		}
		return null;
	}

	@Override
	public void setConnectedDisplay(ConnectedDisplay connectedDisplay) {
		overrideDisplay = connectedDisplay;
	}

	@Override
	public boolean shouldRender() {
		return shouldRender.getObject() && getRegistryID() != -1 && getConnectedDisplay() != null;
	}

	@Override
	public void setShouldRender(boolean shouldRender) {
		if (shouldRender != this.shouldRender.getObject()) {
			this.shouldRender.setObject(shouldRender);
		}
	}

	//// ILogicViewable \\\\

	@Override
	public CableConnectionType canConnect(int registryID, ConnectableType type, EnumFacing dir, boolean internal) {
		boolean cableFace = (dir == getCableFace() || dir == getCableFace().getOpposite());
		boolean cableConnection = (internal && cableFace && type == ConnectableType.CONNECTABLE);
		if (cableConnection || (!cableFace && type == this.getConnectableType() && (getRegistryID() == registryID || !(isLocked())))) {
			return CableConnectionType.NETWORK;
		}
		return CableConnectionType.NONE;
	}

	@Override
	public void addInfo(List<String> info) {
		super.addInfo(info);
		info.add("Large Display ID: " + connected_display_ID);
		info.add("Should Render " + this.shouldRender.getObject());
	}

	public boolean isLocked() {
		return this.isLocked.getObject() || getConnectedDisplay() != null && getConnectedDisplay().isLocked.getObject();
	}
	//// PACKETS \\\\

	@Override
	public NBTTagCompound writeData(NBTTagCompound tag, SyncType type) {
		super.writeData(tag, type);
		if (type.isType(SyncType.SPECIAL)) {
			getGSI().writeData(tag, SyncType.SAVE);
		}
		return tag;
	}

	@Override
	public void readData(NBTTagCompound tag, SyncType type) {
		super.readData(tag, type);
		if (type.isType(SyncType.SPECIAL)) {
			getGSI().readData(tag, SyncType.SAVE);
		}
	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.INTERNAL;
	}

	@Override
	public boolean isBlocked(EnumFacing dir) {
		return false;
	}

	@Override
	public void setLocked(boolean locked) {
		this.isLocked.setObject(locked);
	}
}
