package sonar.logistics.api.wireless;

import java.util.ArrayList;
import java.util.UUID;

import sonar.core.utils.IUUIDIdentity;
import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.readers.IListReader;
import sonar.logistics.connections.monitoring.MonitoredFluidStack;
import sonar.logistics.connections.monitoring.MonitoredItemStack;
import sonar.logistics.connections.monitoring.MonitoredList;

/** implemented on the Data Emitter */
public interface IDataEmitter extends ILogicTile, IListReader<IMonitorInfo> {

	/** can the given player UUID connect to this IDataEmitter */
	public boolean canPlayerConnect(UUID uuid);

	/** the emitters name, as chosen by the user */
	public String getEmitterName();

	/** called when this Emitter is connected to a DataReceiver */
	public void connect(IDataReceiver receiver);

	/** called when this Emitter is disconnected to a DataReceiver */
	public void disconnect(IDataReceiver receiver);

	/** a list of network IDs from all the connected networks. */
	public ArrayList<Integer> getWatchingNetworks();

	public DataEmitterSecurity getSecurity();
	
	public MonitoredList<MonitoredItemStack> getServerItems();
	
	public MonitoredList<MonitoredFluidStack> getServerFluids();

	public static void addConnectedNetworks(IDataEmitter emitter, int current, ArrayList<Integer> networks) {
		ArrayList<Integer> connected = emitter.getWatchingNetworks();
		connected.forEach(network -> {
			if (network != current && !networks.contains(network)) {
				networks.add(network);
			}
		});
	}
}
