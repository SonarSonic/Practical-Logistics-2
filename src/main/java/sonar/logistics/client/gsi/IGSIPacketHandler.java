package sonar.logistics.client.gsi;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import sonar.logistics.api.displays.DisplayInfo;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;

/**can be implemented on info, if not will use a default handler (NOT MADE YET) FIXME*/
public interface IGSIPacketHandler {

	public void runGSIPacket(DisplayScreenClick click, DisplayInfo displayInfo, EntityPlayer player, NBTTagCompound clickTag);
	
}
