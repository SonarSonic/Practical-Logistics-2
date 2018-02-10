package sonar.logistics.api;

import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.world.World;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.api.viewers.ILogicListenable;

public interface IInfoManager {

	public Map<Integer,ILogicListenable> getMonitors();

	public Map<InfoUUID, IInfo> getInfoList();	

	public IInfo getInfoFromUUID(InfoUUID uuid);
	
	public void setInfo(InfoUUID uuid, IInfo newInfo);

	public Map<Integer, ConnectedDisplay> getConnectedDisplays();

	@Nullable
	public <T extends IInfo> AbstractChangeableList getMonitoredList(InfoUUID uuid);

	public default InfoContainer getInfoContainer(int iden) {
		IDisplay display = getDisplay(iden);
		if (display == null) {
			display = getConnectedDisplay(iden);
		}
		return display == null ? null : display.container();
	}

	public ConnectedDisplay getConnectedDisplay(int iden);

	public ConnectedDisplay getOrCreateDisplayScreen(World world, ILargeDisplay display, int registryID);

	public void addIdentityTile(ILogicListenable infoProvider);

	public void removeIdentityTile(ILogicListenable monitor);

	public void removeAll();
	
	public IDisplay getDisplay(int iden);

	public void addDisplay(IDisplay display);

	public void removeDisplay(IDisplay display);
	
	
}
