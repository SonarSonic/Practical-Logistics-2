package sonar.logistics.info.registries;

import com.google.common.collect.Lists;

import sonar.flux.api.AccessType;
import sonar.flux.api.network.EnergyStats;
import sonar.flux.api.network.IFluxCommon;
import sonar.flux.api.tiles.IFlux;
import sonar.flux.api.tiles.IFlux.ConnectionType;
import sonar.flux.connection.transfer.stats.NetworkStatistics;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.ClientNameConstants;
import sonar.logistics.api.info.register.IInfoRegistry;
import sonar.logistics.api.info.register.IMasterInfoRegistry;
import sonar.logistics.api.register.RegistryType;

@InfoRegistry(modid = "FluxNetworks")
public class FluxNetworksRegistry extends IInfoRegistry {

	public void registerBaseReturns(IMasterInfoRegistry registry) {
		registry.registerValidReturn(IFluxCommon.class);
		registry.registerValidReturn(ConnectionType.class);
		registry.registerValidReturn(AccessType.class);
		registry.registerValidReturn(NetworkStatistics.class);
		registry.registerValidReturn(EnergyStats.class);
	}

	public void registerBaseMethods(IMasterInfoRegistry registry) {
		registry.registerMethods(IFlux.class, RegistryType.TILE, Lists.newArrayList("getCoords", "getNetwork", "getConnectionType", "getTransferLimit", "getCurrentTransferLimit", "getCurrentPriority", "getCustomName"), false);
		registry.registerMethods(IFluxCommon.class, RegistryType.TILE, Lists.newArrayList("getAccessType", "getNetworkID", "getNetworkName", "getCachedPlayerName", "getEnergyAvailable", "getMaxEnergyStored", "getStatistics"), false);
		registry.registerMethods(AccessType.class, RegistryType.TILE, Lists.newArrayList("name"), false);
		registry.registerMethods(NetworkStatistics.class, RegistryType.TILE, Lists.newArrayList("getLatestStats"), false);
		registry.registerMethods(EnergyStats.class, RegistryType.TILE, Lists.newArrayList("getLatestStats"), false);

	}

	public void registerAllFields(IMasterInfoRegistry registry) {
		registry.registerFields(EnergyStats.class, RegistryType.TILE, Lists.newArrayList("transfer", "maxSent", "maxReceived"));
	}

	public void registerAdjustments(IMasterInfoRegistry registry) {
		registry.registerInfoAdjustments(Lists.newArrayList("EnergyStats.transfer", "EnergyStats.maxSent", "EnergyStats.maxReceived", "IFlux.getTransferLimit", "IFlux.getCurrentTransferLimit"), "", "RF/t");
		registry.registerInfoAdjustments(Lists.newArrayList("IFluxCommon.getEnergyAvailable", "IFluxCommon.getMaxEnergyStored"), "", "RF");
		registry.registerClientNames(ClientNameConstants.PRIORITY, Lists.newArrayList("IFlux.getCurrentPriority"));
	}
	
}
