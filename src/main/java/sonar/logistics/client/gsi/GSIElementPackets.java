package sonar.logistics.client.gsi;

public enum GSIElementPackets {
	INFO_SET(GSIElementPacketHelper::doInfoRequirementPacket), //
	GUI_REQUEST(GSIElementPacketHelper::doGuiRequestPacket),
	INFO_ADDITION(GSIElementPacketHelper::doInfoAdditionPacket),
	DELETE_CONTAINERS(GSIElementPacketHelper::doDeleteContainersPacket),
	RESIZE_CONTAINER(GSIElementPacketHelper::doResizeContainerPacket);

	IGSIElementPacketHandler logic;

	GSIElementPackets(IGSIElementPacketHandler logic) {
		this.logic = logic;
	}
}
