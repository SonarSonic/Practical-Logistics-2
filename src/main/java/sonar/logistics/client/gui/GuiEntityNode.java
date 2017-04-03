package sonar.logistics.client.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.PL2Translate;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.common.multiparts.EntityNodePart;

public class GuiEntityNode extends GuiLogistics {
	public EntityNodePart part;

	public GuiEntityNode(EntityNodePart entityNodePart) {
		super(new ContainerMultipartSync(entityNodePart), entityNodePart);
		this.part = entityNodePart;
		this.xSize = 176;
		this.ySize = 80;
	}

	public void initGui() {
		super.initGui();
		buttonList.add(new GuiButton(0, guiLeft + 6, guiTop + 20, 80, 20, "Target: " + part.entityTarget.getObject()));
		buttonList.add(new GuiButton(1, guiLeft + 90, guiTop + 20, 80, 20, part.nearest.getObject() ? "Nearest" : "Furthest"));
		buttonList.add(new GuiButton(2, guiLeft + 10, guiTop + 50, 20, 20, "<"));
		buttonList.add(new GuiButton(3, guiLeft + xSize - 30, guiTop + 50, 20, 20, ">"));
	}

	public void actionPerformed(GuiButton button) {
		if (button != null) {
			switch (button.id) {
			case 0:
				part.entityTarget.incrementEnum();
				part.sendByteBufPacket(0);
				reset();
				break;
			case 1:
				part.nearest.invert();
				part.sendByteBufPacket(3);
				reset();
				break;
			case 2:
				part.sendByteBufPacket(2);
				break;
			case 3:
				part.sendByteBufPacket(1);
				break;
			}

		}
	}

	@Override
	public void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		FontHelper.textCentre(PL2Translate.ENTITY_NODE.t(), xSize, 6, LogisticsColours.white_text.getRGB());
		FontHelper.textCentre("Entity Range: " + part.entityRange.getObject(), xSize, 56, LogisticsColours.white_text.getRGB());

	}

	@Override
	public void mouseClicked(int i, int j, int k) throws IOException {
		super.mouseClicked(i, j, k);
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
		RenderHelper.restoreBlendState();
	}
}
