package sonar.logistics.client.gui;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import sonar.core.helpers.FontHelper;
import sonar.logistics.PL2;
import sonar.logistics.api.wireless.ClientDataEmitter;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.client.gui.generic.GuiSelectionList;
import sonar.logistics.common.containers.ContainerDataReceiver;
import sonar.logistics.common.multiparts.wireless.TileDataReceiver;
import sonar.logistics.helpers.InfoRenderer;

public class GuiDataReceiver extends GuiSelectionList<ClientDataEmitter> {
	public TileDataReceiver tile;

	public GuiDataReceiver(TileDataReceiver tileDataReceiver) {
		super(new ContainerDataReceiver(tileDataReceiver), tileDataReceiver);
		this.tile = tileDataReceiver;
		this.xSize = 182 + 66;
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		//FontHelper.textCentre(PL2Translate.DATA_RECEIVER.t(), xSize, 6, LogisticsColours.white_text);
		//FontHelper.textCentre(PL2Translate.DATA_RECEIVER_HELP.t(), xSize, 18, LogisticsColours.grey_text);
	}

	public void selectionPressed(GuiButton button, int infoPos, int buttonID, ClientDataEmitter info) {
		if (buttonID == 0) {
			tile.selectedEmitter.setObject(info);
			tile.sendByteBufPacket(0);
		} else {
			RenderBlockSelection.addPosition(info.coords.getCoords(), false);
		}
	}

	public void setInfo() {
		infoList = Lists.newArrayList(PL2.getClientManager().clientEmitters);
	}

	@Override
	public boolean isCategoryHeader(ClientDataEmitter info) {
		if (!RenderBlockSelection.positions.isEmpty()) {
			if (RenderBlockSelection.isPositionRenderered(info.coords.getCoords())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSelectedInfo(ClientDataEmitter info) {
		if (tile.clientEmitters.getObjects().contains(info)) {
			return true;
		}
		return false;
	}

	@Override
	public void renderInfo(ClientDataEmitter info, int yPos) {
		int colour = LogisticsColours.white_text.getRGB();
		FontHelper.text(info.name.getObject(), InfoRenderer.identifierLeft, yPos, colour);
		FontHelper.text(info.coords.getCoords().toString(), (int) ((1.0 / 0.75) * (130)), yPos, colour);
	}

	@Override
	public int getColour(int i, int type) {
		return LogisticsColours.getDefaultSelection().getRGB();
	}

	@Override
	public boolean isPairedInfo(ClientDataEmitter info) {
		return false;
	}

}
