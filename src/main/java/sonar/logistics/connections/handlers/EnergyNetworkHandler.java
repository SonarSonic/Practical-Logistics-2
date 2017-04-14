package sonar.logistics.connections.handlers;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import sonar.core.SonarCore;
import sonar.core.api.energy.ISonarEnergyHandler;
import sonar.core.api.energy.StoredEnergyStack;
import sonar.logistics.PL2Config;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.NetworkHandler;
import sonar.logistics.api.asm.NetworkHandlerField;
import sonar.logistics.api.networks.INetworkChannels;
import sonar.logistics.api.networks.INetworkListChannels;
import sonar.logistics.api.networks.ITileMonitorHandler;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.info.types.MonitoredBlockCoords;
import sonar.logistics.info.types.MonitoredEnergyStack;

@NetworkHandler(handlerID = EnergyNetworkHandler.id, modid = PL2Constants.MODID)
public class EnergyNetworkHandler<C extends INetworkListChannels> extends ListNetworkHandler<MonitoredEnergyStack> implements ITileMonitorHandler<MonitoredEnergyStack,INetworkListChannels> {
	
	@NetworkHandlerField(handlerID = EnergyNetworkHandler.id)
	public static EnergyNetworkHandler INSTANCE;

	public static final String id = "energy";

	@Override
	public String id() {
		return id;
	}

	@Override
	public MonitoredList<MonitoredEnergyStack> updateInfo(INetworkListChannels channels, MonitoredList<MonitoredEnergyStack> newList, MonitoredList<MonitoredEnergyStack> previousList, BlockConnection connection) {
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
			newList.addInfoToList(coords, previousList);
		}
		return newList;
	}

	@Override
	public int updateRate() {
		return PL2Config.energyUpdate;
	}
}
