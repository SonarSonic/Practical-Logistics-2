package sonar.logistics.info.registries;

import com.google.common.collect.Lists;

import sonar.logistics.Logistics;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.IInfoRegistry;
import sonar.logistics.api.info.ILogicInfoRegistry;
import sonar.logistics.api.register.RegistryType;
import sonar.logistics.common.tileentity.TileEntityHammer;

@InfoRegistry(modid = Logistics.MODID)
public class LogisticsInfoRegistry extends IInfoRegistry {

	@Override
	public void registerBaseMethods(ILogicInfoRegistry registry) {
		registry.registerMethods(TileEntityHammer.class, RegistryType.TILE, Lists.newArrayList("getSpeed", "getProgress", "getCoolDown", "getCoolDownSpeed"));
	}

	@Override
	public void registerAdjustments(ILogicInfoRegistry registry) {
		registry.registerInfoAdjustments(Lists.newArrayList("TileEntityHammer.getSpeed", "TileEntityHammer.getProgress", "TileEntityHammer.getCoolDown", "TileEntityHammer.getCoolDownSpeed"), "", "ticks");
		registry.registerInfoAdjustments("item.storage", "", "items");
		registry.registerInfoAdjustments("fluid.storage", "", "mb");
	}
}
