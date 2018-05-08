package sonar.logistics.api.core.tiles.wireless.emitters;

import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.readers.IListReader;
import sonar.logistics.core.tiles.displays.info.types.fluids.InfoNetworkFluid;
import sonar.logistics.core.tiles.displays.info.types.items.MonitoredItemStack;

/** implemented on the Data Emitter */
public interface IDataEmitter extends IWirelessEmitter, IListReader<IInfo> {

	AbstractChangeableList<MonitoredItemStack> getServerItems();
	
	AbstractChangeableList<InfoNetworkFluid> getServerFluids();

	void sendRapidUpdate(EntityPlayer player);
}
