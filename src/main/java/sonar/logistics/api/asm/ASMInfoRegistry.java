package sonar.logistics.api.asm;

import sonar.logistics.api.core.tiles.displays.info.register.IInfoRegistry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**use this with {@link IInfoRegistry}*/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ASMInfoRegistry {

	/**specify the MODID required for the Info Registry to load, note if you want it to always load use the Practical Logistics MODID*/
	String modid();
}