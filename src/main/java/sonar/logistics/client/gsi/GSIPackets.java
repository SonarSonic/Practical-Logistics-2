package sonar.logistics.client.gsi;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;

public enum GSIPackets {
	ITEM_CLICK(GSIHelper::doItemPacket), FLUID_CLICK(GSIHelper::doFluidPacket), SOURCE_BUTTON(GSIHelper::doSourceButtonPacket);

	IPacketLogic logic;

	GSIPackets(IPacketLogic logic) {
		this.logic = logic;
	}

	public static interface IPacketLogic {
		public void doPacket(DisplayGSI gsi, DisplayScreenClick click, EntityPlayer player, NBTTagCompound clickTag);
	}
}
