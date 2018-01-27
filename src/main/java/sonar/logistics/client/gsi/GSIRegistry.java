package sonar.logistics.client.gsi;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.render.DisplayInfo;
import sonar.logistics.info.types.InfoError;
import sonar.logistics.info.types.LogicInfo;

public class GSIRegistry implements IGSIRegistry {

	public Map<String, Class<? extends IGSI>> infoReferences = Maps.newHashMap();

	public void register(String infoID, Class<? extends IGSI> gsiClass) {
		infoReferences.put(infoID, gsiClass);
	}

	public Class<? extends IGSI> getGSIClass(String s) {
		return infoReferences.get(s);
	}

	@Nullable
	public IGSI getGSIInstance(String infoID, DisplayInfo renderInfo) {
		IGSI gsi = null;
		Class<? extends IGSI> clazz = infoReferences.get(infoID);
		if (clazz != null) {
			try {
				gsi = clazz.getConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}
		if(gsi==null){
			gsi = getGSIInstance(LogicInfo.id, renderInfo);//returns GSIBasicInfo
		}
		gsi.initGSI(renderInfo);
		return gsi;

	}
}
