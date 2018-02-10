package sonar.logistics.client.gsi.info;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.client.gsi.AbstractGSI;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.MonitoredEnergyStack;

public class GSIEnergyStack extends AbstractGSI<MonitoredEnergyStack> {

	@Override
	public void renderGSIForeground(MonitoredEnergyStack info, InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		super.renderGSIForeground(info, container, displayInfo, width, height, scale, infoPos);
		GL11.glPushMatrix();
		GL11.glPushMatrix();
		GlStateManager.disableLighting();
		GL11.glTranslated(-1, -+0.0625 * 12, +0.004);
		Minecraft.getMinecraft().getTextureManager().bindTexture(InfoContainer.getColour(infoPos));
		InfoRenderer.renderProgressBar(width, height, info.getEnergyStack().stored, info.getEnergyStack().capacity);
		GlStateManager.enableLighting();
		GL11.glTranslated(0, 0, -0.001);
		GL11.glPopMatrix();
		InfoRenderer.renderNormalInfo(width, height, scale, displayInfo.getFormattedStrings());
		GL11.glPopMatrix();
	}
}
