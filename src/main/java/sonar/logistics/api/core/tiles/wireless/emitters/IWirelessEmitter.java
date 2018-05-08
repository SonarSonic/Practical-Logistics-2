package sonar.logistics.api.core.tiles.wireless.emitters;

import sonar.logistics.api.core.tiles.wireless.EnumWirelessConnectionState;
import sonar.logistics.api.core.tiles.wireless.EnumWirelessSecurity;
import sonar.logistics.base.tiles.INetworkTile;

import java.util.UUID;

public interface IWirelessEmitter extends INetworkTile{

	/** can the given player UUID connect to this IDataEmitter */
    EnumWirelessConnectionState canPlayerConnect(UUID uuid);

	/** the emitters name, as chosen by the user */
    String getEmitterName();
	
	EnumWirelessSecurity getSecurity();
}
