package sonar.logistics.client.gsi;

import static net.minecraft.client.renderer.GlStateManager.disableAlpha;
import static net.minecraft.client.renderer.GlStateManager.disableBlend;
import static net.minecraft.client.renderer.GlStateManager.disableLighting;
import static net.minecraft.client.renderer.GlStateManager.disableTexture2D;
import static net.minecraft.client.renderer.GlStateManager.enableAlpha;
import static net.minecraft.client.renderer.GlStateManager.enableBlend;
import static net.minecraft.client.renderer.GlStateManager.enableLighting;
import static net.minecraft.client.renderer.GlStateManager.enableTexture2D;
import static net.minecraft.client.renderer.GlStateManager.popMatrix;
import static net.minecraft.client.renderer.GlStateManager.pushMatrix;
import static net.minecraft.client.renderer.GlStateManager.scale;
import static net.minecraft.client.renderer.GlStateManager.translate;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.displays.DisplayInfo;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.displays.elements.IDisplayRenderable;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.api.tiles.displays.DisplayScreenLook;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.helpers.InteractionHelper;

@SideOnly(Side.CLIENT)
public abstract class AbstractGSI<I extends IInfo> extends Gui implements IGSI<I> {

	public static final ResourceLocation BUTTON_TEX = new ResourceLocation(PL2Constants.MODID + ":textures/gui/filter_buttons.png");
	public InfoContainer container;
	public DisplayInfo renderInfo;
	public EntityPlayer player;
	public World world;
	public List<GSIButton> buttons;
	public Map<Integer, Long> buttonClickCount;
	public List<IDisplayRenderable> renderables;

	public AbstractGSI() {}

	public void initGSI(DisplayInfo renderInfo) {
		this.renderInfo = renderInfo;
		this.container = renderInfo.container;
		this.player = Minecraft.getMinecraft().player;
		this.world = player.getEntityWorld();
		this.buttonClickCount = Maps.newHashMap();
	}

	@Override
	public void resetGSI() {
		List<GSIButton> newButtonList = Lists.newArrayList();
		initButtons(newButtonList);
		buttons = newButtonList;

		List<IDisplayRenderable> newRenderableList = Lists.newArrayList();
		initRenderables(newRenderableList);
		renderables = newRenderableList;
	}

	@Override
	public void renderGSIBackground(I info, InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		bindTexture(BUTTON_TEX);
		pushMatrix();
		disableLighting();
		GL11.glTranslated(-1, -0.0625 * 12, +0.004);

		for (GSIButton button : buttons) {
			pushMatrix();
			int texPixelWidth = 16;
			double pixel = 0.0625;
			RenderHelper.drawModalRectWithCustomSizedTexture(button.posX, button.posY, (double) button.buttonX / ((double) texPixelWidth / (button.width / pixel)), (double) button.buttonY / ((double) texPixelWidth / (button.height / pixel)), button.width, button.height, button.width / pixel, button.width / pixel);
			if (shouldRenderClick(button.buttonID)) {
				/** renders a blue select box around the button if has been clicked */
				RenderHelper.drawModalRectWithCustomSizedTexture(button.posX, button.posY, (double) 15 / ((double) texPixelWidth / (button.width / pixel)), (double) 15 / ((double) texPixelWidth / (button.height / pixel)), button.width, button.height, button.width / pixel, button.width / pixel);
			}
			popMatrix();
		}
		popMatrix();
	}

	private boolean shouldRenderClick(int buttonID) {
		Long click = buttonClickCount.get(buttonID);
		if (click != null) {
			boolean canDelete = (System.currentTimeMillis() - click) > 1000; // holds for one second
			if (canDelete) {
				buttonClickCount.remove(buttonID);
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public void renderGSIForeground(I info, InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		pushMatrix();
		renderables.forEach(IDisplayRenderable::updateRender);
		renderables.forEach(display -> {
			pushMatrix();
			display.render();
			popMatrix();
		});
		popMatrix();
	}

	@Override
	public void renderLookOverlays(I info, InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos, DisplayScreenLook look) {
		GSIButton button = getButtonFromXY(look.lookX, look.lookY);
		if (button != null) {
			double displayScale = 0.016;//FIXME
			pushMatrix();
			translate(-1, -0.0625 * 12, +0.004);
			translate(button.posX + (button.width / 2), button.posY - 0.12, -0.05);
			scale(displayScale * 2, displayScale * 2, displayScale * 2);
			int textWidth = RenderHelper.fontRenderer.getStringWidth(button.hoverString);

			RenderHelper.saveBlendState();

			disableTexture2D();
			enableBlend();
			enableAlpha();
			disableLighting();
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			// GuiSonar.drawRect(left, top, right, bottom, color);
			int left = -textWidth / 2, top = 0, right = textWidth + (-textWidth / 2), bottom = 8, colour = LogisticsColours.blue_overlay.getRGB();
			GuiSonar.drawTransparentRect(left, top, right, bottom, colour * 4);
			translate(0, 0, -0.004);
			FontHelper.textCentre(button.hoverString, 0, 0, -1);

			enableLighting();
			disableAlpha();
			disableBlend();
			enableTexture2D();
			RenderHelper.restoreBlendState();
			popMatrix();
		}
	}

	@Override
	public boolean canInteractWith(I info, DisplayScreenClick click, EnumHand hand) {
		return InteractionHelper.canBeClickedStandard(renderInfo, click);
	}

	@Override
	public void onGSIClicked(I info, DisplayScreenClick click, EnumHand hand) {
		GSIButton button = getButtonFromXY(click.clickX, click.clickY);
		if (button != null) {
			onButtonClicked(info, button, click, hand);
			buttonClickCount.put(button.buttonID, System.currentTimeMillis());
		}
	}

	@Nullable
	public GSIButton getButtonFromXY(double x, double y) {
		double[] click = InteractionHelper.getActualBox(x, y, renderInfo);
		for (GSIButton button : buttons) {
			if (button.isClickOver(click[6], click[7])) {
				return button;
			}
		}
		return null;
	}

	public void initButtons(List<GSIButton> buttons) {}

	public void initRenderables(List<IDisplayRenderable> renderables) {}

	public void bindTexture(ResourceLocation resource) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(resource);
	}
}
