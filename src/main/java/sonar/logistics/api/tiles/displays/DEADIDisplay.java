package sonar.logistics.api.tiles.displays;

import net.minecraft.util.EnumFacing;
import sonar.core.listener.ISonarListener;
import sonar.core.network.sync.ISyncableListener;
import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.api.displays.IInfoContainer;

/** implemented by any Display Screen TileEntity */
public interface DEADIDisplay extends INetworkTile, /* ILogicListenable<PlayerListener>, */ ISyncableListener, ISonarListener {	
	
	/**the IInfoContainer holding all the current Display Info*/
	public IInfoContainer container();

	/**the current screen layout*/
	public DisplayLayout getLayout();
	
	/**the current Display Type*/
	public DisplayType getDisplayType();
	
	/**the maximum amount of info which can be displayed on the screen at a time*/
	public int maxInfo();
	
	/**which face the Display is facing, used for checking if LargeDisplayScreens can connect*/
	public EnumFacing getCableFace();
	
	/**which face the Display is rotated*/
	public EnumFacing getRotation();
	
	/**gets the identity of the displays container, this is normally shared with the display, but in the case of large displays will be different*/
	public default int getContainerIdentity(){
		return getIdentity();
	}
	
}
