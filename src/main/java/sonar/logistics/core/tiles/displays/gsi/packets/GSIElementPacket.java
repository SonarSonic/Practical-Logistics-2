package sonar.logistics.core.tiles.displays.gsi.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.info.elements.base.IDisplayElement;

public enum GSIElementPacket {
	INFO_SET(GSIElementPacketHelper::doInfoRequirementPacket), //
	GUI_REQUEST(GSIElementPacketHelper::doGuiRequestPacket),
	INFO_ADDITION(GSIElementPacketHelper::doInfoAdditionPacket),
	DELETE_CONTAINERS(GSIElementPacketHelper::doDeleteContainersPacket),
	DELETE_ELEMENTS(GSIElementPacketHelper::doDeleteElementsPacket),
	RESET_GSI(GSIElementPacketHelper::doResetGSIPacket),
	RESIZE_CONTAINER(GSIElementPacketHelper::doResizeContainerPacket),
	CONFIGURE_INFO_ELEMENT(GSIElementPacketHelper::doConfigureInfoPacket),
	EDIT_MODE(GSIElementPacketHelper::doEditModePacket),
	EDIT_ELEMENT(GSIElementPacketHelper::doEditElementPacket),
	UPDATE_ELEMENT(GSIElementPacketHelper::doUpdateElementPacket);

	IGSIElementPacketHandler logic;

	GSIElementPacket(IGSIElementPacketHandler logic) {
		this.logic = logic;
	}


	public interface IGSIElementPacketHandler {

		void runGSIElementPacket(DisplayGSI gsi, IDisplayElement element, EntityPlayer player, NBTTagCompound clickTag);

	}
}
