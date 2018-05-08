package sonar.logistics.core.tiles.displays.gsi.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.SonarCore;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.FlexibleGuiHandler;
import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.tiles.IDisplay;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.gsi.interaction.GSIInteractionHelper;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayElementContainer;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;
import sonar.logistics.core.tiles.displays.tiles.connected.ConnectedDisplay;
import sonar.logistics.network.packets.gsi.PacketGSIClick;

public class GSIClickPacketHelper {

	public static final String PACKET_ID = "PktID";
	public static GSIClickPacket.IGSIClickPacketHandler handler = GSIClickPacketHelper::runGSIClickPacket;
	/*
	public static IGSI getGSIForInfo(IInfo cachedInfo, DisplayInfo renderInfo) {
		IInfo info = cachedInfo == null ? InfoError.noData : cachedInfo;
		IGSI gsi = PL2.proxy.getGSIRegistry().getGSIInstance(info.getID(), renderInfo);
		return gsi;
	}
	*/
	public static GSIClickPacket.IGSIClickPacketHandler getGSIHandler(IInfo info) {
		return info instanceof GSIClickPacket.IGSIClickPacketHandler ? (GSIClickPacket.IGSIClickPacketHandler) info : GSIClickPacketHelper.handler;
	}

	public static void sendGSIClickPacket(NBTTagCompound tag, DisplayElementContainer container, DisplayScreenClick click) {
		if (!tag.hasNoTags()) {
			PL2.network.sendToServer(new PacketGSIClick(container.getContainerIdentity(), click, tag));
		}
	}

	public static void runGSIClickPacket(DisplayGSI gsi, DisplayScreenClick click, EntityPlayer player, NBTTagCompound clickTag) {
		readPacketID(clickTag).logic.runGSIClickPacket(gsi, click, player, clickTag);
	}

	public static NBTTagCompound createBasicPacket(GSIClickPacket packet) {
		return writePacketID(new NBTTagCompound(), packet);
	}

	public static NBTTagCompound writePacketID(NBTTagCompound tag, GSIClickPacket packet) {
		tag.setInteger(PACKET_ID, packet.ordinal());
		return tag;
	}

	public static GSIClickPacket readPacketID(NBTTagCompound tag) {
		return GSIClickPacket.values()[tag.getInteger(PACKET_ID)];
	}

	//// ITEM PACKET \\\\

	public static NBTTagCompound createItemClickPacket(StoredItemStack stack, int networkID) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIClickPacket.ITEM_CLICK);
		tag.setInteger("networkID", networkID);
		if (stack != null) {
			stack.writeData(tag, SyncType.SAVE);
		}
		return tag;
	}

	public static void doItemPacket(DisplayGSI gsi, DisplayScreenClick click, EntityPlayer player, NBTTagCompound clickTag) {
		StoredItemStack clicked = NBTHelper.instanceNBTSyncable(StoredItemStack.class, clickTag);
		int networkID = clickTag.getInteger("networkID");

		SonarCore.proxy.getThreadListener(Side.SERVER).addScheduledTask(() -> {
			GSIInteractionHelper.screenItemStackClicked(networkID, clicked.item.isEmpty() ? null : clicked, click, player, clickTag);
		});
		
	}

	//// FLUID PACKET \\\\

	public static NBTTagCompound createFluidClickPacket(StoredFluidStack stack, int networkID) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIClickPacket.FLUID_CLICK);
		tag.setInteger("networkID", networkID);
		if (stack != null) {
			stack.writeData(tag, SyncType.SAVE);
		}
		return tag;
	}

	public static void doFluidPacket(DisplayGSI gsi, DisplayScreenClick click, EntityPlayer player, NBTTagCompound clickTag) {
		StoredFluidStack clicked = NBTHelper.instanceNBTSyncable(StoredFluidStack.class, clickTag);
		GSIInteractionHelper.onScreenFluidStackClicked(clickTag.getInteger("networkID"), clicked.fluid == null ? null : clicked, click, player, clickTag);
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
			//SonarCore.instance.guiHandler.openGui(false, player, tile.getWorld(), tile.getPos(), GuiState.SOURCE.ordinal(), tag);
		}
		
	}


}
