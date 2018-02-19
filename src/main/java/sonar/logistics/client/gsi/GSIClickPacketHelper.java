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
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.DisplayInfo;
import sonar.logistics.api.displays.elements.DisplayElementContainer;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.client.gui.GuiDisplayScreen.GuiState;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.InteractionHelper;
import sonar.logistics.info.types.InfoError;
import sonar.logistics.packets.PacketGSIClick;

public class GSIClickPacketHelper {

	public static final String PACKET_ID = "PktID";
	public static IGSIClickPacketHandler handler = GSIClickPacketHelper::runGSIClickPacket;

	public static IGSI getGSIForInfo(IInfo cachedInfo, DisplayInfo renderInfo) {
		IInfo info = cachedInfo == null ? InfoError.noData : cachedInfo;
		IGSI gsi = PL2.proxy.getGSIRegistry().getGSIInstance(info.getID(), renderInfo);
		return gsi;
	}

	public static IGSIClickPacketHandler getGSIHandler(IInfo info) {
		return info instanceof IGSIClickPacketHandler ? (IGSIClickPacketHandler) info : GSIClickPacketHelper.handler;
	}

	public static void sendGSIClickPacket(NBTTagCompound tag, DisplayElementContainer container, DisplayScreenClick click) {
		if (!tag.hasNoTags()) {
			PL2.network.sendToServer(new PacketGSIClick(container.getContainerIdentity(), click, tag));
		}
	}

	public static void runGSIClickPacket(DisplayGSI gsi, DisplayScreenClick click, EntityPlayer player, NBTTagCompound clickTag) {
		readPacketID(clickTag).logic.runGSIClickPacket(gsi, click, player, clickTag);
	}

	public static NBTTagCompound createBasicPacket(GSIClickPackets packet) {
		return writePacketID(new NBTTagCompound(), packet);
	}

	public static NBTTagCompound writePacketID(NBTTagCompound tag, GSIClickPackets packet) {
		tag.setInteger(PACKET_ID, packet.ordinal());
		return tag;
	}

	public static GSIClickPackets readPacketID(NBTTagCompound tag) {
		return GSIClickPackets.values()[tag.getInteger(PACKET_ID)];
	}

	//// ITEM PACKET \\\\

	public static NBTTagCompound createItemClickPacket(StoredItemStack stack, int networkID) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIClickPackets.ITEM_CLICK);
		tag.setInteger("networkID", networkID);
		if (stack != null) {
			stack.writeData(tag, SyncType.SAVE);
		}
		return tag;
	}

	public static void doItemPacket(DisplayGSI gsi, DisplayScreenClick click, EntityPlayer player, NBTTagCompound clickTag) {
		StoredItemStack clicked = NBTHelper.instanceNBTSyncable(StoredItemStack.class, clickTag);
		int networkID = clickTag.getInteger("networkID");
		InteractionHelper.screenItemStackClicked(networkID, clicked.item.isEmpty() ? null : clicked, click, player, clickTag);
		
	}

	//// FLUID PACKET \\\\

	public static NBTTagCompound createFluidClickPacket(StoredFluidStack stack, int networkID) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIClickPackets.FLUID_CLICK);
		tag.setInteger("networkID", networkID);
		if (stack != null) {
			stack.writeData(tag, SyncType.SAVE);
		}
		return tag;
	}

	public static void doFluidPacket(DisplayGSI gsi, DisplayScreenClick click, EntityPlayer player, NBTTagCompound clickTag) {
		StoredFluidStack clicked = NBTHelper.instanceNBTSyncable(StoredFluidStack.class, clickTag);
		InteractionHelper.onScreenFluidStackClicked(clickTag.getInteger("networkID"), clicked.fluid == null ? null : clicked, click, player, clickTag);
	}

	//// SOURCE BUTTON \\\\ - BASIC PACKET

	public static void doSourceButtonPacket(DisplayGSI gsi, DisplayScreenClick click, EntityPlayer player, NBTTagCompound clickTag) {
		
		IDisplay display = gsi.getDisplay();
		if (display instanceof ConnectedDisplay) {
			display = ((ConnectedDisplay) display).getTopLeftScreen();
		}
		if (display instanceof TileAbstractDisplay) {
			TileAbstractDisplay tile = (TileAbstractDisplay) display;
			int slotID = tile.getSlotID();
			NBTTagCompound tag = new NBTTagCompound();
			tag.setBoolean(slotID == -1 ? FlexibleGuiHandler.TILEENTITY : FlexibleGuiHandler.MULTIPART, true);
			tag.setInteger(FlexibleGuiHandler.SLOT_ID, slotID);
			//tag.setInteger("infopos", displayInfo.id);
			SonarCore.instance.guiHandler.openGui(false, player, tile.getWorld(), tile.getPos(), GuiState.SOURCE.ordinal(), tag);
		}
		
	}

}
