package sonar.logistics.common.multiparts.displays;

import net.minecraft.util.EnumFacing;
import sonar.core.api.utils.BlockCoords;
import sonar.core.network.sync.IDirtyPart;
import sonar.logistics.api.info.render.IInfoContainer;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.states.TileMessage;
import sonar.logistics.api.tiles.cable.CableRenderType;
import sonar.logistics.api.tiles.cable.ConnectableType;
import sonar.logistics.api.tiles.cable.NetworkConnectionType;
import sonar.logistics.api.tiles.displays.DisplayLayout;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.api.tiles.displays.IDisplay;

public class NEWConnectedDisplay implements IDisplay {

	@Override
	public int getNetworkID() {
		return 0;
	}

	@Override
	public ILogisticsNetwork getNetwork() {
		return null;
	}

	@Override
	public int getIdentity() {
		return 0;
	}

	@Override
	public TileMessage[] getValidMessages() {
		return null;
	}

	@Override
	public BlockCoords getCoords() {
		return null;
	}

	@Override
	public void onNetworkConnect(ILogisticsNetwork network) {		
	}

	@Override
	public void onNetworkDisconnect(ILogisticsNetwork network) {		
	}

	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public NetworkConnectionType canConnect(int registryID, ConnectableType type, EnumFacing dir, boolean internal) {
		return null;
	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return null;
	}

	@Override
	public void markChanged(IDirtyPart part) {		
	}

	@Override
	public IInfoContainer container() {
		return null;
	}

	@Override
	public DisplayLayout getLayout() {
		return null;
	}

	@Override
	public DisplayType getDisplayType() {
		return null;
	}

	@Override
	public int maxInfo() {
		return 0;
	}

	@Override
	public EnumFacing getCableFace() {
		return null;
	}

	@Override
	public EnumFacing getRotation() {
		return null;
	}

}
