package sonar.logistics.client.gui;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import sonar.core.helpers.FontHelper;
import sonar.logistics.PL2Translate;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.common.multiparts.wireless.TileRedstoneReceiver;
import sonar.logistics.networking.ClientInfoHandler;

public class GuiRedstoneReceiver extends GuiAbstractReceiver {

	public TileRedstoneReceiver receiver;

	public GuiRedstoneReceiver(TileRedstoneReceiver receiver) {
		super(receiver);
		this.receiver = receiver;
	}

	public void initGui() {
		super.initGui();
		this.buttonList.add(new LogisticsButton(this, -1, guiLeft + 2, guiTop + 2, 48, 0 + 16 * receiver.mode.getObject().ordinal(), "Emit If: " + receiver.mode.getObject().name(), "button.EmitterMode"));
		
	}

	public void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if (button.id == -1) {
			receiver.mode.incrementEnum();
			receiver.sendByteBufPacket(1);
			reset();
			return;
		}
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		FontHelper.textCentre(PL2Translate.REDSTONE_RECEIVER.t(), xSize, 6, LogisticsColours.white_text);
		FontHelper.textCentre(PL2Translate.REDSTONE_RECEIVER_HELP.t(), xSize, 18, LogisticsColours.grey_text);
	}

	public void setInfo() {
		infoList = Lists.newArrayList(ClientInfoHandler.instance().clientRedstoneEmitters);
	}
}
