package sonar.logistics.core.tiles.displays.gsi.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;

public enum GSIClickPacket {
	ITEM_CLICK(GSIClickPacketHelper::doItemPacket), //
	FLUID_CLICK(GSIClickPacketHelper::doFluidPacket), //
	SOURCE_BUTTON(GSIClickPacketHelper::doSourceButtonPacket); //

	IGSIClickPacketHandler logic;

	GSIClickPacket(IGSIClickPacketHandler logic) {
		this.logic = logic;
	}

	public interface IGSIClickPacketHandler {

		void runGSIClickPacket(DisplayGSI gsi, DisplayScreenClick click, EntityPlayer player, NBTTagCompound clickTag);

	}
}
