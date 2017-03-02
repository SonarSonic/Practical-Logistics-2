package sonar.logistics.info.registries;

import com.google.common.collect.Lists;

import sonar.calculator.mod.api.machines.IFlawlessGreenhouse;
import sonar.calculator.mod.api.machines.IGreenhouse;
import sonar.calculator.mod.api.machines.ITeleport;
import sonar.calculator.mod.api.nutrition.IHealthProcessor;
import sonar.calculator.mod.api.nutrition.IHungerProcessor;
import sonar.core.api.machines.IProcessMachine;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.IInfoRegistry;
import static sonar.logistics.info.LogicInfoRegistry.*;
import static sonar.logistics.info.LogicInfoRegistry.RegistryType.*;

@InfoRegistry(modid = "calculator")
public class CalculatorInfoRegistry extends IInfoRegistry {

	@Override
	public void registerBaseMethods() {
		registerMethods(IHealthProcessor.class, TILE);
		registerMethods(IHungerProcessor.class, TILE);
		registerMethods(IProcessMachine.class, TILE);
		registerMethods(IGreenhouse.class, TILE, Lists.newArrayList("getState"), true);
		registerMethods(ITeleport.class, TILE, Lists.newArrayList("getCoords"), true);
		registerMethods(IFlawlessGreenhouse.class,TILE, Lists.newArrayList("getPlantsHarvested", "getPlantsGrown"));
		//LogicInfoRegistry.registerMethods(ICalculatorGenerator.class, RegistryType.TILE, Lists.newArrayList("getItemLevel", "getMaxItemLevel"));
	}

	@Override
	public void registerAdjustments() {
		registerInfoAdjustments(Lists.newArrayList("IHealthProcessor.getHealthPoints", "IHungerProcessor.getHungerPoints"), "", "points");
		registerInfoAdjustments(Lists.newArrayList("IProcessMachine.getCurrentProcessTime", "IProcessMachine.getProcessTime", "IProcessMachine.getBaseProcessTime"), "", "ticks");
	}

}
