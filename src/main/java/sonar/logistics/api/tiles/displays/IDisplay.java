package sonar.logistics.api.tiles.displays;

import net.minecraft.util.EnumFacing;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.ISyncableListener;
import sonar.logistics.api.info.render.IInfoContainer;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.viewers.ILogicListenable;

/** implemented by any Display Screen TileEntity */
public interface IDisplay extends INetworkTile, ILogicListenable<PlayerListener>, ISyncableListener {	
	
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
	
}
