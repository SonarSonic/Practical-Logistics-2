package sonar.logistics.core.tiles.displays.gsi.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.core.SonarCore;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.FlexibleGuiHandler;
import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.requests.info.IInfoRequirement;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.modes.GSICreateInfo;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayElementContainer;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayGSISaveHandler;
import sonar.logistics.core.tiles.displays.info.elements.AbstractDisplayElement;
import sonar.logistics.core.tiles.displays.info.elements.DisplayElementHelper;
import sonar.logistics.core.tiles.displays.info.elements.UnconfiguredInfoElement;
import sonar.logistics.core.tiles.displays.info.elements.base.IDisplayElement;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;
import sonar.logistics.network.packets.gsi.PacketGSIElement;

import java.util.ArrayList;
import java.util.List;

public class GSIElementPacketHelper {

	public static GSIElementPacket.IGSIElementPacketHandler handler = GSIElementPacketHelper::runGSIElementPacket;
	public static final String PACKET_ID = "PktID";

	public static void sendGSIPacket(NBTTagCompound tag, int elementID, DisplayGSI gsi) {
		if (!tag.hasNoTags()) {
			PL2.network.sendToServer(new PacketGSIElement(gsi.getDisplayGSIIdentity(), elementID, tag));
		}
	}

	public static void runGSIElementPacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound clickTag) {
		readPacketID(clickTag).logic.runGSIElementPacket(gsi, element, player, clickTag);
	}

	public static NBTTagCompound createBasicPacket(GSIElementPacket packet) {
		return writePacketID(new NBTTagCompound(), packet);
	}

	public static NBTTagCompound writePacketID(NBTTagCompound tag, GSIElementPacket packet) {
		tag.setInteger(PACKET_ID, packet.ordinal());
		return tag;
	}

	public static GSIElementPacket readPacketID(NBTTagCompound tag) {
		return GSIElementPacket.values()[tag.getInteger(PACKET_ID)];
	}

	//// CREATE INFO TYPE \\\\

	public static NBTTagCompound createInfoAdditionPacket(double[] translate, double[] scale, double pScale, GSICreateInfo type) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIElementPacket.INFO_ADDITION);
		NBTHelper.writeDoubleArray(tag, translate, "translate");
		NBTHelper.writeDoubleArray(tag, scale, "scale");
		tag.setDouble("pscale", pScale);
		tag.setInteger("type", type.ordinal());
		return tag;
	}

	public static void doInfoAdditionPacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound packetTag) {
		double[] translate = NBTHelper.readDoubleArray(packetTag, "translate", 3);
		double[] scale = NBTHelper.readDoubleArray(packetTag, "scale", 3);
		double pScale = packetTag.getDouble("pscale");
		DisplayElementContainer c = gsi.addElementContainer(translate, scale, pScale);
		IDisplayElement e = GSICreateInfo.values()[packetTag.getInteger("type")].logic.create(c);
		gsi.sendInfoContainerPacket(DisplayGSISaveHandler.DisplayGSISavedData.ALL_DATA);
	}

	//// REQUEST GUI \\\\

	public static NBTTagCompound createGuiRequestPacket(int guiID, NBTTagCompound packetTag) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIElementPacket.GUI_REQUEST);
		tag.setInteger("GUI_ID", guiID);
		tag.setTag("gui_tag", packetTag);
		return tag;
	}

	public static void doGuiRequestPacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound packetTag) {
		if (gsi.getDisplay().getActualDisplay() instanceof TileAbstractDisplay) {
			NBTTagCompound tag = packetTag.getCompoundTag("gui_tag");
			int slotID = ((TileAbstractDisplay) gsi.getDisplay().getActualDisplay()).getSlotID();
			if (slotID == -1) {
				tag.setBoolean(FlexibleGuiHandler.TILEENTITY, true);
			} else {
				tag.setBoolean(FlexibleGuiHandler.MULTIPART, true);
				tag.setInteger(FlexibleGuiHandler.SLOT_ID, slotID);
			}
			if (!tag.hasKey("ELE_ID"))
				tag.setInteger("ELE_ID", element == null ? -1 : element.getElementIdentity());
			if (!tag.hasKey("CONT_ID"))
				tag.setInteger("CONT_ID", element == null ? -1 : element.getHolder().getContainer().getContainerIdentity());
			SonarCore.instance.guiHandler.openGui(false, player, player.getEntityWorld(), gsi.getDisplay().getCoords().getBlockPos(), packetTag.getInteger("GUI_ID"), tag);
		}
	}

	//// INFO REQUIREMENT \\\\

	public static NBTTagCompound createInfoRequirementPacket(List<InfoUUID> uuids) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIElementPacket.INFO_SET);
		NBTTagList list = new NBTTagList();
		uuids.forEach(uuid -> list.appendTag(uuid.writeData(new NBTTagCompound(), SyncType.SAVE)));
		tag.setTag("uuids", list);
		return tag;
	}

	public static void doInfoRequirementPacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound packetTag) {
		if (element instanceof IInfoRequirement) {
			IInfoRequirement require = (IInfoRequirement) element;
			NBTTagList tag = packetTag.getTagList("uuids", NBT.TAG_COMPOUND);
			List<InfoUUID> required = new ArrayList<>();
			for (int i = 0; i < tag.tagCount(); i++) {
				NBTTagCompound nbt = tag.getCompoundTagAt(i);
				InfoUUID uuid = NBTHelper.instanceNBTSyncable(InfoUUID.class, nbt);
				if (InfoUUID.valid(uuid)) {
					required.add(uuid);
				}
			}
			if (required.size() == require.getRequired()) {
				require.doInfoRequirementPacket(gsi, player, required);
			}
		}
	}

	//// DELETE CONTAINERS \\\\

	public static NBTTagCompound createDeleteContainersPacket(List<Integer> toDelete) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIElementPacket.DELETE_CONTAINERS);
		NBTTagList list = new NBTTagList();
		toDelete.forEach(del -> list.appendTag(new NBTTagInt(del)));
		tag.setTag("del", list);
		return tag;
	}

	public static void doDeleteContainersPacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound packetTag) {
		NBTTagList tag = packetTag.getTagList("del", NBT.TAG_INT);
		List<Integer> toDelete = new ArrayList<>();
		for (int i = 0; i < tag.tagCount(); i++) {
			toDelete.add(tag.getIntAt(i));
		}
		toDelete.forEach(gsi::removeElementContainer);
		gsi.sendInfoContainerPacket(DisplayGSISaveHandler.DisplayGSISavedData.ALL_DATA);
	}

	//// DELETE ELEMENTS \\\\

	public static NBTTagCompound createDeleteElementsPacket(List<Integer> toDelete) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIElementPacket.DELETE_ELEMENTS);
		NBTTagList list = new NBTTagList();
		toDelete.forEach(del -> list.appendTag(new NBTTagInt(del)));
		tag.setTag("del", list);
		return tag;
	}

	public static void doDeleteElementsPacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound packetTag) {
		NBTTagList tag = packetTag.getTagList("del", NBT.TAG_INT);
		List<Integer> toDelete = new ArrayList<>();
		for (int i = 0; i < tag.tagCount(); i++) {
			toDelete.add(tag.getIntAt(i));
		}
		toDelete.forEach(gsi::removeElement);
		gsi.sendInfoContainerPacket(DisplayGSISaveHandler.DisplayGSISavedData.ALL_DATA);
	}

	//// RESIZE CONTAINERS \\\\

	public static NBTTagCompound createResizeContainerPacket(int containerID, double[] translate, double[] scale, double pScale) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIElementPacket.RESIZE_CONTAINER);
		tag.setInteger("CONT_ID", containerID);
		NBTHelper.writeDoubleArray(tag, translate, "translate");
		NBTHelper.writeDoubleArray(tag, scale, "scale");
		tag.setDouble("pscale", pScale);
		return tag;
	}

	public static void doResizeContainerPacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound packetTag) {
		int containerID = packetTag.getInteger("CONT_ID");
		double[] translate = NBTHelper.readDoubleArray(packetTag, "translate", 3);
		double[] scale = NBTHelper.readDoubleArray(packetTag, "scale", 3);
		double pScale = packetTag.getDouble("pscale");
		DisplayElementContainer c = gsi.getContainer(containerID);
		if (c != null) {
			c.resize(translate, scale, pScale);
			c.updateActualScaling();
			gsi.sendInfoContainerPacket(DisplayGSISaveHandler.DisplayGSISavedData.CONTAINERS);
		}
	}

	//// CONFIGURE INFO \\\\

	public static NBTTagCompound createConfigureInfoPacket(UnconfiguredInfoElement element) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIElementPacket.CONFIGURE_INFO_ELEMENT);
		NBTTagList list = new NBTTagList();
		for (IDisplayElement e : element.elements) {
			if (e != null) {
				list.appendTag(DisplayElementHelper.saveElement(new NBTTagCompound(), e, SyncType.SAVE));
			}
		}
		tag.setTag("elements", list);
		return tag;
	}

	public static void doConfigureInfoPacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound packetTag) {
		if (element instanceof UnconfiguredInfoElement) {
			element.getHolder().getElements().removeElement(element);
			NBTTagList tag = packetTag.getTagList("elements", NBT.TAG_COMPOUND);
			for (int i = 0; i < tag.tagCount(); i++) {
				NBTTagCompound nbt = tag.getCompoundTagAt(i);
				nbt.setInteger(AbstractDisplayElement.IDENTITY_TAG_NAME, ServerInfoHandler.instance().getNextIdentity());
				IDisplayElement e = DisplayElementHelper.loadElement(nbt, element.getHolder());
				element.getHolder().getElements().addElement(e);
			}
			gsi.sendInfoContainerPacket(DisplayGSISaveHandler.DisplayGSISavedData.ALL_DATA);
		}
	}

	//// SAVE TEXT \\\\

	public static NBTTagCompound createEditElementPacket(IDisplayElement e) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIElementPacket.EDIT_ELEMENT);
		tag.setTag("edits", e.writeData(new NBTTagCompound(), SyncType.SAVE));
		return tag;
	}

	public static void doEditElementPacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound packetTag) {
		NBTTagCompound tag = packetTag.getCompoundTag("edits");
		tag.setInteger(AbstractDisplayElement.IDENTITY_TAG_NAME, element.getElementIdentity());
		element.readData(tag, SyncType.SAVE);
		element.onElementChanged();
		gsi.sendInfoContainerPacket(DisplayGSISaveHandler.DisplayGSISavedData.ALL_DATA);
	}

	//// EDIT MODE \\\\

	public static NBTTagCompound createEditModePacket(boolean set) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIElementPacket.EDIT_MODE);
		tag.setBoolean("edit_mode", set);
		return tag;
	}

	public static void doEditModePacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound packetTag) {
		gsi.edit_mode.setObject(packetTag.getBoolean("edit_mode"));
		gsi.sendInfoContainerPacket(DisplayGSISaveHandler.DisplayGSISavedData.SYNC_PARTS);
	}

	//// UPDATE ELEMENT \\\\

	public static NBTTagCompound createUpdateElementPacket(IDisplayElement element, SyncType type) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIElementPacket.UPDATE_ELEMENT);
		tag.setTag("update_tag", element.writeData(new NBTTagCompound(), type));
		tag.setInteger("type", type.ordinal());
		return tag;
	}

	public static void doUpdateElementPacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound packetTag) {
		NBTTagCompound tag = packetTag.getCompoundTag("update_tag");
		element.readData(tag, SyncType.values()[packetTag.getInteger("type")]);
	}

	//// RESET GSI \\\\

	public static NBTTagCompound createResetGSIPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIElementPacket.RESET_GSI);
		return tag;
	}

	public static void doResetGSIPacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound packetTag) {
		gsi.containers.clear();
		gsi.edit_mode.setObject(true);
		gsi.sendInfoContainerPacket(DisplayGSISaveHandler.DisplayGSISavedData.ALL_DATA);
	}
}
