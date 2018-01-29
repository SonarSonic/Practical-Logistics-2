package sonar.logistics.api.wireless;

import java.util.UUID;

import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.api.viewers.ILogicListenable;

public interface IWirelessEmitter extends INetworkTile{

	/** can the given player UUID connect to this IDataEmitter */
	public EnumConnected canPlayerConnect(UUID uuid);

	/** the emitters name, as chosen by the user */
	public String getEmitterName();
	
	public WirelessSecurity getSecurity();
}
