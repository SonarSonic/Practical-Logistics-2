package sonar.logistics.core.tiles.nodes.entity;

import net.minecraft.client.gui.GuiButton;
import sonar.core.helpers.FontHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.PL2Translate;
import sonar.logistics.base.gui.GuiLogistics;
import sonar.logistics.base.gui.PL2Colours;

import java.io.IOException;

public class GuiEntityNode extends GuiLogistics {
	public TileEntityNode part;

	public GuiEntityNode(TileEntityNode tileEntityNode) {
		super(new ContainerMultipartSync(tileEntityNode), tileEntityNode);
		this.part = tileEntityNode;
		this.xSize = 176;
		this.ySize = 80;
	}

	public void initGui() {
		super.initGui();
		buttonList.add(new GuiButton(0, guiLeft + 6, guiTop + 20, 80, 20, PL2Translate.BUTTON_TARGET.t() + ": " + part.entityTarget.getObject()));
		buttonList.add(new GuiButton(1, guiLeft + 90, guiTop + 20, 80, 20, part.nearest.getObject() ? PL2Translate.BUTTON_NEAREST.t() : PL2Translate.BUTTON_FURTHEST.t()));
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
		FontHelper.textCentre(PL2Translate.ENTITY_NODE.t(), xSize, 6, PL2Colours.white_text.getRGB());
		FontHelper.textCentre(PL2Translate.ENTITY_NODE_RANGE.t() + ": " + part.entityRange.getObject(), xSize, 56, PL2Colours.white_text.getRGB());

	}

	@Override
	public void mouseClicked(int i, int j, int k) throws IOException {
		super.mouseClicked(i, j, k);
	}
}
