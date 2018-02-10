package sonar.logistics.client;

import java.util.List;
import java.util.Optional;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import mcmultipart.MCMultiPart;
import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.event.DrawMultipartHighlightEvent;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.logistics.api.operator.IOperatorProvider;
import sonar.logistics.api.operator.IOperatorTool;

public class RenderOperatorOverlay {

	public static boolean isUsing, gotFirstPacket;
	public static BlockPos lastPos;

	public static void tick(DrawBlockHighlightEvent evt) {
		if (!Minecraft.getMinecraft().inGameHasFocus || !isUsing) {
			return;
		}
		ItemStack stack = evt.getPlayer().getHeldItemMainhand();
		if (stack == null || !(stack.getItem() instanceof IOperatorTool)) {
			return;
		}
		BlockPos pos = evt.getTarget().getBlockPos();
		if (pos == null) {
			return;
		}
		boolean requestPacket = !pos.equals(lastPos);
		lastPos = pos;
		TileEntity tile = evt.getPlayer().getEntityWorld().getTileEntity(pos);
		if (tile == null) {
			return;
		}
		IOperatorProvider provider = null;
		if (evt instanceof DrawMultipartHighlightEvent) {
			DrawMultipartHighlightEvent multipart = (DrawMultipartHighlightEvent) evt;
			IMultipartTile multipartTile = multipart.getPartInfo().getTile();
			if (multipartTile != null && multipartTile instanceof IOperatorProvider) {
				provider = (IOperatorProvider) multipartTile;
			}
		} else {
			if (tile instanceof IOperatorProvider) {
				provider = (IOperatorProvider) tile;
			} else if (tile instanceof IMultipartContainer && evt.getTarget().subHit != -1) {
				IMultipartContainer container = (IMultipartContainer) tile;
				IPartSlot slot = MCMultiPart.slotRegistry.getValue(evt.getTarget().subHit);
				if (slot != null) {
					Optional<IMultipartTile> multipartTile = container.getPartTile(slot);
					if (multipartTile.isPresent() && multipartTile.get() instanceof IOperatorProvider) {
						provider = (IOperatorProvider) multipartTile.get();
					}
				}
			}
		}

		if (provider != null) {
			GlStateManager.pushAttrib();
			net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
			GlStateManager.disableLighting();
			RenderHelper.offsetRendering(pos, evt.getPartialTicks());
			Entity view = Minecraft.getMinecraft().getRenderViewEntity();
			if (requestPacket)
				provider.updateOperatorInfo();
			List<String> infoList = Lists.newArrayList();
			provider.addInfo(infoList);
			if (infoList.isEmpty()) {
				return;
			}
			int maxWidth = 60;
			int maxHeight = infoList.size() * 12;
			for (int i = 0; i < infoList.size(); i++) {
				int length = (int) ((RenderHelper.fontRenderer.getStringWidth(infoList.get(i)) + 4) * 0.8);
				if (length > maxWidth) {
					maxWidth = length;
				}
			}
			GlStateManager.translate(0.5, 1, 0.5);
			GlStateManager.rotate(-view.rotationYaw - 180, 0, 1, 0);
			GlStateManager.rotate(-view.rotationPitch, 1, 0, 0);
			RenderHelper.saveBlendState();
			GlStateManager.disableDepth();
			GlStateManager.depthMask(true);
			GlStateManager.scale(0.016, 0.016, 1);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			GuiSonar.drawTransparentRect(-maxWidth / 2, -maxHeight / 2, maxWidth / 2, maxHeight / 2, LogisticsColours.layers[1].getRGB());
			GuiSonar.drawTransparentRect(-maxWidth / 2 + 1, -maxHeight / 2 + 1, maxWidth / 2 - 1, maxHeight / 2 - 1, LogisticsColours.layers[2].getRGB());
			GuiSonar.drawTransparentRect(-maxWidth / 2 + 1, -maxHeight / 2 + 1, maxWidth / 2 - 1, maxHeight / 2 - 1, LogisticsColours.layers[2].getRGB());
			GuiSonar.drawTransparentRect(-maxWidth / 2 + 1, -maxHeight / 2 + 1, maxWidth / 2 - 1, maxHeight / 2 - 1, LogisticsColours.layers[2].getRGB());

			GlStateManager.scale(0.8, -0.8, 0.8);
			// FontHelper.textCentre(infoList.get(0), 0, -maxHeight/2 + 4, -1);
			double yCentre = 0;
			double centre = ((double) (infoList.size()) / 2) - yCentre;
			float offset = 12F;

			for (int i = 0; i < infoList.size(); i++) {
				String info = infoList.get(i);
				FontHelper.textCentre(info, 0, (int) (i == centre ? yCentre : i < centre ? yCentre - offset * -(i - centre) : yCentre + offset * (i - centre)), -1);
			}

			RenderHelper.restoreBlendState();
			GlStateManager.enableDepth();

			GlStateManager.popMatrix();
			GlStateManager.popAttrib();
		}

	}
	// }
}
