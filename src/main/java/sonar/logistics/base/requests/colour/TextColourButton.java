package sonar.logistics.base.requests.colour;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.RenderHelper;
import sonar.core.utils.CustomColour;

import javax.annotation.Nonnull;

public abstract class TextColourButton extends GuiButton {

	public int formattingColour;
	public int formattingShadow;
	public int shadowRGB, colourRGB;
	public TextFormatting colour;
	public final GuiSonar gui;

	public TextColourButton(GuiSonar gui, int id, int x, int y, TextFormatting c) {
		super(id, x, y, 12, 12, c.getFriendlyName());
		this.gui = gui;
		formattingColour = RenderHelper.getTextFormattingColour(c);
		formattingShadow = RenderHelper.getTextFormattingShadow(c);
		int r = formattingColour >> 16 & 255;
		int g = formattingColour >> 8 & 255;
		int b = formattingColour & 255;
		int rS = formattingShadow >> 16 & 255;
		int gS = formattingShadow >> 8 & 255;
		int bS = formattingShadow & 255;
		shadowRGB = new CustomColour(rS, gS, bS).getRGB();
		colourRGB = new CustomColour(r, g, b).getRGB();
		
		colour = c;
	}
	
	public abstract boolean isSelected();

	@Override
	public void drawButton(@Nonnull Minecraft mc, int x, int y, float partialTicks) {
		if (this.visible) {
			this.hovered = x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			boolean isSelected = isSelected();//gui.currentColour == colourRGB;
			drawRect(this.x, this.y, this.x + 12, this.y + 12, isSelected ? -1 : shadowRGB);
			drawRect(this.x + 1, this.y + 1, this.x + 11, this.y + 11, colourRGB);
		}

	}

	@Override
	public void drawButtonForegroundLayer(int x, int y) {
		if (hovered) {
			gui.drawSonarCreativeTabHoveringText(this.displayString, x, y);
		}
	}
}