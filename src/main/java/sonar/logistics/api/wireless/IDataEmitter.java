package sonar.logistics.api.wireless;

import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.info.types.MonitoredFluidStack;
import sonar.logistics.info.types.MonitoredItemStack;

/** implemented on the Data Emitter */
public interface IDataEmitter extends IWirelessEmitter, IListReader<IInfo> {

	public AbstractChangeableList<MonitoredItemStack> getServerItems();
	
	public AbstractChangeableList<MonitoredFluidStack> getServerFluids();

	public void sendRapidUpdate(EntityPlayer player);
}
