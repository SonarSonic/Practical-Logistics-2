package sonar.logistics.api.tiles;

import net.minecraft.util.EnumFacing;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.cable.CableRenderType;
import sonar.logistics.api.tiles.cable.ConnectableType;
import sonar.logistics.api.tiles.cable.NetworkConnectionType;

public interface INetworkConnection {

	/** can the Tile connect to cables on the given direction 
	 * @param type TODO*/
	public NetworkConnectionType canConnect(int registryID, ConnectableType type, EnumFacing dir, boolean internal);

	/** for internal connections */
	public CableRenderType getCableRenderSize(EnumFacing dir);
	
	public ILogisticsNetwork getNetwork();
}
