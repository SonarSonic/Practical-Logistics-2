package sonar.logistics.connections.monitoring;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import sonar.core.SonarCore;
import sonar.core.api.energy.ISonarEnergyHandler;
import sonar.core.api.energy.StoredEnergyStack;
import sonar.logistics.PL2;
import sonar.logistics.api.asm.TileMonitorHandler;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.info.ITileMonitorHandler;
import sonar.logistics.api.nodes.BlockConnection;

@TileMonitorHandler(handlerID = EnergyMonitorHandler.id, modid = PL2.MODID)
public class EnergyMonitorHandler extends LogicMonitorHandler<MonitoredEnergyStack> implements ITileMonitorHandler<MonitoredEnergyStack> {

	public static final String id = "energy";

	@Override
	public String id() {
		return id;
	}

	@Override
	public MonitoredList<MonitoredEnergyStack> updateInfo(INetworkCache network, MonitoredList<MonitoredEnergyStack> previousList, BlockConnection connection) {
		MonitoredList<MonitoredEnergyStack> list = MonitoredList.<MonitoredEnergyStack>newMonitoredList(network.getNetworkID());
		List<ISonarEnergyHandler> providers = SonarCore.energyHandlers;
		StoredEnergyStack maxEnergy = null;
		for (ISonarEnergyHandler provider : providers) {
			TileEntity tile = connection.coords.getTileEntity();
			if (tile != null && provider.canProvideEnergy(tile, connection.face)) {
				StoredEnergyStack info = provider.getEnergy(new StoredEnergyStack(provider.getProvidedType()), tile, connection.face);
				if (info != null) {
					if (maxEnergy == null) {
						maxEnergy = info;
					} else {
						StoredEnergyStack converted = info.copy().convertEnergyType(maxEnergy.energyType);
						if (!maxEnergy.hasInput && converted.hasInput) {
							maxEnergy.hasInput = true;
							maxEnergy.input = converted.input;
						}
						if (!maxEnergy.hasOutput && converted.hasOutput) {
							maxEnergy.hasOutput = true;
							maxEnergy.output = converted.output;
						}
						if (!maxEnergy.hasUsage && converted.hasUsage) {
							maxEnergy.hasUsage = true;
							maxEnergy.usage = converted.usage;
						}
					}
					break;
				}
			}
		}
		if (maxEnergy != null){
			TileEntity tile = connection.coords.getTileEntity();
			MonitoredEnergyStack coords = new MonitoredEnergyStack(maxEnergy, new MonitoredBlockCoords(connection.coords, tile != null && tile.getDisplayName() != null ? tile.getDisplayName().getFormattedText() : connection.coords.getBlock().getLocalizedName()));
			list.addInfoToList(coords, previousList);
		}
		return list;
	}
}
