package sonar.logistics.client.gsi.info;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.client.gsi.AbstractGSI;
import sonar.logistics.client.gsi.GSIHelper;
import sonar.logistics.info.types.MonitoredItemStack;

public class GSIItemStack extends AbstractGSI<MonitoredItemStack> {
	
	@Override
	public void renderGSIForeground(MonitoredItemStack info, InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		if (info.itemStack.getObject() != null) {
			//DisplayType type = container.display.getDisplayType();
			StoredItemStack stack = info.itemStack.getObject();
			ItemStack item = stack.item;
			GlStateManager.pushAttrib();
			GL11.glPushMatrix();
			GlStateManager.enableDepth();
			GL11.glTranslated(-(1 - width / 2 - 0.0625), -0.68 + height / 2, 0.00);
			GL11.glRotated(180, 0, 1, 0);
			GL11.glScaled(-1, 1, 1);			
			double actualScale = scale * 2;			
			GL11.glScaled(actualScale, actualScale, 0.01);			
			GL11.glTranslated(-8, -8, 0);
			GlStateManager.disableLighting();
			GlStateManager.enablePolygonOffset();
			GlStateManager.doPolygonOffset(-1, -1);
			GlStateManager.enableCull();
			RenderHelper.renderItemIntoGUI(item, 0, 0);
			GlStateManager.disablePolygonOffset();
			GlStateManager.translate(0, 0, 2);
			GlStateManager.depthMask(false);
			RenderHelper.renderStoredItemStackOverlay(item, 0, 0, 0, "" + stack.stored, false);
			GlStateManager.depthMask(true);			
			GlStateManager.disableDepth();
			GL11.glPopMatrix();
			GlStateManager.popAttrib();
		}
	}

	@Override
	public void onGSIClicked(MonitoredItemStack info, DisplayScreenClick click, EnumHand hand) {
		super.onGSIClicked(info, click, hand);
		NBTTagCompound packet = GSIHelper.createItemClickPacket(info.getStoredStack(), info.getNetworkSource());
		sendGSIPacket(packet, info, click, hand);
	}
	
}
