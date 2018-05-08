package sonar.logistics.core.tiles.displays.gsi.interaction.actions;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.api.nbt.INBTSyncable;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;

public interface IDisplayAction extends INBTSyncable {

	String getRegisteredName();
	
	int doAction(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY);
	
}
