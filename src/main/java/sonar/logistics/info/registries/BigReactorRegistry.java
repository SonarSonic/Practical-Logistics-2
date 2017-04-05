package sonar.logistics.info.registries;

import com.google.common.collect.Lists;

import appeng.tile.grindstone.TileGrinder;
import erogenousbeef.bigreactors.common.interfaces.IReactorFuelInfo;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPartBase;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.ClientNameConstants;
import sonar.logistics.api.info.IInfoRegistry;
import sonar.logistics.api.info.ILogicInfoRegistry;
import sonar.logistics.api.register.RegistryType;

@InfoRegistry(modid = "bigreactors")
public class BigReactorRegistry extends IInfoRegistry {

	public void registerBaseReturns(ILogicInfoRegistry registry) {
		registry.registerReturn(IReactorFuelInfo.class);
	}

	public void registerBaseMethods(ILogicInfoRegistry registry) {
		registry.registerMethods(TileEntityReactorPartBase.class, RegistryType.TILE, Lists.newArrayList("getReactorController"));
		registry.registerMethods(IReactorFuelInfo.class, RegistryType.TILE);
		registry.registerMethods(MultiblockReactor.class, RegistryType.TILE, Lists.newArrayList("getActive", "getEnergyGeneratedLastTick", "getFuelHeat"));
		
	}

	public void registerAllFields(ILogicInfoRegistry registry) {
		registry.registerFields(TileGrinder.class, RegistryType.TILE, Lists.newArrayList("points"));		
	}

	public void registerAdjustments(ILogicInfoRegistry registry) {
		registry.registerClientNames(ClientNameConstants.BASE_PROCESS_TIME, Lists.newArrayList("TileInscriber.getMaxProcessingTime"));
		registry.registerClientNames(ClientNameConstants.PROCESS_TIME, Lists.newArrayList("TileInscriber.getProcessingTime"));
		registry.registerClientNames(ClientNameConstants.PRIORITY, Lists.newArrayList("IPriorityHost.getPriority"));
		registry.registerInfoAdjustments(Lists.newArrayList("TileInscriber.getMaxProcessingTime", "TileInscriber.getProcessingTime"), "", ClientNameConstants.TICKS);
		registry.registerInfoAdjustments(Lists.newArrayList("TileGrinder.requiredTurns", "TileGrinder.points"), "", "turns");		
		
	}
}
