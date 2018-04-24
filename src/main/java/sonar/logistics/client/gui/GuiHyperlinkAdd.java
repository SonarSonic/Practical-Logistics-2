package sonar.logistics.client.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import net.minecraft.inventory.Container;
import net.minecraftforge.fml.client.FMLClientHandler;
import sonar.core.client.gui.SonarTextField;
import sonar.core.helpers.FontHelper;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.elements.IHyperlinkRequirement;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.generic.info.HyperlinkRequest;

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
		FontHelper.textCentre("Add hyperlink", xSize, 6, LogisticsColours.white_text.getRGB());
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
