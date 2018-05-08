package sonar.logistics.base.requests.hyperlink;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.input.Keyboard;
import sonar.core.client.gui.SonarTextField;
import sonar.core.helpers.FontHelper;
import sonar.logistics.base.gui.GuiLogistics;
import sonar.logistics.base.gui.PL2Colours;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.info.elements.base.IHyperlinkRequirement;

import java.io.IOException;

public class GuiHyperlinkAdd extends GuiLogistics {

	public IHyperlinkRequirement element;
	public String hyperlink;
	public DisplayGSI gsi;
	private SonarTextField nameField;

	public GuiHyperlinkAdd(IHyperlinkRequirement element, DisplayGSI gsi, Container container) {
		super(container, gsi.getDisplay());
		this.element = element;
		this.hyperlink = element.getHyperlink();
		this.ySize = 60;
	}

	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		nameField = new SonarTextField(0, this.fontRenderer, 8, 18, 160, 12);
		nameField.setMaxStringLength(Integer.MAX_VALUE);
		nameField.setText(getCurrentHyperlink());
		fieldList.add(nameField);
	}

	public String getCurrentHyperlink() {
		return nameField == null ? hyperlink : nameField.getText();
	}

	@Override
	public void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		FontHelper.textCentre("Add hyperlink", xSize, 6, PL2Colours.white_text.getRGB());
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException {
		if (isCloseKey(i)) {
			boolean isTyping = this.fieldList.stream().anyMatch(GuiTextField::isFocused);
			if (!isTyping) {
				element.onGuiClosed(getCurrentHyperlink());
				if (element instanceof HyperlinkRequest) {
					FMLClientHandler.instance().showGuiScreen(((HyperlinkRequest) element).screen);
					return;
				}
			}
		}
		super.keyTyped(c, i);
	}

}
