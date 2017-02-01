package sonar.logistics.api.displays;

import net.minecraft.util.EnumFacing;
import sonar.core.network.sync.ISyncableListener;
import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.api.viewers.ILogicViewable;

/** implemented by any Display Screen TileEntity */
public interface IInfoDisplay extends ILogicTile, ILogicViewable, ISyncableListener {	
	
	/**the IInfoContainer holding all the current Display Info*/
	public IInfoContainer container();

	/**the current screen layout*/
	public ScreenLayout getLayout();
	
	/**the current Display Type*/
	public DisplayType getDisplayType();
	
	/**the maximum amount of info which can be displayed on the screen at a time*/
	public int maxInfo();
	
	/**which face the Display is facing, used for checking if LargeDisplayScreens can connect*/
	public EnumFacing getFace();
	
}
