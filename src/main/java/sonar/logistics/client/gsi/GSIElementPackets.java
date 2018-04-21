package sonar.logistics.client.gsi;

public enum GSIElementPackets {
	INFO_SET(GSIElementPacketHelper::doInfoRequirementPacket), //
	GUI_REQUEST(GSIElementPacketHelper::doGuiRequestPacket),
	INFO_ADDITION(GSIElementPacketHelper::doInfoAdditionPacket),
	DELETE_CONTAINERS(GSIElementPacketHelper::doDeleteContainersPacket),
	DELETE_ELEMENTS(GSIElementPacketHelper::doDeleteElementsPacket),
	RESIZE_CONTAINER(GSIElementPacketHelper::doResizeContainerPacket),
	CONFIGURE_INFO_ELEMENT(GSIElementPacketHelper::doConfigureInfoPacket),
	EDIT_MODE(GSIElementPacketHelper::doEditModePacket),
	EDIT_ELEMENT(GSIElementPacketHelper::doEditElementPacket),
	UPDATE_ELEMENT(GSIElementPacketHelper::doUpdateElementPacket);

	IGSIElementPacketHandler logic;

	GSIElementPackets(IGSIElementPacketHandler logic) {
		this.logic = logic;
	}
}
