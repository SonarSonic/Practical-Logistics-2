package sonar.logistics.info.registries;

import com.google.common.collect.Lists;

import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.register.IInfoRegistry;
import sonar.logistics.api.info.register.IMasterInfoRegistry;
import sonar.logistics.api.register.RegistryType;
import sonar.logistics.common.hammer.TileEntityHammer;

@InfoRegistry(modid = PL2Constants.MODID)
public class LogisticsInfoRegistry extends IInfoRegistry {

	@Override
	public void registerBaseMethods(IMasterInfoRegistry registry) {
		registry.registerMethods(TileEntityHammer.class, RegistryType.TILE, Lists.newArrayList("getSpeed", "getProgress", "getCoolDown", "getCoolDownSpeed"));
	}

	@Override
	public void registerAdjustments(IMasterInfoRegistry registry) {
		registry.registerInfoAdjustments(Lists.newArrayList("TileEntityHammer.getSpeed", "TileEntityHammer.getProgress", "TileEntityHammer.getCoolDown", "TileEntityHammer.getCoolDownSpeed"), "", "ticks");
		registry.registerInfoAdjustments("item.storage", "", "items");
		registry.registerInfoAdjustments("fluid.storage", "", "mb");
	}
}
