package sonar.logistics.client.gsi;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.elements.IDisplayElement;

/**can be implemented on info, if not will use a default handler (NOT MADE YET) FIXME*/
public interface IGSIElementPacketHandler {

	public void runGSIElementPacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound clickTag);
	
}
