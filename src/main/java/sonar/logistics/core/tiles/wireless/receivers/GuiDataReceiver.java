package sonar.logistics.core.tiles.wireless.receivers;

import com.google.common.collect.Lists;
import sonar.core.helpers.FontHelper;
import sonar.logistics.PL2Translate;
import sonar.logistics.base.ClientInfoHandler;
import sonar.logistics.base.gui.PL2Colours;

public class GuiDataReceiver extends GuiAbstractReceiver {

	public GuiDataReceiver(TileAbstractReceiver tileDataReceiver) {
		super(tileDataReceiver);
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		FontHelper.textCentre(PL2Translate.DATA_RECEIVER.t(), xSize, 6, PL2Colours.white_text);
		FontHelper.textCentre(PL2Translate.DATA_RECEIVER_HELP.t(), xSize, 18, PL2Colours.grey_text);
	}

	public void setInfo() {
		infoList = Lists.newArrayList(ClientInfoHandler.instance().clientDataEmitters);
	}
}
