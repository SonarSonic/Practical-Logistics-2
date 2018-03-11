package sonar.logistics.client.gui.display;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.gui.textedit.GuiStyledStringFunctions;

public class SpecialFormatButton extends LogisticsButton {

	public TextFormatting specialFormat;
	public final GuiStyledStringFunctions gui;

	public SpecialFormatButton(GuiStyledStringFunctions gui, TextFormatting specialFormat, int id, int x, int y, int texX, int texY, String buttonText, String helpKey) {
		super(gui, id, x, y, texX, texY, buttonText, helpKey);
		this.gui = gui;
		this.specialFormat = specialFormat;
	}

	@Override
	public void drawButton(Minecraft mc, int x, int y, float partialTicks) {
		if (this.visible) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
			mc.getTextureManager().bindTexture(texture);
			this.drawTexturedModalRect(this.x, this.y, getTextureX(), getTextureY(), sizeX + 1, sizeY + 1);

			// select
			if (gui.specials.contains(specialFormat)) {
				this.drawTexturedModalRect(this.x, this.y, (15 * 16), (15 * 16), sizeX + 1, sizeY + 1);
			}
		}
	}
}