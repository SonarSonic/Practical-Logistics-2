package sonar.logistics.api.tiles.displays;

import net.minecraft.util.EnumFacing;
import sonar.core.listener.ISonarListener;
import sonar.core.network.sync.ISyncableListener;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.api.displays.DisplayGSI;

public interface IDisplay extends INetworkTile, ISyncableListener, ISonarListener {

	public int getInfoContainerID();
	
	public DisplayGSI getGSI();
	
	public EnumFacing getCableFace();
	
	public DisplayType getDisplayType();
	
	public void sendInfoContainerPacket();
	
}
