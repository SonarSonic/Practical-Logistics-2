package sonar.logistics.connections.monitoring;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import sonar.core.SonarCore;
import sonar.core.api.energy.ISonarEnergyHandler;
import sonar.core.api.energy.StoredEnergyStack;
import sonar.logistics.Logistics;
import sonar.logistics.api.asm.TileMonitorHandler;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.info.ITileMonitorHandler;
import sonar.logistics.api.nodes.NodeConnection;

@TileMonitorHandler(handlerID = EnergyMonitorHandler.id, modid = Logistics.MODID)
public class EnergyMonitorHandler extends LogicMonitorHandler<MonitoredEnergyStack> implements ITileMonitorHandler<MonitoredEnergyStack> {

	public static final String id = "energy";

	@Override
	public String id() {
		return id;
	}

	@Override
	public MonitoredList<MonitoredEnergyStack> updateInfo(INetworkCache network, MonitoredList<MonitoredEnergyStack> previousList, NodeConnection connection) {
		MonitoredList<MonitoredEnergyStack> list = MonitoredList.<MonitoredEnergyStack>newMonitoredList(network.getNetworkID());
		List<ISonarEnergyHandler> providers = SonarCore.energyHandlers;
		for (ISonarEnergyHandler provider : providers) {
			TileEntity tile = connection.coords.getTileEntity();
			if (tile != null && provider.canProvideEnergy(tile, connection.face)) {
				StoredEnergyStack info = provider.getEnergy(new StoredEnergyStack(provider.getProvidedType()), tile, connection.face);
				if (info != null)
					list.addInfoToList(new MonitoredEnergyStack(info, new MonitoredBlockCoords(connection.coords, connection.coords.getBlock().getUnlocalizedName())), previousList);

			}
		}
		return null;
	}
}
