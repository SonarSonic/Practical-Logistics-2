package sonar.logistics.api.displays.elements;

import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.info.InfoUUID;

import java.util.List;

public interface IInfoRequirement {
	
	int getRequired();	
	
	List<InfoUUID> getSelectedInfo();
	
	/**sends the selected info to the server*/
	void onGuiClosed(List<InfoUUID> selected);	
	
	void doInfoRequirementPacket(DisplayGSI gsi, EntityPlayer player, List<InfoUUID> require);
}
