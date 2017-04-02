package sonar.logistics.info.registries;

import com.google.common.collect.Lists;

import sonar.flux.api.EnergyStats;
import sonar.flux.api.IFlux;
import sonar.flux.api.IFlux.ConnectionType;
import sonar.flux.api.IFluxCommon;
import sonar.flux.api.IFluxCommon.AccessType;
import sonar.flux.api.INetworkStatistics;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.ClientNameConstants;
import sonar.logistics.api.info.IInfoRegistry;
import sonar.logistics.api.info.ILogicInfoRegistry;
import sonar.logistics.api.register.RegistryType;
import sonar.logistics.info.LogicInfoRegistry;

@InfoRegistry(modid = "FluxNetworks")
public class FluxNetworksRegistry extends IInfoRegistry {

	public void registerBaseReturns(ILogicInfoRegistry registry) {
		registry.registerReturn(IFluxCommon.class);
		registry.registerReturn(ConnectionType.class);
		registry.registerReturn(AccessType.class);
		registry.registerReturn(INetworkStatistics.class);
		registry.registerReturn(EnergyStats.class);
	}

	public void registerBaseMethods(ILogicInfoRegistry registry) {
		registry.registerMethods(IFlux.class, RegistryType.TILE, Lists.newArrayList("getCoords", "getNetwork", "getConnectionType", "getTransferLimit", "getCurrentTransferLimit", "getCurrentPriority", "getCustomName"), false);
		registry.registerMethods(IFluxCommon.class, RegistryType.TILE, Lists.newArrayList("getAccessType", "getNetworkID", "getNetworkName", "getCachedPlayerName", "getEnergyAvailable", "getMaxEnergyStored", "getStatistics"), false);
		registry.registerMethods(AccessType.class, RegistryType.TILE, Lists.newArrayList("name"), false);
		registry.registerMethods(INetworkStatistics.class, RegistryType.TILE, Lists.newArrayList("getLatestStats"), false);
		registry.registerMethods(EnergyStats.class, RegistryType.TILE, Lists.newArrayList("getLatestStats"), false);

	}

	public void registerAllFields(ILogicInfoRegistry registry) {
		registry.registerFields(EnergyStats.class, RegistryType.TILE, Lists.newArrayList("transfer", "maxSent", "maxReceived"));
	}

	public void registerAdjustments(ILogicInfoRegistry registry) {
		registry.registerInfoAdjustments(Lists.newArrayList("EnergyStats.transfer", "EnergyStats.maxSent", "EnergyStats.maxReceived", "IFlux.getTransferLimit", "IFlux.getCurrentTransferLimit"), "", "RF/t");
		registry.registerInfoAdjustments(Lists.newArrayList("IFluxCommon.getEnergyAvailable", "IFluxCommon.getMaxEnergyStored"), "", "RF");
		registry.registerClientNames(ClientNameConstants.PRIORITY, Lists.newArrayList("IFlux.getCurrentPriority"));
	}
	
}
