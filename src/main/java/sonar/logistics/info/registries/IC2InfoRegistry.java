package sonar.logistics.info.registries;

import com.google.common.collect.Lists;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.ClientNameConstants;
import sonar.logistics.api.info.IInfoRegistry;
import sonar.logistics.api.info.ILogicInfoRegistry;
import sonar.logistics.api.register.RegistryType;

@InfoRegistry(modid = "IC2")
public class IC2InfoRegistry extends IInfoRegistry {

	public void registerBaseReturns(ILogicInfoRegistry registry) {
		registry.registerReturn(IReactor.class);
	}

	public void registerBaseMethods(ILogicInfoRegistry registry) {
		registry.registerMethods(IReactorChamber.class, RegistryType.TILE, Lists.newArrayList("getReactorInstance"));
		registry.registerMethods(IReactor.class, RegistryType.TILE, Lists.newArrayList("getHeat", "getMaxHeat", "getReactorEUEnergyOutput"));
		
	}

	public void registerAllFields(ILogicInfoRegistry registry) {	
		registry.registerFields(TileEntityNuclearReactorElectric.class, RegistryType.TILE, Lists.newArrayList("EmitHeat"));
	}

	public void registerAdjustments(ILogicInfoRegistry registry) {
		registry.registerInfoAdjustments(Lists.newArrayList("IReactor.getReactorEUEnergyOutput"), "", "EU/t");		
	}
}
