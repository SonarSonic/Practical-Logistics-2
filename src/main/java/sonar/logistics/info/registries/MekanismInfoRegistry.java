package sonar.logistics.info.registries;

import com.google.common.collect.Lists;

import mekanism.api.IEvaporationSolar;
import mekanism.api.IHeatTransfer;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.reactor.IFusionReactor;
import mekanism.api.reactor.IReactorBlock;
import mekanism.common.tile.TileEntityBasicMachine;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.ClientNameConstants;
import sonar.logistics.api.info.register.IInfoRegistry;
import sonar.logistics.api.info.register.IMasterInfoRegistry;
import sonar.logistics.api.register.RegistryType;

@InfoRegistry(modid = "Mekanism")
public class MekanismInfoRegistry extends IInfoRegistry {

	@Override
	public void registerBaseReturns(IMasterInfoRegistry registry) {
		registry.registerValidReturn(IFusionReactor.class);
	}	

	@Override
	public void registerBaseMethods(IMasterInfoRegistry registry) {
		registry.registerMethods(IHeatTransfer.class, RegistryType.TILE, Lists.newArrayList("getTemp", "getInverseConductionCoefficient", "getInsulationCoefficient", "canConnectHeat"));
		registry.registerMethods(IEvaporationSolar.class, RegistryType.TILE, Lists.newArrayList("seesSun"));
		registry.registerMethods(IEvaporationSolar.class, RegistryType.TILE, Lists.newArrayList("seesSun"));
		registry.registerMethods(ILaserReceptor.class, RegistryType.TILE, Lists.newArrayList("canLasersDig"));
		registry.registerMethods(IReactorBlock.class, RegistryType.TILE, Lists.newArrayList("getReactor"));
		registry.registerMethods(IFusionReactor.class, RegistryType.TILE, Lists.newArrayList("isBurning", "isFormed", "getCaseTemp", "getPlasmaTemp", "getInjectionRate"));
		
	}

	@Override
	public void registerAllFields(IMasterInfoRegistry registry){
		registry.registerFields(TileEntityBasicMachine.class, RegistryType.TILE, Lists.newArrayList("BASE_ENERGY_PER_TICK", "energyPerTick"));
		registry.registerFields(TileEntityBasicMachine.class, RegistryType.TILE, Lists.newArrayList("operatingTicks", "ticksRequired", "BASE_TICKS_REQUIRED"));
	}

	@Override
	public void registerAdjustments(IMasterInfoRegistry registry) {
		registry.registerClientNames(ClientNameConstants.BASE_PROCESS_TIME, Lists.newArrayList("TileEntityBasicMachine.BASE_TICKS_REQUIRED"));
		registry.registerClientNames(ClientNameConstants.PROCESS_TIME, Lists.newArrayList("TileEntityBasicMachine.ticksRequired"));
		registry.registerClientNames(ClientNameConstants.CURRENT_PROCESS_TIME, Lists.newArrayList("TileEntityBasicMachine.operatingTicks"));
		registry.registerClientNames(ClientNameConstants.ENERGY_USAGE, Lists.newArrayList("TileEntityBasicMachine.energyPerTick"));
		registry.registerClientNames(ClientNameConstants.BASE_ENERGY_USAGE, Lists.newArrayList("TileEntityBasicMachine.BASE_ENERGY_PER_TICK"));
		registry.registerInfoAdjustments(Lists.newArrayList("TileEntityBasicMachine.BASE_ENERGY_PER_TICK", "TileEntityBasicMachine.energyPerTick"), "", "RF/t");
		registry.registerInfoAdjustments(Lists.newArrayList("TileEntityBasicMachine.operatingTicks", "TileEntityBasicMachine.ticksRequired", "TileEntityBasicMachine.BASE_TICKS_REQUIRED"), "", ClientNameConstants.TICKS);
	}
}
