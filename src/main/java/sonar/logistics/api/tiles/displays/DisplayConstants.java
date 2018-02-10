package sonar.logistics.api.tiles.displays;

import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.info.ISuffixable;

/**these constants are used when configuring data on Display Screens*/
public class DisplayConstants {

	public static final String DATA = "%DATA%";
	public static final String NAME = "%NAME%";
	public static final String PREFIX = "%PRE%";
	public static final String SUFFIX = "%SUF%";
	public static final String SPACE = "%S%";
	
	/**replaces the constants with the data from the provided {@link IDisplayInfo}*/
	public static String formatText(String string, IDisplayInfo info) {
		String formatted = string;
		if (info.getSidedCachedInfo(true) != null && info.getSidedCachedInfo(true) instanceof INameableInfo) {
			INameableInfo cachedInfo = (INameableInfo) info.getSidedCachedInfo(true);
			formatted = formatted.replaceAll(DisplayConstants.NAME, cachedInfo.getClientIdentifier());
			if (cachedInfo instanceof ISuffixable) {
				ISuffixable suffixable = (ISuffixable) cachedInfo;
				formatted = formatted.replaceAll(DisplayConstants.DATA, suffixable.getRawData());
				formatted = formatted.replaceAll(DisplayConstants.SUFFIX, suffixable.getSuffix());
				formatted = formatted.replaceAll(DisplayConstants.PREFIX, suffixable.getPrefix());				
			} else {
				formatted = formatted.replaceAll(DisplayConstants.DATA, cachedInfo.getClientObject());
			}
		}
		return formatted;
	}

}
