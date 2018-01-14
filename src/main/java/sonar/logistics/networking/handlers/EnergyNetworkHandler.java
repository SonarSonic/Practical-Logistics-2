package sonar.logistics.networking.handlers;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import sonar.core.SonarCore;
import sonar.core.api.energy.ISonarEnergyHandler;
import sonar.core.api.energy.StoredEnergyStack;
import sonar.logistics.PL2Config;
import sonar.logistics.api.lists.types.InfoChangeableList;
import sonar.logistics.api.lists.types.UniversalChangeableList;
import sonar.logistics.api.networks.INetworkListChannels;
import sonar.logistics.api.networks.ITileMonitorHandler;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.info.types.MonitoredBlockCoords;
import sonar.logistics.info.types.MonitoredEnergyStack;
import sonar.logistics.networking.channels.EnergyNetworkChannels;

public class EnergyNetworkHandler<C extends INetworkListChannels> extends ListNetworkHandler<MonitoredEnergyStack, InfoChangeableList> implements ITileMonitorHandler<MonitoredEnergyStack, InfoChangeableList, INetworkListChannels> {

	public static EnergyNetworkHandler INSTANCE = new EnergyNetworkHandler();

	@Override
	public Class<? extends INetworkListChannels> getChannelsType() {
		return EnergyNetworkChannels.class;
	}

	@Override
	public InfoChangeableList updateInfo(INetworkListChannels channels, InfoChangeableList list, BlockConnection connection) {
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
		if (maxEnergy != null) {
			TileEntity tile = connection.coords.getTileEntity();
			MonitoredEnergyStack coords = new MonitoredEnergyStack(maxEnergy, new MonitoredBlockCoords(connection.coords, tile != null && tile.getDisplayName() != null ? tile.getDisplayName().getFormattedText() : connection.coords.getBlock().getLocalizedName()));
			list.add(coords);
		}
		return list;
	}

	@Override
	public int updateRate() {
		return PL2Config.energyUpdate;
	}

	@Override
	public InfoChangeableList newChangeableList() {
		return new InfoChangeableList();
	}
}
