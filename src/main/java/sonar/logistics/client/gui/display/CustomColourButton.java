package sonar.logistics.client.gui.display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.RenderHelper;
import sonar.core.utils.CustomColour;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.GuiLogistics;

public abstract class CustomColourButton extends GuiButton {

	public final GuiLogistics gui;

	public CustomColourButton(GuiLogistics gui, int id, int x, int y, String hover) {
		super(id, x, y, 12, 12, hover);
		this.gui = gui;
	}
	
	public abstract boolean isSelected();

	@Override
	public void drawButton(Minecraft mc, int x, int y, float partialTicks) {
		if (this.visible) {
			this.hovered = x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			boolean isSelected = isSelected();//gui.currentColour == colourRGB;
			drawRect(this.x, this.y, this.x + 12, this.y + 12, LogisticsColours.blue_overlay.getRGB());
			drawRect(this.x + 1, this.y + 1, this.x + 11, this.y + 11, GuiLogistics.getCurrentColour());
		}

	}

	@Override
	public void drawButtonForegroundLayer(int x, int y) {
		if (hovered) {
			gui.drawSonarCreativeTabHoveringText(this.displayString, x, y);
		}
	}
}