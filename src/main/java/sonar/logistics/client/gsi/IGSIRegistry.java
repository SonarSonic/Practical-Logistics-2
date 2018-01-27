package sonar.logistics.client.gsi;

import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.render.DisplayInfo;

public interface IGSIRegistry {

	public void register(String infoID, Class<? extends IGSI> gsiClass);

	public Class<? extends IGSI> getGSIClass(String s);

	public IGSI getGSIInstance(String infoID, DisplayInfo renderInfo);
}
