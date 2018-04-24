package sonar.logistics.info.registries;

import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.register.IInfoRegistry;
import sonar.logistics.api.info.register.IMasterInfoRegistry;

@InfoRegistry(modid = "endercore")
public class EnderCoreRegistry implements IInfoRegistry {

	public void registerBaseReturns(IMasterInfoRegistry registry) {}

	public void registerBaseMethods(IMasterInfoRegistry registry) {}

	public void registerAllFields(IMasterInfoRegistry registry) {}

	public void registerAdjustments(IMasterInfoRegistry registry) {}
}
