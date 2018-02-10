package sonar.logistics.api.displays;

import java.util.function.Consumer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.ISyncableListener;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.tiles.displays.DisplayLayout;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;

/** used for storing display info to be used on Screens */
public interface IInfoContainer extends INBTSyncable, IDirtyPart, ISyncableListener {

	/**the current screen layout*/
	public DisplayLayout getLayout();
	
	public int getContainerIdentity();
	
	/** if this Display requires this InfoUUID to be synced */
	public boolean isDisplayingUUID(InfoUUID id);

	/** get the current info UUID of the Monitor Info at the given position */
	public InfoUUID getInfoUUID(int pos);
	
	public EnumFacing getFacing();
	
	public EnumFacing getRotation();
	
	public double[] getDisplayScaling();

	/** set the current info UUID at the given position */
	public void setUUID(InfoUUID id, int pos);

	/** renders the container, you should never need to call this yourself */
	public void renderContainer();

	/** the maximum amount of info to be displayed at a given time */
	public int getMaxCapacity();

	/** called when a display associated with this Container is clicked */
	public boolean onClicked(TileAbstractDisplay part, BlockInteractionType type, World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ);

	/** gets the display this InfoContainer is connected to */
	public IDisplay getDisplay();

	public DisplayInfo getDisplayInfo(int pos);
	
	public void forEachValidUUID(Consumer<InfoUUID> action);
	
	public void onInfoChanged(InfoUUID uuid, IInfo list);
	
	public void onMonitoredListChanged(InfoUUID uuid, AbstractChangeableList list);
}
