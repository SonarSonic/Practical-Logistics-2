package sonar.logistics.api.wireless;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.info.types.MonitoredFluidStack;
import sonar.logistics.info.types.MonitoredItemStack;

public interface IWirelessEmitter extends INetworkTile, ILogicListenable{

	/** can the given player UUID connect to this IDataEmitter */
	public EnumConnected canPlayerConnect(UUID uuid);

	/** the emitters name, as chosen by the user */
	public String getEmitterName();
	
	public WirelessSecurity getSecurity();
}
