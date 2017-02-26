package sonar.logistics.info.registries;

import com.google.common.collect.Lists;

import mekanism.api.IEvaporationSolar;
import mekanism.api.IHeatTransfer;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.reactor.IFusionReactor;
import mekanism.api.reactor.IReactorBlock;
import mekanism.common.tile.TileEntityBasicMachine;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.IInfoRegistry;
import sonar.logistics.info.LogicInfoRegistry;
import sonar.logistics.info.LogicInfoRegistry.RegistryType;

@InfoRegistry(modid = "Mekanism")
public class MekanismInfoRegistry extends IInfoRegistry {

	@Override
	public void registerBaseReturns() {
		LogicInfoRegistry.registerReturn(IFusionReactor.class);
	}	

	@Override
	public void registerBaseMethods() {
		LogicInfoRegistry.registerMethods(IHeatTransfer.class, RegistryType.TILE, Lists.newArrayList("getTemp", "getInverseConductionCoefficient", "getInsulationCoefficient", "canConnectHeat"));
		LogicInfoRegistry.registerMethods(IEvaporationSolar.class, RegistryType.TILE, Lists.newArrayList("seesSun"));
		LogicInfoRegistry.registerMethods(IEvaporationSolar.class, RegistryType.TILE, Lists.newArrayList("seesSun"));
		LogicInfoRegistry.registerMethods(ILaserReceptor.class, RegistryType.TILE, Lists.newArrayList("canLasersDig"));
		LogicInfoRegistry.registerMethods(IReactorBlock.class, RegistryType.TILE, Lists.newArrayList("getReactor"));
		LogicInfoRegistry.registerMethods(IFusionReactor.class, RegistryType.TILE, Lists.newArrayList("isBurning", "isFormed", "getCaseTemp", "getPlasmaTemp", "getInjectionRate"));
		
	}

	@Override
	public void registerAllFields(){
		LogicInfoRegistry.registerFields(TileEntityBasicMachine.class, RegistryType.TILE, Lists.newArrayList("BASE_ENERGY_PER_TICK", "energyPerTick"));
		LogicInfoRegistry.registerFields(TileEntityBasicMachine.class, RegistryType.TILE, Lists.newArrayList("operatingTicks", "ticksRequired", "BASE_TICKS_REQUIRED"));
		LogicInfoRegistry.registerClientNames("IProcessMachine.getBaseProcessTime", Lists.newArrayList("TileEntityBasicMachine.BASE_TICKS_REQUIRED"));
		LogicInfoRegistry.registerClientNames("IProcessMachine.getProcessTime", Lists.newArrayList("TileEntityBasicMachine.ticksRequired"));
		LogicInfoRegistry.registerClientNames("IProcessMachine.getCurrentProcessTime", Lists.newArrayList("TileEntityBasicMachine.operatingTicks"));
		LogicInfoRegistry.registerClientNames("IProcessMachine.getEnergyUsage", Lists.newArrayList("TileEntityBasicMachine.energyPerTick"));
		LogicInfoRegistry.registerClientNames("IProcessMachine.getBaseEnergyUsage", Lists.newArrayList("TileEntityBasicMachine.BASE_ENERGY_PER_TICK"));
	}

	@Override
	public void registerAdjustments() {
		LogicInfoRegistry.registerInfoAdjustments(Lists.newArrayList("TileEntityBasicMachine.BASE_ENERGY_PER_TICK", "TileEntityBasicMachine.energyPerTick"), "", "RF/t");
		LogicInfoRegistry.registerInfoAdjustments(Lists.newArrayList("TileEntityBasicMachine.operatingTicks", "TileEntityBasicMachine.ticksRequired", "TileEntityBasicMachine.BASE_TICKS_REQUIRED"), "", "ticks");
	}
}
