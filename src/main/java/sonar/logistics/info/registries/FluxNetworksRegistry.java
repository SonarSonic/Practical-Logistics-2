package sonar.logistics.info.registries;

import com.google.common.collect.Lists;

import sonar.calculator.mod.api.machines.IGreenhouse;
import sonar.flux.api.EnergyStats;
import sonar.flux.api.IFlux;
import sonar.flux.api.IFlux.ConnectionType;
import sonar.flux.api.IFluxCommon;
import sonar.flux.api.IFluxCommon.AccessType;
import sonar.flux.api.IFluxNetwork;
import sonar.flux.api.INetworkStatistics;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.IInfoRegistry;
import sonar.logistics.info.LogicInfoRegistry;
import sonar.logistics.info.LogicInfoRegistry.RegistryType;

@InfoRegistry(modid = "FluxNetworks")
public class FluxNetworksRegistry extends IInfoRegistry {

	public void registerBaseReturns() {
		LogicInfoRegistry.registerReturn(IFluxCommon.class);
		LogicInfoRegistry.registerReturn(ConnectionType.class);
		LogicInfoRegistry.registerReturn(AccessType.class);
		LogicInfoRegistry.registerReturn(INetworkStatistics.class);
		LogicInfoRegistry.registerReturn(EnergyStats.class);
	}

	public void registerBaseMethods() {
		LogicInfoRegistry.registerMethods(IFlux.class, RegistryType.TILE, Lists.newArrayList("getCoords", "getNetwork", "getConnectionType", "getTransferLimit", "getCurrentTransferLimit", "getCurrentPriority", "getCustomName"), false);
		LogicInfoRegistry.registerMethods(IFluxCommon.class, RegistryType.TILE, Lists.newArrayList("getAccessType", "getNetworkID", "getNetworkName", "getCachedPlayerName", "getEnergyAvailable", "getMaxEnergyStored", "getStatistics"), false);
		LogicInfoRegistry.registerMethods(AccessType.class, RegistryType.TILE, Lists.newArrayList("name"), false);
		LogicInfoRegistry.registerMethods(INetworkStatistics.class, RegistryType.TILE, Lists.newArrayList("getLatestStats"), false);
		LogicInfoRegistry.registerMethods(EnergyStats.class, RegistryType.TILE, Lists.newArrayList("getLatestStats"), false);

	}

	public void registerAllFields() {
		LogicInfoRegistry.registerFields(EnergyStats.class, RegistryType.TILE, Lists.newArrayList("transfer", "maxSent", "maxReceived"));
	}

	public void registerAdjustments() {
		LogicInfoRegistry.registerInfoAdjustments(Lists.newArrayList("EnergyStats.transfer", "EnergyStats.maxSent", "EnergyStats.maxReceived", "IFlux.getTransferLimit", "IFlux.getCurrentTransferLimit"), "", "RF/t");
		LogicInfoRegistry.registerInfoAdjustments(Lists.newArrayList("IFluxCommon.getEnergyAvailable", "IFluxCommon.getMaxEnergyStored"), "", "RF");
	}
	
}
