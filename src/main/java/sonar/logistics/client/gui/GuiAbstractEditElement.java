package sonar.logistics.client.gui;

import java.io.IOException;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import ic2.core.gui.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextFormatting;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.client.gui.GuiSonar;
import sonar.core.client.gui.widgets.ScrollerOrientation;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.utils.CustomColour;
import sonar.logistics.api.displays.elements.IClickableElement;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.IElementStorageHolder;
import sonar.logistics.api.displays.storage.DisplayElementContainer;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gsi.GSIElementPacketHelper;
import sonar.logistics.client.gui.GuiAbstractEditElement.SpecialFormatButton;
import sonar.logistics.client.gui.GuiAbstractEditElement.TextColourButton;
import sonar.logistics.client.gui.textedit.GuiActions;
import sonar.logistics.client.gui.textedit.GuiStyledStringFunctions;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.helpers.InteractionHelper;

public class GuiAbstractEditElement extends GuiLogistics {

	public DisplayElementContainer c;
	public long lastClickTime;
	public int currentColour = -1;
	public List<TextFormatting> specials = Lists.newArrayList();
	public SonarScroller scaling_scroller;
	public SonarScroller spacing_scroller;

	public double userScaling = 1;
	public double percentageFill;
	public double[] scaling, unscaled;
	public double actualLeft, actualTop, actualElementScale, actualElementWidth, actualElementHeight;

	public GuiAbstractEditElement(DisplayElementContainer c) {
		super(new ContainerMultipartSync((TileSonarMultipart) c.getGSI().getDisplay().getActualDisplay()), c.getGSI().getDisplay());
		// DisplayElementContainer copy = new DisplayElementContainer();
		// NBTTagCompound copyTag = new NBTTagCompound();
		// c.writeData(copyTag, SyncType.SAVE);
		// copy.readData(copyTag, SyncType.SAVE);
		this.c = c;
	}

	@Override
	public void initGui() {
		super.initGui();
		xSize = 256;
		ySize = 256;
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
		// this.buttonList.add(new GuiButton(2, guiLeft + 130 - 3, guiTop + 25, 40, 20, "+0.1 s"));

		scaling_scroller = new SonarScroller(this.guiLeft + 90, this.guiTop + 151, 16, 80);
		scaling_scroller.setOrientation(ScrollerOrientation.HORIZONTAL);

		spacing_scroller = new SonarScroller(this.guiLeft + 90, this.guiTop + 151 + 20, 16, 80);
		spacing_scroller.setOrientation(ScrollerOrientation.HORIZONTAL);
		setScalingScroller((float) c.percentageScale);
		setContainerScaling();
	}

	public void setContainerScaling() {
		percentageFill = 1;
		scaling = new double[] { (xSize) - 8, ((ySize / 2)) - 8, 1 };
		unscaled = new double[] { c.getContainerMaxScaling()[0], c.getContainerMaxScaling()[1], 1 };
		actualLeft = (guiLeft) + 1 * 4;
		actualTop = (guiTop) + 1 * 4;
		actualElementScale = Math.min(scaling[0] / unscaled[0], scaling[1] / unscaled[1]);
		actualElementWidth = (unscaled[0] * actualElementScale) * percentageFill;
		actualElementHeight = (unscaled[1] * actualElementScale) * percentageFill;
		c.updateActualScaling();
		if (this instanceof GuiStyledStringFunctions)
			GuiActions.UPDATE_TEXT_SCALING.trigger((GuiStyledStringFunctions) this);
	}

	public void save() {
		GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createResizeContainerPacket(c.containerIdentity, c.getTranslation(), c.getContainerMaxScaling(), c.percentageScale), -1, c.getGSI());
	}

	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
	}

	public void drawScreen(int x, int y, float var) {
		super.drawScreen(x, y, var);
		scaling_scroller.drawScreen(x, y, true);
		spacing_scroller.drawScreen(x, y, true);
		setScalingScroller(scaling_scroller.currentScroll);
		
		setSpacingScroller(spacing_scroller.currentScroll);
	}

	public void setScalingScroller(float scaling) {
		if (scaling == 0) {
			scaling = 0.01F;
		}
		if (c.percentageScale != scaling || scaling_scroller.currentScroll != scaling) {
			c.percentageScale = scaling;
			scaling_scroller.currentScroll = scaling;
			setContainerScaling();
		}
	}

	public void setSpacingScroller(float scaling) {
		
	}

	public final void changeSelectedColour(TextFormatting colour) {
		int formattingColour = RenderHelper.getTextFormattingColour(colour);
		int r = (int) (formattingColour >> 16 & 255);
		int g = (int) (formattingColour >> 8 & 255);
		int b = (int) (formattingColour & 255);
		currentColour = FontHelper.getIntFromColor(r, g, b);
		onColourChanged(currentColour);
	}

	public final void toggleSpecialFormatting(TextFormatting format) {
		if (specials.contains(format)) {
			specials.remove(format);
			onSpecialFormatChanged(format, false);
		} else {
			specials.add(format);
			onSpecialFormatChanged(format, true);
		}
	}

	public void onColourChanged(int newColour) {

	}

	public void onSpecialFormatChanged(TextFormatting format, boolean enabled) {

	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		FontHelper.text("TEXT SCALING", 10, 154, -1);
		FontHelper.text("TEXT SPACING", 10, 154+20, -1);
	}
	
	@Override
	public void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
		// userScaling = 1;
		// double userMovement = new double[] { 45, 70, 20 };
		GlStateManager.color(1f, 1f, 1f, 1f);
		drawTransparentRect(this.guiLeft, this.guiTop + xSize / 2 + 20, this.guiLeft + this.xSize, this.guiTop + xSize / 2 + 20 + 80, LogisticsColours.layers[1].getRGB());
		drawTransparentRect(this.guiLeft + 1, this.guiTop + 1 + xSize / 2 + 20, this.guiLeft + this.xSize - 1, this.guiTop + xSize / 2 + 20 + 80 - 1, LogisticsColours.layers[2].getRGB());
		renderScroller(scaling_scroller);
		renderScroller(spacing_scroller);
		double pixel = 0.625;
		double[] actualScaling = getActualScaling();
		GlStateManager.pushMatrix();
		DisplayElementHelper.align(getAlignmentTranslation());
		// screen border
		DisplayElementHelper.drawRect(-pixel, -pixel, actualScaling[0] + pixel, actualScaling[1] + pixel, new CustomColour(174, 227, 227).getRGB());
		// screen background
		DisplayElementHelper.drawRect(0, 0, actualScaling[0], actualScaling[1], new CustomColour(40, 40, 40).getRGB());

		GlStateManager.scale(getActualScaling()[2], getActualScaling()[2], 1);
		GlStateManager.translate(0, 0, 1);
		renderContainer(partialTicks, x, y);
		GlStateManager.popMatrix();
	}

	/** all translation scaling applied */
	public void renderContainer(float partialTicks, int x, int y) {
		DisplayElementHelper.renderElementStorageHolder(c);
	}

	public final double[] getAlignmentTranslation() {
		double x = (actualLeft + (scaling[0] - actualElementWidth) / 2) + ((1 - userScaling) * actualElementWidth / 2);
		double y = (actualTop + (scaling[1] - actualElementHeight) / 2) + ((1 - userScaling) * actualElementHeight / 2);
		double z = 0.001;
		return new double[] { x, y, z };
	}

	public final double[] getActualScaling() {
		return new double[] { actualElementWidth * userScaling, actualElementHeight * userScaling, actualElementScale * userScaling };
	}

	public boolean skipContainerClick() {
		return false;
	}

	@Override
	public void mouseClicked(int x, int y, int key) throws IOException {
		Tuple<Boolean, double[]> canClick = canClickContainer(x, y);
		if (skipContainerClick() || !canClick.getFirst() || !doContainerClick(canClick.getSecond()[0], canClick.getSecond()[1], key)) {
			super.mouseClicked(x, y, key);
		}
	}

	public boolean doContainerClick(double clickX, double clickY, int key) {
		Tuple<IDisplayElement, double[]> click = getElementAtXY(clickX, clickY);
		if (click != null) {
			DisplayScreenClick fakeClick = createFakeClick(c.getTranslation()[0] + clickX, c.getTranslation()[1] + clickY, isDoubleClick(), key);
			onDisplayElementClicked(click.getFirst(), fakeClick, click.getSecond());
			return true;
		}
		return false;
	}

	public Tuple<Boolean, double[]> canClickContainer(int x, int y) {
		double startX = getAlignmentTranslation()[0];
		double startY = getAlignmentTranslation()[1];
		double endX = startX + getActualScaling()[0];
		double endY = startY + getActualScaling()[1];
		if (InteractionHelper.checkClick(x, y, new double[] { startX, startY, endX, endY })) {
			double clickX = (x - startX) / getActualScaling()[2];
			double clickY = (y - startY) / getActualScaling()[2];
			return new Tuple(true, new double[] { clickX, clickY });
		}
		return new Tuple(false, new double[2]);
	}

	/** element could be null */
	public final Tuple<IDisplayElement, double[]> getElementAtXY(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> click = null;
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
			Tuple<IDisplayElement, double[]> clicked = h.getClickBoxes(clickX, clickY);
			if (clicked != null) {
				return clicked;
			}
		}
		return new Tuple(null, new double[] { clickX, clickY });
	}

	public final DisplayScreenClick createFakeClick(double clickX, double clickY, boolean doubleClick, int key) {
		DisplayScreenClick fakeClick = new DisplayScreenClick();
		fakeClick.gsi = c.getGSI();
		fakeClick.type = key == 0 ? BlockInteractionType.LEFT : BlockInteractionType.RIGHT;
		fakeClick.clickX = clickX;
		fakeClick.clickY = clickY;
		fakeClick.clickPos = c.getGSI().getDisplay().getActualDisplay().getCoords().getBlockPos();
		fakeClick.identity = c.getGSI().getDisplayGSIIdentity();
		fakeClick.doubleClick = false;
		fakeClick.fakeGuiClick = true;
		return fakeClick;
	}

	public final boolean isDoubleClick() {
		boolean isDoubleClick = false;
		if (mc.getMinecraft().world.getTotalWorldTime() - lastClickTime < 5) {
			isDoubleClick = true;
		}
		lastClickTime = mc.getMinecraft().world.getTotalWorldTime();
		return isDoubleClick;
	}

	public final void onDisplayElementClicked(IDisplayElement e, DisplayScreenClick fakeClick, double[] subClick) {
		if (e instanceof IClickableElement) {
			((IClickableElement) e).onGSIClicked(fakeClick, mc.player, subClick[0], subClick[1]);
		}
	}

	public static class SpecialFormatButton extends LogisticsButton {

		public TextFormatting specialFormat;
		public final GuiAbstractEditElement gui;

		public SpecialFormatButton(GuiAbstractEditElement gui, TextFormatting specialFormat, int id, int x, int y, int texX, int texY, String buttonText, String helpKey) {
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

	public static class TextColourButton extends GuiButton {

		public int formattingColour;
		public int formattingShadow;
		public TextFormatting colour;
		public final GuiAbstractEditElement gui;

		public TextColourButton(GuiAbstractEditElement gui, int id, int x, int y, TextFormatting c) {
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
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				int r = (int) (formattingColour >> 16 & 255);
				int g = (int) (formattingColour >> 8 & 255);
				int b = (int) (formattingColour & 255);
				int rS = (int) (formattingShadow >> 16 & 255);
				int gS = (int) (formattingShadow >> 8 & 255);
				int bS = (int) (formattingShadow & 255);
				int shadowRGB = new CustomColour(rS, gS, bS).getRGB();
				int colourRGB = new CustomColour(r, g, b).getRGB();

				boolean isSelected = gui.currentColour == colourRGB;
				drawRect(this.x, this.y, this.x + 12, this.y + 12, isSelected ? -1 : shadowRGB);
				drawRect(this.x + 1, this.y + 1, this.x + 11, this.y + 11, colourRGB);
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
