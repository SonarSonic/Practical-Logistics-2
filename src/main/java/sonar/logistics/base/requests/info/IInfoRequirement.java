package sonar.logistics.base.requests.info;

import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;

import java.util.List;

public interface IInfoRequirement {
	
	int getRequired();	
	
	List<InfoUUID> getSelectedInfo();
	
	/**sends the selected info to the server*/
	void onGuiClosed(List<InfoUUID> selected);	
	
	void doInfoRequirementPacket(DisplayGSI gsi, EntityPlayer player, List<InfoUUID> require);
}
