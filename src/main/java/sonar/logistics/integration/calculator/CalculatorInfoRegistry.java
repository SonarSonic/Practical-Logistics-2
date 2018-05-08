package sonar.logistics.integration.calculator;

import com.google.common.collect.Lists;
import sonar.calculator.mod.api.machines.IFlawlessGreenhouse;
import sonar.calculator.mod.api.machines.IGreenhouse;
import sonar.calculator.mod.api.machines.ITeleport;
import sonar.calculator.mod.api.nutrition.IHealthProcessor;
import sonar.calculator.mod.api.nutrition.IHungerProcessor;
import sonar.core.api.machines.IProcessMachine;
import sonar.logistics.api.asm.ASMInfoRegistry;
import sonar.logistics.api.core.tiles.displays.info.ClientNameConstants;
import sonar.logistics.api.core.tiles.displays.info.register.IInfoRegistry;
import sonar.logistics.api.core.tiles.displays.info.register.IMasterInfoRegistry;
import sonar.logistics.api.core.tiles.displays.info.register.RegistryType;

@ASMInfoRegistry(modid = "calculator")
public class CalculatorInfoRegistry implements IInfoRegistry {

	@Override
	public void registerBaseMethods(IMasterInfoRegistry registry) {
		registry.registerMethods(IHealthProcessor.class, RegistryType.TILE);
		registry.registerMethods(IHungerProcessor.class, RegistryType.TILE);
		registry.registerMethods(IProcessMachine.class, RegistryType.TILE);
		registry.registerMethods(IGreenhouse.class, RegistryType.TILE, Lists.newArrayList("getState"), true);
		registry.registerMethods(ITeleport.class, RegistryType.TILE, Lists.newArrayList("getCoords"), true);
		registry.registerMethods(IFlawlessGreenhouse.class, RegistryType.TILE, Lists.newArrayList("getPlantsHarvested", "getPlantsGrown"));
		//MasterInfoRegistry.registerMethods(ICalculatorGenerator.class, RegistryType.TILE, Lists.newArrayList("getItemLevel", "getMaxItemLevel"));
	}

	@Override
	public void registerAdjustments(IMasterInfoRegistry registry) {
		registry.registerInfoAdjustments(Lists.newArrayList("IHealthProcessor.getHealthPoints", "IHungerProcessor.getHungerPoints"), "", "points");
		registry.registerInfoAdjustments(Lists.newArrayList("IProcessMachine.getCurrentProcessTime", "IProcessMachine.getProcessTime", "IProcessMachine.getBaseProcessTime"), "", ClientNameConstants.TICKS);
		registry.registerClientNames(ClientNameConstants.PROCESS_TIME, Lists.newArrayList("IProcessMachine.getProcessTime"));
		registry.registerClientNames(ClientNameConstants.CURRENT_PROCESS_TIME, Lists.newArrayList("IProcessMachine.getCurrentProcessTime"));
		registry.registerClientNames(ClientNameConstants.BASE_PROCESS_TIME, Lists.newArrayList("IProcessMachine.getBaseProcessTime"));
		registry.registerClientNames(ClientNameConstants.ENERGY_USAGE, Lists.newArrayList("IProcessMachine.getEnergyUsage"));
	}

}
