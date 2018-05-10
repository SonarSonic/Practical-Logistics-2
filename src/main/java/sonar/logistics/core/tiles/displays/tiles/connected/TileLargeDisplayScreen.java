package sonar.logistics.core.tiles.displays.tiles.connected;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.BOOLEAN;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.connections.EnumCableConnection;
import sonar.logistics.api.core.tiles.connections.EnumCableConnectionType;
import sonar.logistics.api.core.tiles.connections.EnumCableRenderSize;
import sonar.logistics.api.core.tiles.displays.tiles.IDisplay;
import sonar.logistics.api.core.tiles.displays.tiles.ILargeDisplay;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

import java.util.List;
import java.util.Optional;

public class TileLargeDisplayScreen extends TileAbstractDisplay implements ILargeDisplay {

	// public int registryID = -1;
	public ConnectedDisplay overrideDisplay = null;
	public SyncTagType.BOOLEAN shouldRender = (BOOLEAN) new SyncTagType.BOOLEAN(3); // if this is the top left screen
	public SyncTagType.INT connected_display_ID = (INT) new SyncTagType.INT(4).setDefault(-1); // id of the connected display.

	{
		syncList.addParts(shouldRender, connected_display_ID);
	}

	//// IInfoDisplay \\\\

	@Override
	public int getInfoContainerID() {
		Optional<ConnectedDisplay> display = getConnectedDisplay();
		return display.isPresent() ? display.get().getInfoContainerID() : -1;
	}

	@Override
	public DisplayGSI getGSI() {
		Optional<ConnectedDisplay> display = getConnectedDisplay();
		return display.isPresent() ? display.get().getGSI() : null;
	}

	@Override
	public void setGSI(DisplayGSI gsi){
		getConnectedDisplay().ifPresent(d -> d.setGSI(gsi));
	}

	@Override
	public EnumCableConnectionType getConnectableType() {
		return EnumCableConnectionType.SCREEN;
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
	public Optional<ConnectedDisplay> getConnectedDisplay(){
		if (isClient() && overrideDisplay != null) {
			return Optional.of(overrideDisplay);
		}
		if (this.getRegistryID() != -1) {
			return Optional.ofNullable(PL2.proxy.getInfoManager(isClient()).getConnectedDisplay(getRegistryID()));
		}
		return Optional.empty();
	}

	@Override
	public void setConnectedDisplay(ConnectedDisplay connectedDisplay) {
		overrideDisplay = connectedDisplay;
	}

	@Override
	public boolean shouldRender() {
		return shouldRender.getObject() && getRegistryID() != -1 && getConnectedDisplay().isPresent();
	}

	@Override
	public void setShouldRender(boolean shouldRender) {
		if (shouldRender != this.shouldRender.getObject()) {
			this.shouldRender.setObject(shouldRender);
		}
	}

	//// ILogicViewable \\\\

	@Override
	public EnumCableConnection canConnect(int registryID, EnumCableConnectionType type, EnumFacing dir, boolean internal) {
		boolean cableFace = (dir == getCableFace() || dir == getCableFace().getOpposite());
		boolean cableConnection = (internal && cableFace && type == EnumCableConnectionType.CONNECTABLE);
		if (cableConnection || (!cableFace && type == this.getConnectableType())) {
			return EnumCableConnection.NETWORK;
		}
		return EnumCableConnection.NONE;
	}

	@Override
	public void addInfo(List<String> info) {
		super.addInfo(info);
		info.add("Large Display ID: " + connected_display_ID);
		info.add("Should Render " + this.shouldRender.getObject());
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
	public boolean isBlocked(EnumFacing dir) {
		return false;
	}
}
