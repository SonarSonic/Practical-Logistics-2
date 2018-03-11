package sonar.logistics.client.gui.display;

import static net.minecraft.client.renderer.GlStateManager.popMatrix;
import static net.minecraft.client.renderer.GlStateManager.pushMatrix;
import static net.minecraft.client.renderer.GlStateManager.scale;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.client.gui.GuiHelpOverlay;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.utils.CustomColour;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.HeightAlignment;
import sonar.logistics.api.displays.WidthAlignment;
import sonar.logistics.api.displays.elements.IClickableElement;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.IElementStorageHolder;
import sonar.logistics.api.displays.storage.DisplayElementContainer;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.GuiLogistics;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.helpers.InteractionHelper;

public class GuiEditDisplayElement extends GuiLogistics {

	public DisplayElementContainer c;
	public IDisplayElement e;
	public long lastClickTime;
	public TextFormatting currentColour = null;
	public List<TextFormatting> specialFormats = Lists.newArrayList();

	double userScaling = 1;
	double percentageFill;
	double[] scaling, unscaled;
	double actualLeft, actualTop, actualElementScale, actualElementWidth, actualElementHeight;

	public GuiEditDisplayElement(DisplayElementContainer c, IDisplayElement e) {
		super(new ContainerMultipartSync((TileSonarMultipart) c.getGSI().getDisplay().getActualDisplay()), c.getGSI().getDisplay());
		this.c = c;
		this.e = e;
	}

	@Override
	public void initGui() {
		super.initGui();
		xSize = 256;
		ySize = 256;
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		this.buttonList.add(new LogisticsButton(this, 0, guiLeft + 198, guiTop + 150, 11 * 16, 0, "Align Left", "Aligns the element to the left"));
		this.buttonList.add(new LogisticsButton(this, 1, guiLeft + 198 + 20, guiTop + 150, 11 * 16, 16, "Align Centre", "Aligns the element to the centre"));
		this.buttonList.add(new LogisticsButton(this, 2, guiLeft + 198 + 40, guiTop + 150, 11 * 16, 32, "Align Right", "Aligns the element to the right"));
		this.buttonList.add(new SpecialFormatButton(this, TextFormatting.BOLD, 3, guiLeft + 198, guiTop + 150 + 20, 11 * 16, 48, "Bold", "Make the selected text bold"));
		this.buttonList.add(new SpecialFormatButton(this, TextFormatting.ITALIC, 4, guiLeft + 198 + 20, guiTop + 150 + 20, 11 * 16, 64, "Italic", "Italicize the selected text"));
		this.buttonList.add(new SpecialFormatButton(this, TextFormatting.UNDERLINE, 5, guiLeft + 198 + 40, guiTop + 150 + 20, 11 * 16, 80, "Underline", "Underline the selected text"));
		this.buttonList.add(new SpecialFormatButton(this, TextFormatting.STRIKETHROUGH, 6, guiLeft + 198, guiTop + 150 + 40, 11 * 16, 96, "Strikethrough", "Draw a line through the middle of the selected text"));
		this.buttonList.add(new SpecialFormatButton(this, TextFormatting.OBFUSCATED, 7, guiLeft + 198 + 20, guiTop + 150 + 40, 2 * 16, 16 * 5, "Obfuscate", "Obfuscates the selected text"));
		this.buttonList.add(new LogisticsButton(this, 8, guiLeft + 198 + 40, guiTop + 150 + 40, 11 * 16, 112, "Font Colour", "Change the colour of the selected text"));

		for (int i = 0; i < 16; i++) {
			TextFormatting format = TextFormatting.values()[i];
			this.buttonList.add(new TextColourButton(this, 16 + i, guiLeft + 2 + i * 16, guiTop + 210, format));
		}

		// this.buttonList.add(new GuiButton(2, guiLeft + 130 - 3, guiTop + 25, 40, 20, "+0.1 s"));

		percentageFill = 1;
		scaling = new double[] { (xSize) - 8, ((ySize / 2)) - 8, 1 };
		unscaled = new double[] { c.getContainerMaxScaling()[0], c.getContainerMaxScaling()[1], 1 };
		actualLeft = (guiLeft) + 1 * 4;
		actualTop = (guiTop) + 1 * 4;
		actualElementScale = Math.min(scaling[0] / unscaled[0], scaling[1] / unscaled[1]);
		actualElementWidth = (unscaled[0] * actualElementScale) * percentageFill;
		actualElementHeight = (unscaled[1] * actualElementScale) * percentageFill;
	}

	public void changeSelectedColour(TextFormatting colour) {
		currentColour = colour;
	}

	public void toggleSpecialFormatting(TextFormatting format) {
		if (specialFormats.contains(format)) {
			specialFormats.remove(format);
		} else {
			specialFormats.add(format);
		}
	}

	public void selectMouse() {

	}

	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button instanceof TextColourButton) {
			changeSelectedColour(((TextColourButton) button).colour);
		}
		if (button instanceof SpecialFormatButton) {
			toggleSpecialFormatting(((SpecialFormatButton) button).specialFormat);
		}

	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);

	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		// userScaling = 1;
		// double userMovement = new double[] { 45, 70, 20 };
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTransparentRect(this.guiLeft, this.guiTop + xSize / 2 + 20, this.guiLeft + this.xSize, this.guiTop + xSize / 2 + 20 + 80, LogisticsColours.layers[1].getRGB());
		drawTransparentRect(this.guiLeft + 1, this.guiTop + 1 + xSize / 2 + 20, this.guiLeft + this.xSize - 1, this.guiTop + xSize / 2 + 20 + 80 - 1, LogisticsColours.layers[2].getRGB());
		renderScroller(scroller);
		double pixel = 0.625;
		double[] actualScaling = getActualScaling();
		GlStateManager.pushMatrix();
		DisplayElementHelper.align(getAlignmentTranslation());
		DisplayElementHelper.drawRect(-pixel, -pixel, actualScaling[0] + pixel, actualScaling[1] + pixel, new CustomColour(174, 227, 227).getRGB());
		DisplayElementHelper.drawRect(0, 0, actualScaling[0], actualScaling[1], new CustomColour(40, 40, 40).getRGB());
		GlStateManager.scale(getActualScaling()[2], getActualScaling()[2], 0.01);
		GlStateManager.translate(0, 0, 1);
		DisplayElementHelper.renderElementStorageHolder(c);
		// GlStateManager.enableDepth();
		GlStateManager.popMatrix();
	}

	public double[] getAlignmentTranslation() {
		double x = (actualLeft + (scaling[0] - actualElementWidth) / 2) + ((1 - userScaling) * actualElementWidth / 2);
		double y = (actualTop + (scaling[1] - actualElementHeight) / 2) + ((1 - userScaling) * actualElementHeight / 2);
		double z = 0.001;
		return new double[] { x, y, z };
	}

	public double[] getActualScaling() {
		return new double[] { actualElementWidth * userScaling, actualElementHeight * userScaling, actualElementScale * userScaling };
	}

	@Override
	public void mouseClicked(int x, int y, int key) throws IOException {
		Tuple<IDisplayElement, double[]> click = getElementAtXY(x, y);
		if (click != null) {
			boolean isDoubleClick = isDoubleClick();
			double clickX = (x - getAlignmentTranslation()[0]) / getActualScaling()[2];
			double clickY = (y - getAlignmentTranslation()[1]) / getActualScaling()[2];
			DisplayScreenClick fakeClick = createFakeClick(c.getTranslation()[0] + clickX, c.getTranslation()[1] + clickY, isDoubleClick, key);
			onDisplayElementClicked(click.getFirst(), fakeClick, click.getSecond());
			return;
		}
		super.mouseClicked(x, y, key);
	}

	public Tuple<IDisplayElement, double[]> getElementAtXY(int x, int y) {
		double startX = getAlignmentTranslation()[0];
		double startY = getAlignmentTranslation()[1];
		double endX = startX + getActualScaling()[0];
		double endY = startY + getActualScaling()[1];
		if (InteractionHelper.checkClick(x, y, new double[] { startX, startY, endX, endY })) {
			Tuple<IDisplayElement, double[]> click = null;
			double clickX = (x - startX) / getActualScaling()[2];
			double clickY = (y - startY) / getActualScaling()[2];
			for (IDisplayElement e : c.elements) {
				if (!(e instanceof IElementStorageHolder)) {
					double[] alignArray = c.getAlignmentTranslation(e);
					double startXC = alignArray[0];
					double startYC = alignArray[1];
					double endXC = alignArray[0] + e.getActualScaling()[0];
					double endYC = alignArray[1] + e.getActualScaling()[1];
					double[] eBox = new double[] { startXC, startYC, endXC, endYC };
					if (InteractionHelper.checkClick(clickX, clickY, eBox)) {
						double subClickX = clickX - startXC;
						double subClickY = clickY - startYC;
						return new Tuple(e, new double[] { subClickX, subClickY });
					}
				}
			}
			for (IElementStorageHolder h : c.elements.getSubHolders()) {
				Tuple<IDisplayElement, double[]> clicked = h.getClickBoxes(x, y);
				if (clicked != null) {
					return clicked;
				}
			}
		}
		return null;
	}

	public DisplayScreenClick createFakeClick(double clickX, double clickY, boolean doubleClick, int key) {
		DisplayScreenClick fakeClick = new DisplayScreenClick();
		fakeClick.gsi = c.getGSI();
		fakeClick.type = key == 0 ? BlockInteractionType.LEFT : BlockInteractionType.RIGHT;
		fakeClick.clickX = clickX;
		fakeClick.clickY = clickY;
		fakeClick.clickPos = c.getGSI().getDisplay().getActualDisplay().getCoords().getBlockPos();
		fakeClick.identity = c.getGSI().getDisplayGSIIdentity();
		fakeClick.doubleClick = false;
		return fakeClick;
	}

	public boolean isDoubleClick() {
		boolean isDoubleClick = false;
		if (mc.getMinecraft().world.getTotalWorldTime() - lastClickTime < 10) {
			isDoubleClick = true;
		}
		lastClickTime = mc.getMinecraft().world.getTotalWorldTime();
		return isDoubleClick;
	}

	public void onDisplayElementClicked(IDisplayElement e, DisplayScreenClick fakeClick, double[] subClick) {
		if (e instanceof IClickableElement) {
			((IClickableElement) e).onGSIClicked(fakeClick, mc.player, subClick[0], subClick[1]);
		}
	}

	public static class SpecialFormatButton extends LogisticsButton {

		public TextFormatting specialFormat;

		public SpecialFormatButton(GuiSonar sonar, TextFormatting specialFormat, int id, int x, int y, int texX, int texY, String buttonText, String helpKey) {
			super(sonar, id, x, y, texX, texY, buttonText, helpKey);
			this.specialFormat = specialFormat;
		}

	}

	public static class TextColourButton extends GuiButton {

		public int formattingColour;
		public int formattingShadow;
		public TextFormatting colour;
		public final GuiEditDisplayElement gui;

		public TextColourButton(GuiEditDisplayElement gui, int id, int x, int y, TextFormatting c) {
			super(id, x, y, 12, 12, c.getFriendlyName());
			this.gui = gui;
			formattingColour = RenderHelper.getTextFormattingColour(c);
			formattingShadow = RenderHelper.getTextFormattingShadow(c);
			colour = c;
		}

		@Override
		public void drawButton(Minecraft mc, int x, int y, float partialTicks) {
			// super.drawButton(mc, x, y, partialTicks);
			if (this.visible) {
				this.hovered = x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
				boolean isSelected = gui.currentColour != null && gui.currentColour == colour;
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				int r = (int) (formattingColour >> 16 & 255);
				int g = (int) (formattingColour >> 8 & 255);
				int b = (int) (formattingColour & 255);
				int rS = (int) (formattingShadow >> 16 & 255);
				int gS = (int) (formattingShadow >> 8 & 255);
				int bS = (int) (formattingShadow & 255);
				drawRect(this.x, this.y, this.x + 12, this.y + 12, isSelected ? -1 : new CustomColour(rS, gS, bS).getRGB());
				drawRect(this.x + 1, this.y + 1, this.x + 11, this.y + 11, new CustomColour(r, g, b).getRGB());
			}

		}

		@Override
		public void drawButtonForegroundLayer(int x, int y) {
			if (hovered) {
				gui.drawSonarCreativeTabHoveringText(this.displayString, x, y);
			}
		}
	}

	@Override
	public ResourceLocation getBackground() {
		return null;
	}

}
