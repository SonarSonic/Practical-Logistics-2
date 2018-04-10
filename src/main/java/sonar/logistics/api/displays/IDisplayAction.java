package sonar.logistics.api.displays;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.api.nbt.INBTSyncable;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;

public interface IDisplayAction extends INBTSyncable {

	String getRegisteredName();
	
	int doAction(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY);
	
}
