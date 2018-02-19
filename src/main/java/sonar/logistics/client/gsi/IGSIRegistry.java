package sonar.logistics.client.gsi;

import sonar.logistics.api.displays.DisplayInfo;

public interface IGSIRegistry {

	public void register(String infoID, Class<? extends IGSI> gsiClass);

	public Class<? extends IGSI> getGSIClass(String s);

	public IGSI getGSIInstance(String infoID, DisplayInfo renderInfo);
}
