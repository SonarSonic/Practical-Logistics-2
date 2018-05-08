package sonar.logistics.core.tiles.wireless.receivers;

import net.minecraft.client.gui.GuiButton;
import sonar.core.helpers.FontHelper;
import sonar.logistics.api.core.tiles.wireless.emitters.ClientWirelessEmitter;
import sonar.logistics.base.gui.GuiSelectionList;
import sonar.logistics.base.gui.PL2Colours;
import sonar.logistics.base.gui.overlays.OverlayBlockSelection;
import sonar.logistics.core.tiles.displays.info.InfoRenderHelper;

public abstract class GuiAbstractReceiver extends GuiSelectionList<ClientWirelessEmitter> {
	public TileAbstractReceiver tile;

	public GuiAbstractReceiver(TileAbstractReceiver tileDataReceiver) {
		super(new ContainerAbstractReceiver(tileDataReceiver), tileDataReceiver);
		this.tile = tileDataReceiver;
		this.xSize = 182 + 66;
	}

	public void selectionPressed(GuiButton button, int infoPos, int buttonID, ClientWirelessEmitter info) {
		if (buttonID == 0) {
			tile.selectedEmitter.setObject(info);
			tile.sendByteBufPacket(0);
		} else {
			OverlayBlockSelection.addPosition(info.coords.getCoords(), false);
		}
	}

	@Override
	public boolean isCategoryHeader(ClientWirelessEmitter info) {
		if (!OverlayBlockSelection.positions.isEmpty()) {
            return OverlayBlockSelection.isPositionRenderered(info.coords.getCoords());
		}
		return false;
	}

	@Override
	public boolean isSelectedInfo(ClientWirelessEmitter info) {
        return tile.clientEmitters.getObjects().contains(info);
    }

	@Override
	public void renderInfo(ClientWirelessEmitter info, int yPos) {
		int colour = PL2Colours.white_text.getRGB();
		FontHelper.text(info.name.getObject(), InfoRenderHelper.left_offset, yPos, colour);
		FontHelper.text(info.coords.getCoords().toString(), (int) ((1.0 / 0.75) * (130)), yPos, colour);
	}

	@Override
	public int getColour(int i, int type) {
		return PL2Colours.getDefaultSelection().getRGB();
	}

	@Override
	public boolean isPairedInfo(ClientWirelessEmitter info) {
		return false;
	}

}
