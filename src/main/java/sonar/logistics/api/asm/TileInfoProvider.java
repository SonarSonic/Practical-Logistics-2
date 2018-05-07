package sonar.logistics.api.asm;

import sonar.logistics.api.info.handlers.ITileInfoProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**use this with {@link ITileInfoProvider}*/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TileInfoProvider {

	/**specify the MODID required for the handler to load, note if you want it to always load use the Practical Logistics MODID*/
	String modid();

	/**the identification string of the TileHandler*/
	String handlerID();
}
