package sonar.logistics.client.gui;

import com.google.common.collect.Lists;

import sonar.core.helpers.FontHelper;
import sonar.logistics.PL2;
import sonar.logistics.PL2Translate;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.common.multiparts.wireless.TileAbstractReceiver;

public class GuiDataReceiver extends GuiAbstractReceiver {

	public GuiDataReceiver(TileAbstractReceiver tileDataReceiver) {
		super(tileDataReceiver);
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		FontHelper.textCentre(PL2Translate.DATA_RECEIVER.t(), xSize, 6, LogisticsColours.white_text);
		FontHelper.textCentre(PL2Translate.DATA_RECEIVER_HELP.t(), xSize, 18, LogisticsColours.grey_text);
	}

	public void setInfo() {
		infoList = Lists.newArrayList(PL2.getClientManager().clientDataEmitters);
	}
}
