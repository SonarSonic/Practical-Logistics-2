package sonar.logistics.client.gsi;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.core.SonarCore;
import sonar.core.api.IFlexibleGui;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.FlexibleGuiHandler;
import sonar.logistics.PL2;
import sonar.logistics.api.displays.CreateInfoType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.IDisplayElement;
import sonar.logistics.api.displays.elements.DisplayElementContainer;
import sonar.logistics.api.displays.elements.IInfoRequirement;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.packets.PacketGSIElement;

public class GSIElementPacketHelper {

	public static IGSIElementPacketHandler handler = GSIElementPacketHelper::runGSIElementPacket;
	public static final String PACKET_ID = "PktID";

	public static void sendGSIPacket(NBTTagCompound tag, int elementID, DisplayGSI gsi) {
		if (!tag.hasNoTags()) {
			PL2.network.sendToServer(new PacketGSIElement(gsi.getDisplayGSIIdentity(), elementID, tag));
		}
	}

	public static void runGSIElementPacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound clickTag) {
		readPacketID(clickTag).logic.runGSIElementPacket(gsi, element, player, clickTag);
	}

	public static NBTTagCompound createBasicPacket(GSIElementPackets packet) {
		return writePacketID(new NBTTagCompound(), packet);
	}

	public static NBTTagCompound writePacketID(NBTTagCompound tag, GSIElementPackets packet) {
		tag.setInteger(PACKET_ID, packet.ordinal());
		return tag;
	}

	public static GSIElementPackets readPacketID(NBTTagCompound tag) {
		return GSIElementPackets.values()[tag.getInteger(PACKET_ID)];
	}

	//// CREATE INFO TYPE \\\\

	public static NBTTagCompound createInfoAdditionPacket(double[] translate, double[] scale, double pScale, CreateInfoType type) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIElementPackets.INFO_ADDITION);
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
		IDisplayElement e = CreateInfoType.values()[packetTag.getInteger("type")].logic.create(c);
		gsi.display.sendInfoContainerPacket();
	}

	//// REQUEST GUI \\\\

	public static NBTTagCompound createGuiRequestPacket(int guiID) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIElementPackets.GUI_REQUEST);
		tag.setInteger("GUI_ID", guiID);
		return tag;
	}

	public static void doGuiRequestPacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound packetTag) {
		if (element instanceof IFlexibleGui && gsi.getDisplay().getActualDisplay() instanceof TileAbstractDisplay) {
			NBTTagCompound tag = new NBTTagCompound();
			int slotID = ((TileAbstractDisplay) gsi.getDisplay().getActualDisplay()).getSlotID();
			if (slotID == -1) {
				tag.setBoolean(FlexibleGuiHandler.TILEENTITY, true);
			} else {
				tag.setBoolean(FlexibleGuiHandler.MULTIPART, true);
				tag.setInteger(FlexibleGuiHandler.SLOT_ID, slotID);
			}
			tag.setInteger("ELE_ID", element.getElementIdentity());
			tag.setInteger("CONT_ID", element.getHolder().getContainer().getContainerIdentity());
			SonarCore.instance.guiHandler.openGui(false, player, player.getEntityWorld(), gsi.getDisplay().getCoords().getBlockPos(), packetTag.getInteger("GUI_ID"), tag);
		}
	}

	//// INFO REQUIREMENT \\\\

	public static NBTTagCompound createInfoRequirementPacket(List<InfoUUID> uuids, int requiredRef) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIElementPackets.INFO_SET);
		NBTTagList list = new NBTTagList();
		uuids.forEach(uuid -> list.appendTag(uuid.writeData(new NBTTagCompound(), SyncType.SAVE)));
		tag.setTag("uuids", list);
		tag.setInteger("ref", requiredRef);
		return tag;
	}

	public static void doInfoRequirementPacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound packetTag) {
		if (element instanceof IInfoRequirement) {
			IInfoRequirement require = (IInfoRequirement) element;
			NBTTagList tag = packetTag.getTagList("uuids", NBT.TAG_COMPOUND);
			List<InfoUUID> required = Lists.newArrayList();
			for (int i = 0; i < tag.tagCount(); i++) {
				NBTTagCompound nbt = tag.getCompoundTagAt(i);
				InfoUUID uuid = NBTHelper.instanceNBTSyncable(InfoUUID.class, nbt);
				if (InfoUUID.valid(uuid)) {
					required.add(uuid);
				}
			}
			if (required.size() == require.getRequired()) {
				require.doInfoRequirementPacket(gsi, player, required, packetTag.getInteger("ref"));
			}
		}
	}

	//// DELETE CONTAINERS \\\\

	public static NBTTagCompound createDeleteContainersPacket(List<Integer> toDelete) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIElementPackets.DELETE_CONTAINERS);
		NBTTagList list = new NBTTagList();
		toDelete.forEach(del -> list.appendTag(new NBTTagInt(del)));
		tag.setTag("del", list);
		return tag;
	}

	public static void doDeleteContainersPacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound packetTag) {
		NBTTagList tag = packetTag.getTagList("del", NBT.TAG_INT);
		List<Integer> toDelete = Lists.newArrayList();
		for (int i = 0; i < tag.tagCount(); i++) {
			toDelete.add(tag.getIntAt(i));
		}
		toDelete.forEach(del -> gsi.removeElementContainer(del));
	}

	//// DELETE CONTAINERS \\\\

	public static NBTTagCompound createResizeContainerPacket(int containerID, double[] translate, double[] scale, double pScale) {
		NBTTagCompound tag = new NBTTagCompound();
		writePacketID(tag, GSIElementPackets.RESIZE_CONTAINER);
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
			gsi.sendInfoContainerPacket();
		}
	}

}
