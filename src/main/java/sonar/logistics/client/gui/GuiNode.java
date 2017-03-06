package sonar.logistics.client.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiTextField;
import sonar.core.client.gui.SonarTextField;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.common.multiparts.NodePart;

public class GuiNode extends GuiLogistics {
	public NodePart part;
	private SonarTextField priority;

	public GuiNode(NodePart part) {
		super(new ContainerMultipartSync(part), part);
		this.part = part;
		this.xSize = 66;
		this.ySize = 40;
	}

	public void initGui() {
		super.initGui();
		priority = new SonarTextField(0, this.fontRendererObj, 8, 18, 50, 12);
		priority.setMaxStringLength(7);
		priority.setText("" + part.getPriority());
		priority.setDigitsOnly(true);
		fieldList.add(priority);
	}

	@Override
	public void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		FontHelper.textCentre(FontHelper.translate("item.Node.name"), xSize, 6, LogisticsColours.white_text.getRGB());
	}

	public void onTextFieldChanged(SonarTextField field) {
		if (field == priority) {
			this.part.priority.setObject(priority.getIntegerFromText());
			part.sendByteBufPacket(1);
		}
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
		RenderHelper.restoreBlendState();
	}
}
