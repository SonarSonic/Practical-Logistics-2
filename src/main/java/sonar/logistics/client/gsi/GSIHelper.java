package sonar.logistics.client.gsi;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.SonarCore;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.FlexibleGuiHandler;
import sonar.logistics.PL2;
import sonar.logistics.api.displays.DisplayInfo;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.client.gui.GuiDisplayScreen.GuiState;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.InteractionHelper;
import sonar.logistics.info.types.InfoError;

public class GSIHelper {

	public static final String PACKET_ID = "PktID";
	public static IGSIPacketHandler handler = GSIHelper::runGSIPacket;

	public static IGSI getGSIForInfo(IInfo cachedInfo, DisplayInfo renderInfo) {
		IInfo info = cachedInfo == null ? InfoError.noData : cachedInfo;
		IGSI gsi = PL2.proxy.getGSIRegistry().getGSIInstance(info.getID(), renderInfo);
		return gsi;
	}

	public static IGSIPacketHandler getGSIHandler(IInfo info) {
		return info instanceof IGSIPacketHandler ? (IGSIPacketHandler) info : GSIHelper.handler;
	}

	public static void runGSIPacket(DisplayScreenClick click, DisplayInfo displayInfo, EntityPlayer player, NBTTagCompound clickTag) {
		readPacketID(clickTag).logic.doPacket(click, displayInfo, player, clickTag);
	}

	public static NBTTagCompound createBasicPacket(GSIPackets packet) {
		return writePacketID(new NBTTagCompound(), packet);
	}

	public static NBTTagCompound writePacketID(NBTTagCompound tag, GSIPackets packet) {
		tag.setInteger(PACKET_ID, packet.ordinal());
		return tag;
	}

	public static GSIPackets readPacketID(NBTTagCompound tag) {
		return GSIPackets.values()[tag.getInteger(PACKET_ID)];
	}

	//// ITEM PACKET \\\\

	public static NBTTagCompound createItemClickPacket(StoredItemStack stack, int networkID) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIPackets.ITEM_CLICK);
		tag.setInteger("networkID", networkID);
		if (stack != null) {
			stack.writeData(tag, SyncType.SAVE);
		}
		return tag;
	}

	public static void doItemPacket(DisplayScreenClick click, DisplayInfo displayInfo, EntityPlayer player, NBTTagCompound clickTag) {
		StoredItemStack clicked = NBTHelper.instanceNBTSyncable(StoredItemStack.class, clickTag);
		int networkID = clickTag.getInteger("networkID");
		InteractionHelper.screenItemStackClicked(networkID, clicked.item.isEmpty() ? null : clicked, click, displayInfo, player, clickTag);
		
	}

	//// FLUID PACKET \\\\

	public static NBTTagCompound createFluidClickPacket(StoredFluidStack stack, int networkID) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIPackets.FLUID_CLICK);
		tag.setInteger("networkID", networkID);
		if (stack != null) {
			stack.writeData(tag, SyncType.SAVE);
		}
		return tag;
	}

	public static void doFluidPacket(DisplayScreenClick click, DisplayInfo displayInfo, EntityPlayer player, NBTTagCompound clickTag) {
		StoredFluidStack clicked = NBTHelper.instanceNBTSyncable(StoredFluidStack.class, clickTag);
		InteractionHelper.onScreenFluidStackClicked(clickTag.getInteger("networkID"), clicked.fluid == null ? null : clicked, click, displayInfo, player, clickTag);
	}

	//// SOURCE BUTTON \\\\ - BASIC PACKET

	public static void doSourceButtonPacket(DisplayScreenClick click, DisplayInfo displayInfo, EntityPlayer player, NBTTagCompound clickTag) {
		IDisplay display = displayInfo.container.getDisplay();
		if (display instanceof ConnectedDisplay) {
			display = ((ConnectedDisplay) display).getTopLeftScreen();
		}
		if (display instanceof TileAbstractDisplay) {
			TileAbstractDisplay tile = (TileAbstractDisplay) display;
			int slotID = tile.getSlotID();
			NBTTagCompound tag = new NBTTagCompound();
			tag.setBoolean(slotID == -1 ? FlexibleGuiHandler.TILEENTITY : FlexibleGuiHandler.MULTIPART, true);
			tag.setInteger(FlexibleGuiHandler.SLOT_ID, slotID);
			tag.setInteger("infopos", displayInfo.id);
			SonarCore.instance.guiHandler.openGui(false, player, tile.getWorld(), tile.getPos(), GuiState.SOURCE.ordinal(), tag);
		}
	}

}
