package sonar.logistics.api.viewers;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.utils.IUUIDIdentity;
import sonar.logistics.api.cabling.ILogicTile;

public interface ILogicViewable extends ILogicTile, IUUIDIdentity {

	public IViewersList getViewersList();
	
	public void onViewerAdded(EntityPlayer player, List<ViewerTally> arrayList);
	
	public void onViewerRemoved(EntityPlayer player, List<ViewerTally> arrayList);
				
}
