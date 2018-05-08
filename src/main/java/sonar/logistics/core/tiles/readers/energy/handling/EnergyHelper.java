package sonar.logistics.core.tiles.readers.energy.handling;

import sonar.core.SonarCore;
import sonar.core.api.energy.EnergyType;
import sonar.core.api.energy.ISonarEnergyHandler;

import java.util.ArrayList;
import java.util.List;

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
