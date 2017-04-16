package sonar.logistics.client;

import static net.minecraft.client.renderer.GlStateManager.alphaFunc;
import static net.minecraft.client.renderer.GlStateManager.color;
import static net.minecraft.client.renderer.GlStateManager.disableAlpha;
import static net.minecraft.client.renderer.GlStateManager.disableLighting;
import static net.minecraft.client.renderer.GlStateManager.disableRescaleNormal;
import static net.minecraft.client.renderer.GlStateManager.enableAlpha;
import static net.minecraft.client.renderer.GlStateManager.enableLighting;
import static net.minecraft.client.renderer.GlStateManager.enableRescaleNormal;
import static net.minecraft.client.renderer.GlStateManager.scale;
import static net.minecraft.client.renderer.GlStateManager.translate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2Constants;

//TODO W.I.P
public class RenderInteractionOverlay {

	public static final ResourceLocation sorting_icons = new ResourceLocation(PL2Constants.MODID + ":textures/gui/interaction_overlay.png");
	public static ItemStack stack;
	public static long stored;
	public static long change;
	public static int ticks;

	public static void setStack(ItemStack stack) {
		if (RenderInteractionOverlay.stack == null || !RenderInteractionOverlay.stack.isItemEqual(stack)) {
			RenderInteractionOverlay.stack = stack.copy();
		}
		ticks = 0;
	}

	public static void change(long change) {
		RenderInteractionOverlay.change += change;
	}

	public static void stored(long stored) {
		RenderInteractionOverlay.stored = stored;
	}

	public static void tick(RenderGameOverlayEvent event) {
		if (!Minecraft.getMinecraft().inGameHasFocus) {
			return;
		}
		if (stack != null) {
			ticks++;

			GlStateManager.pushMatrix();
			GlStateManager.pushAttrib();
			RenderHelper.saveBlendState();
			color(1.0F, 1.0F, 1.0F, 1.0F);
			translate((event.getResolution().getScaledWidth() / 2 )-8, (event.getResolution().getScaledHeight() / 1.4), 0.00);
			scale(2, 2, 2);
			//RenderHelper.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
			//blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			enableRescaleNormal();
			enableAlpha();
			alphaFunc(516, 0.1F);
			//enableBlend();
			enableLighting();
			//GL11.glEnable(GL11.GL_DEPTH_TEST);
			net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
			RenderHelper.itemRender.renderItemIntoGUI(stack, 0, 0);
			RenderHelper.itemRender.renderItemOverlayIntoGUI(RenderHelper.fontRenderer, stack, 0, 0, stored + (change == 0 ? "" : (change > 0 ? "" + TextFormatting.GREEN + "+" + change : "" + TextFormatting.RED + change)) + TextFormatting.RESET);
			RenderHelper.restoreBlendState();
			disableAlpha();
			disableRescaleNormal();
			disableLighting();
			//disableBlend();
			//RenderHelper.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
			
			GlStateManager.popAttrib();
			GlStateManager.popMatrix();

			if (ticks == 1000) {
				stack = null;
				change = 0;
				stored = 0;
				ticks = 0;
			}
		}

	}
}
