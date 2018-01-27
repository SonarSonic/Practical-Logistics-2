package sonar.logistics.client.gsi;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import sonar.logistics.api.info.render.DisplayInfo;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;

public enum GSIPackets {
	ITEM_CLICK(GSIHelper::runItemPacket), FLUID_CLICK(GSIHelper::runFluidPacket);

	IPacketLogic logic;

	GSIPackets(IPacketLogic logic) {
		this.logic = logic;
	}

	public static interface IPacketLogic {
		public void runPacket(DisplayScreenClick click, DisplayInfo displayInfo, EntityPlayer player, NBTTagCompound clickTag);
	}
}
