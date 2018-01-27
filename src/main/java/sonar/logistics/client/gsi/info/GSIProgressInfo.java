package sonar.logistics.client.gsi.info;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import sonar.logistics.api.info.render.IDisplayInfo;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.client.gsi.AbstractGSI;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.ProgressInfo;

public class GSIProgressInfo extends AbstractGSI<ProgressInfo> {

	@Override
	public void renderGSIForeground(ProgressInfo info, InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		super.renderGSIForeground(info, container, displayInfo, width, height, scale, infoPos);
		GL11.glPushMatrix();
		GL11.glPushMatrix();
		GlStateManager.disableLighting();
		GL11.glTranslated(-1, -+0.0625 * 12, +0.004);
		Minecraft.getMinecraft().getTextureManager().bindTexture(InfoContainer.getColour(infoPos));
		double num1 = (info.compare == 1 ? info.secondNum : info.firstNum);
		double num2 = (info.compare == 1 ? info.firstNum : info.secondNum);
		InfoRenderer.renderProgressBar(width, height, num1 < 0 ? 0 : num1, num2);
		GlStateManager.enableLighting();
		GL11.glTranslated(0, 0, -0.001);
		GL11.glPopMatrix();
		InfoRenderer.renderNormalInfo(container.display.getDisplayType(), width, height, scale, displayInfo.getFormattedStrings());
		GL11.glPopMatrix();
	}
}
