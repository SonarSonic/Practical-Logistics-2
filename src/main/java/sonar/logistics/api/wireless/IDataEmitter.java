package sonar.logistics.api.wireless;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.UniversalChangeableList;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.info.types.MonitoredFluidStack;
import sonar.logistics.info.types.MonitoredItemStack;

/** implemented on the Data Emitter */
public interface IDataEmitter extends INetworkTile, IListReader<IInfo> {

	/** can the given player UUID connect to this IDataEmitter */
	public boolean canPlayerConnect(UUID uuid);

	/** the emitters name, as chosen by the user */
	public String getEmitterName();
	
	public DataEmitterSecurity getSecurity();

	public AbstractChangeableList<MonitoredItemStack> getServerItems();
	
	public AbstractChangeableList<MonitoredFluidStack> getServerFluids();

	public void sendRapidUpdate(EntityPlayer player);
}
