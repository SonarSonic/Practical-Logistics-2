package sonar.logistics.client.gui;

import sonar.core.client.gui.SonarTextField;
import sonar.core.helpers.FontHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.PL2Translate;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.common.multiparts.nodes.TileNode;

public class GuiNode extends GuiLogistics {
	public TileNode part;
	private SonarTextField priority;

	public GuiNode(TileNode part) {
		super(new ContainerMultipartSync(part), part);
		this.part = part;
		this.xSize = 66;
		this.ySize = 40;
	}

	public void initGui() {
		super.initGui();
		priority = new SonarTextField(0, this.fontRenderer, 8, 18, 50, 12);
		priority.setMaxStringLength(7);
		priority.setText("" + part.getPriority());
		priority.setDigitsOnly(true);
		fieldList.add(priority);
	}

	@Override
	public void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		FontHelper.textCentre(PL2Translate.NODE.t(), xSize, 6, LogisticsColours.white_text.getRGB());
	}

	public void onTextFieldChanged(SonarTextField field) {
		if (field == priority) {
			this.part.priority.setObject(priority.getIntegerFromText());
			part.sendByteBufPacket(1);
		}
	}
}
