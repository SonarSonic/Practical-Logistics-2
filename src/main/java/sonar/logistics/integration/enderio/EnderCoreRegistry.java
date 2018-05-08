package sonar.logistics.integration.enderio;

import sonar.logistics.api.asm.ASMInfoRegistry;
import sonar.logistics.api.core.tiles.displays.info.register.IInfoRegistry;
import sonar.logistics.api.core.tiles.displays.info.register.IMasterInfoRegistry;

@ASMInfoRegistry(modid = "endercore")
public class EnderCoreRegistry implements IInfoRegistry {

	public void registerBaseReturns(IMasterInfoRegistry registry) {}

	public void registerBaseMethods(IMasterInfoRegistry registry) {}

	public void registerAllFields(IMasterInfoRegistry registry) {}

	public void registerAdjustments(IMasterInfoRegistry registry) {}
}
