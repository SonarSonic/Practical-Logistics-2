package sonar.logistics.client.gui;

import static net.minecraft.client.renderer.GlStateManager.popMatrix;
import static net.minecraft.client.renderer.GlStateManager.pushMatrix;
import static net.minecraft.client.renderer.GlStateManager.scale;

import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.client.gui.GuiHelpOverlay;
import sonar.core.client.gui.GuiSonar;
import sonar.core.client.gui.SonarTextField;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.utils.CustomColour;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.IDisplayElement;
import sonar.logistics.api.displays.ITextElement;
import sonar.logistics.api.displays.elements.DisplayElementContainer;
import sonar.logistics.api.displays.elements.HeightAlignment;
import sonar.logistics.api.displays.elements.IClickableElement;
import sonar.logistics.api.displays.elements.IElementStorageHolder;
import sonar.logistics.api.displays.elements.WidthAlignment;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.GuiAbstractEditElement.SpecialFormatButton;
import sonar.logistics.client.gui.GuiAbstractEditElement.TextColourButton;
import sonar.logistics.client.gui.textedit.HotKeyFunctions;
import sonar.logistics.client.gui.textedit.TextSelection;
import sonar.logistics.client.gui.textedit.TextSelectionType;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.helpers.InteractionHelper;

public class GuiEditTextElement extends GuiAbstractEditElement {

	public double[] textDragStart;
	public double[] textDragEnd;
	public boolean isDragging = false;
	public long lastCursorClick = 0;
	public ITextElement cursorElement;

	public GuiEditTextElement(DisplayElementContainer c) {
		super(c);
	}

	@Override
	public void initGui() {
		super.initGui();
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

	public void onColourChanged(int newColour) {
		super.onColourChanged(newColour);
		setTextColourOnSelected(newColour);
	}

	public void onSpecialFormatChanged(TextFormatting format, boolean enabled) {
		super.onSpecialFormatChanged(format, enabled);
		if (enabled) {
			enableSpecialFormattingOnSelected(Lists.newArrayList(format));
		} else {
			disableSpecialFormattingOnSelected(Lists.newArrayList(format));
		}
	}

	@Override
	public boolean doContainerClick(double clickX, double clickY, int key) {
		if (key == 0) {

			Tuple<IDisplayElement, double[]> click = getElementAtXY(clickX, clickY);
			if (click.getFirst() instanceof ITextElement) {
				ITextElement e = (ITextElement) click.getFirst();
				if (isDoubleClick()) {
					deselectAllText();
					setSelectedRange(e, new double[1], new double[1], TextSelectionType.SELECT_ALL);
				} else {
					// set cursor
					removeCursor();
					double[] worldRenderOffset = c.getAlignmentTranslation();
					double[] alignArray = c.getFullAlignmentTranslation(e);
					double xStartPercent = DisplayElementHelper.percentageFromScale(new double[] { clickX - (alignArray[0] - worldRenderOffset[0]) }, e.getActualScaling())[0];
					String formattedString = e.getStyledStringCompound().getCachedFormattedString();
					int indexS = FontHelper.getIndexFromPixel(formattedString, (int) Math.floor(InfoRenderer.getStringWidth(formattedString) * xStartPercent / 100));
					if (indexS != -1) {
						//e.getStyledStringCompound().setCursor(indexS);
						cursorElement = e;
						lastCursorClick = mc.getSystemTime();
					}
				}
			}
			textDragStart = new double[] { clickX, clickY };
			textDragEnd = null;
			return true;
		}
		return false;
	}

	@Override
	public void renderContainer(float partialTicks, int mouseX, int mouseY) {
		super.renderContainer(partialTicks, mouseX, mouseY);
		/* if (hasTextDrag(mouseX, mouseY)) { double[] start = getTextDragStart(mouseX, mouseY); double[] end = getTextDragEnd(mouseX, mouseY); GlStateManager.translate(0, 0, -0.01); double clickStartX = Math.min(start[0], end[0]); double clickStartY = Math.min(start[1], end[1]); double clickEndX = Math.max(start[0], end[0]); double clickEndY = Math.max(start[1], end[1]); // DisplayElementHelper.drawRect(clickStartX, clickStartY, clickEndX, clickEndY, new CustomColour(49, 145, 88).getRGB()); } */
	}

	public void doFormatting(Consumer<ITextElement> func) {

		for (IDisplayElement e : c.getElements()) {
			if ((e instanceof IElementStorageHolder)) {
				IElementStorageHolder holder = (IElementStorageHolder) e;
				holder.getElements().forEach(se -> {
					if (se instanceof ITextElement) {
						func.accept((ITextElement) se);
					}
				});
			} else if (e instanceof ITextElement) {
				func.accept((ITextElement) e);
			}
		}
	}

	public void removeCursor() {
		if (cursorElement != null) {
			//cursorElement.getStyledStringCompound().setCursor(-1);
			cursorElement = null;
		}
	}

	public void deselectAllText() {
		doFormatting(e -> setSelectedRange(e, new double[1], new double[1], TextSelectionType.DESELECT_ALL));
	}

	public void selectAllText() {
		doFormatting(e -> setSelectedRange(e, new double[1], new double[1], TextSelectionType.SELECT_ALL));
	}

	public void deleteAllSelected() {
		doFormatting(e -> e.getStyledStringCompound().forSelections(ss -> null));
	}

	public void updateSelectedText(int mouseX, int mouseY) {
		double[] start = getTextDragStart(mouseX, mouseY);
		double[] end = getTextDragEnd(mouseX, mouseY);
		doFormatting(e -> setSelectedText(e, start, end));
	}

	public void enableSpecialFormattingOnSelected(List<TextFormatting> formatting) {
		doFormatting(e -> e.getStyledStringCompound().enableSpecialFormatting(formatting));
	}

	public void disableSpecialFormattingOnSelected(List<TextFormatting> formatting) {
		doFormatting(e -> e.getStyledStringCompound().disableSpecialFormatting(formatting));
	}

	public void setTextColourOnSelected(int colour) {
		doFormatting(e -> e.getStyledStringCompound().setFontColour(colour));
	}

	public void setSelectedText(ITextElement e, double[] start, double[] end) {
		double[] guiAlign = getAlignmentTranslation();
		double[] worldRenderOffset = c.getAlignmentTranslation();
		double[] alignArray = c.getFullAlignmentTranslation(e);
		double startXC = alignArray[0] - worldRenderOffset[0];
		double startYC = alignArray[1] - worldRenderOffset[1];
		double endXC = alignArray[0] - worldRenderOffset[0] + e.getActualScaling()[0];
		double endYC = alignArray[1] - worldRenderOffset[1] + e.getActualScaling()[1];
		double[] eBox = new double[] { startXC, startYC, endXC, endYC };
		double[] clickBox = new double[] { Math.min(start[0], end[0]), Math.min(start[1], end[1]), Math.max(start[0], end[0]), Math.max(start[1], end[1]) };

		if (InteractionHelper.checkOverlap(eBox, clickBox)) {
			double subStartX = Math.max(0, clickBox[0] - startXC);
			double subStartY = Math.max(0, clickBox[1] - startYC);
			double subEndX = Math.min(e.getActualScaling()[0], clickBox[2] - startXC);
			double subEndY = Math.min(e.getActualScaling()[1], clickBox[3] - startYC);
			TextSelectionType type = (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) ? TextSelectionType.COMBINE : TextSelectionType.SET_SELECTION;
			setSelectedRange(e, new double[] { subStartX, subStartY }, new double[] { subEndX, subEndY }, type);
		} else {
			setSelectedRange(e, new double[2], new double[2], TextSelectionType.DESELECT_ALL);
		}
	}

	public void setSelectedRange(ITextElement e, double[] start, double[] end, TextSelectionType type) {
		if (type.requiresPosition()) {
			double xStartPercent = DisplayElementHelper.percentageFromScale(start, e.getActualScaling())[0];
			double xEndPercent = DisplayElementHelper.percentageFromScale(end, e.getActualScaling())[0];
			int indexS = FontHelper.getIndexFromPixel(e.getStyledStringCompound().getCachedFormattedString(), (int) Math.floor(e.getUnscaledWidthHeight()[0] * xStartPercent / 100));
			int indexE = FontHelper.getIndexFromPixel(e.getStyledStringCompound().getCachedFormattedString(), (int) Math.ceil(e.getUnscaledWidthHeight()[0] * xEndPercent / 100));
			e.getStyledStringCompound().setSelectedRange(indexS, indexE, type);
		} else {
			e.getStyledStringCompound().setSelectedRange(-1, -1, type);
		}
	}

	public void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
		boolean setTextDrag = true;

		if (isDragging) {
			boolean canContinue = Mouse.isButtonDown(0);
			if (!canContinue) {
				isDragging = false;
				textDragEnd = findClosestValidTextDrag(x, y);
			} else {
				updateSelectedText(x, y);
			}
		} else if (lastCursorClick + 200 < mc.getSystemTime()) {
			isDragging = Mouse.isButtonDown(0);
			lastCursorClick = mc.getSystemTime();
		}
		super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
	}

	public void finishTextDrag(int mouseX, int mouseY) {
		isDragging = false;
		textDragEnd = findClosestValidTextDrag(mouseX, mouseX);
		// FIXME - IF THEY ARE THE SAME ADD CURSOR!
	}

	public boolean hasTextDrag(int mouseX, int mouseY) {
		return textDragStart != null;
	}

	public double[] getTextDragStart(int mouseX, int mouseY) {
		return textDragStart;
	}

	public double[] getTextDragEnd(int mouseX, int mouseY) {
		if (textDragEnd == null) {
			return findClosestValidTextDrag(mouseX, mouseY);
		}
		return textDragEnd;
	}

	public double[] findClosestValidTextDrag(int mouseX, int mouseY) {
		double startX = getAlignmentTranslation()[0];
		double startY = getAlignmentTranslation()[1];
		double endX = startX + getActualScaling()[0];
		double endY = startY + getActualScaling()[1];

		double validX = mouseX <= startX ? startX : mouseX >= endX ? endX : mouseX;
		double validY = mouseY <= startY ? startY : mouseY >= endY ? endY : mouseY;

		double dragX = (validX - startX) / getActualScaling()[2];
		double dragY = (validY - startY) / getActualScaling()[2];
		return new double[] { dragX, dragY };
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException {
		boolean control = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
		if (GuiScreen.isKeyComboCtrlA(i)) {
			selectAllText();
			return;
		}
		if (isCloseKey(i) || c == 4) {
			deselectAllText();
		}
		if (cursorElement != null) {
			if (ChatAllowedCharacters.isAllowedCharacter(c)) {
				deselectAllText();
				//cursorElement.getStyledStringCompound().addText(Character.toString(c));
				return;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_BACK)) {
				deselectAllText();
				// deleteAllSelected();
				//cursorElement.getStyledStringCompound().removeText(1);
			}
		}else{
		}
		super.keyTyped(c, i);
	}

	/* public static String getClipboardString() { try { Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents((Object) null); if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) { return (String) transferable.getTransferData(DataFlavor.plainTextFlavor); } } catch (Exception var1) { ; } return ""; } public static void setClipboardString(String copyText) { if (!StringUtils.isEmpty(copyText)) { try { StringSelection stringselection = new StringSelection(copyText); Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, (ClipboardOwner) null); } catch (Exception var2) { ; } } } */
}
