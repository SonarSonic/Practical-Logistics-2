package sonar.logistics.client.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiTextField;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.common.multiparts.EntityNodePart;
import sonar.logistics.common.multiparts.NodePart;

public class GuiEntityNode extends GuiLogistics {
	public EntityNodePart part;

	public GuiEntityNode(EntityNodePart entityNodePart) {
		super(new ContainerMultipartSync(entityNodePart), entityNodePart);
		this.part = entityNodePart;
		this.xSize=66;
		this.ySize = 40;
	}

	public void initGui() {
		super.initGui();
	}

	@Override
	public void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		FontHelper.textCentre(FontHelper.translate("item.EntityNode.name"), xSize, 6, LogisticsColours.white_text.getRGB());
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
