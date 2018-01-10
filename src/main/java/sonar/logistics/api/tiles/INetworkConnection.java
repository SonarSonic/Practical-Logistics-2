package sonar.logistics.api.tiles;

import net.minecraft.util.EnumFacing;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.cable.CableRenderType;
import sonar.logistics.api.tiles.cable.NetworkConnectionType;

public interface INetworkConnection {

	/** can the Tile connect to cables on the given direction */
	public NetworkConnectionType canConnect(int networkID, EnumFacing dir, boolean internal);

	/** for internal connections */
	public CableRenderType getCableRenderSize(EnumFacing dir);
	
	public ILogisticsNetwork getNetwork();
}
