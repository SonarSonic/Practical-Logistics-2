package sonar.logistics.client.gsi.info;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.client.gsi.AbstractGSI;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.AE2DriveInfo;

public class GSIAE2DriveInfo extends AbstractGSI<AE2DriveInfo> {

	@Override
	public void renderGSIForeground(AE2DriveInfo info, InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		super.renderGSIForeground(info, container, displayInfo, width, height, scale, infoPos);
		GL11.glPushMatrix();
		GL11.glPushMatrix();
		GlStateManager.disableLighting();
		GL11.glTranslated(-1, -+0.0625 * 12, +0.004);
		Minecraft.getMinecraft().getTextureManager().bindTexture(InfoContainer.getColour(infoPos));
		InfoRenderer.renderProgressBar(width, height, info.usedBytes.getObject(), info.totalBytes.getObject());
		GlStateManager.enableLighting();
		GL11.glTranslated(0, 0, -0.001);
		GL11.glPopMatrix();
		List<String> strings = Lists.newArrayList();
		strings.add("Bytes: " + info.usedBytes.getObject() + "/" + info.totalBytes.getObject());
		strings.add("Types: " + info.usedTypes.getObject() + "/" + info.totalTypes.getObject());
		InfoRenderer.renderNormalInfo(width, height, scale, strings);
		GL11.glPopMatrix();
	}

}
