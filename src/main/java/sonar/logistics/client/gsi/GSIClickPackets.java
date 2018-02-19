package sonar.logistics.client.gsi;

public enum GSIClickPackets {
	ITEM_CLICK(GSIClickPacketHelper::doItemPacket), FLUID_CLICK(GSIClickPacketHelper::doFluidPacket), SOURCE_BUTTON(GSIClickPacketHelper::doSourceButtonPacket);

	IGSIClickPacketHandler logic;

	GSIClickPackets(IGSIClickPacketHandler logic) {
		this.logic = logic;
	}
}
