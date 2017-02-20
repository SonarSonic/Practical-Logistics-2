package sonar.logistics.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sonar.core.client.gui.GuiSonar;
import sonar.core.client.gui.SonarButtons.AnimatedButton;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.Logistics;

@SideOnly(Side.CLIENT)
public class LogisticsButton extends AnimatedButton {
	public static final ResourceLocation logisticsButtons = new ResourceLocation(Logistics.MODID + ":textures/gui/filter_buttons.png");
	public GuiSonar sonar;
	public String buttonText;
	public int texX, texY;

	public LogisticsButton(GuiSonar sonar, int id, int x, int y, int texX, int texY, String buttonText) {
		super(id, x, y, logisticsButtons, 15, 15);
		this.sonar=sonar;
		this.buttonText = buttonText;
		this.texX = texX;
		this.texY = texY;

	}

	public void drawButtonForegroundLayer(int x, int y) {
		sonar.drawSonarCreativeTabHoveringText(buttonText, x, y);
	}

	public void drawButton(Minecraft mc, int x, int y) {
		super.drawButton(mc, x, y);
	}
	
	@Override
	public void onClicked() {}

	@Override
	public int getTextureX() {
		return texX;
	}

	@Override
	public int getTextureY() {
		return texY;
	}

}