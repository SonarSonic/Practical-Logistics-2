package sonar.logistics.client.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiTextField;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.common.multiparts.DataEmitterPart;
import sonar.logistics.common.multiparts.NodePart;

public class GuiNode extends GuiLogistics {
	public NodePart part;
	private GuiTextField priority;

	public GuiNode(NodePart part) {
		super(new ContainerMultipartSync(part), part);
		this.part = part;
		this.xSize=66;
		this.ySize = 40;
	}

	public void initGui() {
		super.initGui();
		priority = new GuiTextField(0, this.fontRendererObj, 8, 18, 50, 12);
		priority.setMaxStringLength(7);
		priority.setText("" + part.getPriority());
	}

	@Override
	public void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		priority.drawTextBox();
		FontHelper.textCentre(FontHelper.translate("item.Node.name"), xSize, 6, LogisticsColours.white_text.getRGB());
		//FontHelper.text("Priority: ", 8, 20, LogisticsColours.white_text.getRGB());
	}

	@Override
	public void mouseClicked(int i, int j, int k) throws IOException {
		super.mouseClicked(i, j, k);
		priority.mouseClicked(i - guiLeft, j - guiTop, k);
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException {
		if (priority.isFocused()) {
			if (c == 13 || c == 27) {
				priority.setFocused(false);
			} else {
				FontHelper.addDigitsToString(priority, c, i);
				final String text = priority.getText();
				setPriority((text.isEmpty() || text == "" || text == null) ? 0 : Integer.parseInt(text));
			}
		} else {
			super.keyTyped(c, i);
		}
	}

	public void setPriority(int priority) {
		this.part.priority.setObject(priority);
		part.sendByteBufPacket(1);
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
		RenderHelper.restoreBlendState();
	}
}
