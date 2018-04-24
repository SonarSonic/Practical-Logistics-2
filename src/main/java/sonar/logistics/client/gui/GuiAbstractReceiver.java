package sonar.logistics.client.gui;

import net.minecraft.client.gui.GuiButton;
import sonar.core.helpers.FontHelper;
import sonar.logistics.api.wireless.ClientWirelessEmitter;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.client.gui.generic.GuiSelectionList;
import sonar.logistics.common.containers.ContainerDataReceiver;
import sonar.logistics.common.multiparts.wireless.TileAbstractReceiver;
import sonar.logistics.helpers.InfoRenderer;

public abstract class GuiAbstractReceiver extends GuiSelectionList<ClientWirelessEmitter> {
	public TileAbstractReceiver tile;

	public GuiAbstractReceiver(TileAbstractReceiver tileDataReceiver) {
		super(new ContainerDataReceiver(tileDataReceiver), tileDataReceiver);
		this.tile = tileDataReceiver;
		this.xSize = 182 + 66;
	}

	public void selectionPressed(GuiButton button, int infoPos, int buttonID, ClientWirelessEmitter info) {
		if (buttonID == 0) {
			tile.selectedEmitter.setObject(info);
			tile.sendByteBufPacket(0);
		} else {
			RenderBlockSelection.addPosition(info.coords.getCoords(), false);
		}
	}

	@Override
	public boolean isCategoryHeader(ClientWirelessEmitter info) {
		if (!RenderBlockSelection.positions.isEmpty()) {
            return RenderBlockSelection.isPositionRenderered(info.coords.getCoords());
		}
		return false;
	}

	@Override
	public boolean isSelectedInfo(ClientWirelessEmitter info) {
        return tile.clientEmitters.getObjects().contains(info);
    }

	@Override
	public void renderInfo(ClientWirelessEmitter info, int yPos) {
		int colour = LogisticsColours.white_text.getRGB();
		FontHelper.text(info.name.getObject(), InfoRenderer.left_offset, yPos, colour);
		FontHelper.text(info.coords.getCoords().toString(), (int) ((1.0 / 0.75) * (130)), yPos, colour);
	}

	@Override
	public int getColour(int i, int type) {
		return LogisticsColours.getDefaultSelection().getRGB();
	}

	@Override
	public boolean isPairedInfo(ClientWirelessEmitter info) {
		return false;
	}

}
