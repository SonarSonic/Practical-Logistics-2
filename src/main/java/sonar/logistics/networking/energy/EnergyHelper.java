package sonar.logistics.networking.energy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import sonar.core.SonarCore;
import sonar.core.api.energy.EnergyType;
import sonar.core.api.energy.ISonarEnergyHandler;
import sonar.core.api.energy.StoredEnergyStack;
import sonar.core.helpers.SonarHelper;
import sonar.core.utils.SortingDirection;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.tiles.readers.EnergyReader.SortingType;
import sonar.logistics.info.types.MonitoredEnergyStack;

public class EnergyHelper {

	public List<ISonarEnergyHandler> getProviders(EnergyType type) {
		List<ISonarEnergyHandler> providers = new ArrayList<>();
		List<ISonarEnergyHandler> handlers = SonarCore.energyHandlers;
		for (ISonarEnergyHandler provider : handlers) {
			if (provider.getProvidedType().getName().equals(type.getName())) {
				providers.add(provider);
			}
		}
		return providers;
	}

}
