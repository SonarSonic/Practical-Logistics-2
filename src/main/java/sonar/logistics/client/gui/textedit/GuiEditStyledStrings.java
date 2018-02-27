package sonar.logistics.client.gui.textedit;

import static net.minecraft.client.renderer.GlStateManager.popMatrix;
import static net.minecraft.client.renderer.GlStateManager.pushMatrix;
import static net.minecraft.client.renderer.GlStateManager.scale;

import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

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
import net.minecraft.inventory.Container;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.client.gui.GuiHelpOverlay;
import sonar.core.client.gui.GuiSonar;
import sonar.core.client.gui.GuiSonarTile;
import sonar.core.client.gui.SonarTextField;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.helpers.SonarHelper;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.core.inventory.ContainerEmpty;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.inventory.ContainerSonar;
import sonar.core.inventory.ContainerSync;
import sonar.core.utils.CustomColour;
import sonar.core.utils.IWorldPosition;
import sonar.core.utils.SortingDirection;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.IDisplayElement;
import sonar.logistics.api.displays.ITextElement;
import sonar.logistics.api.displays.elements.DisplayElementContainer;
import sonar.logistics.api.displays.elements.HeightAlignment;
import sonar.logistics.api.displays.elements.IClickableElement;
import sonar.logistics.api.displays.elements.IElementStorageHolder;
import sonar.logistics.api.displays.elements.StyledTextElement;
import sonar.logistics.api.displays.elements.WidthAlignment;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.GuiAbstractEditElement;
import sonar.logistics.client.gui.GuiAbstractEditElement.SpecialFormatButton;
import sonar.logistics.client.gui.GuiAbstractEditElement.TextColourButton;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.helpers.InteractionHelper;

public class GuiEditStyledStrings extends GuiAbstractEditElement implements ILineCounter {

	public StyledTextElement text;
	public List<TextSelection> savedSelections = Lists.newArrayList();
	// public TextSelection currentSelection = null;
	// public int cursorX = -1, cursorY = -1;
	// public int selectX = -1, selectY = -1;
	public CursorPosition cursorPosition = CursorPosition.newInvalid();
	public CursorPosition selectPosition = CursorPosition.newInvalid();

	public long lastCursorClick = 0;
	public boolean isDragging = false;

	public GuiEditStyledStrings(StyledTextElement text, DisplayElementContainer c) {
		super(c);
		this.text = text;
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

	//// RENDER \\\\

	public void renderContainer(float partialTicks, int x, int y) {
		text.getHolder().startElementRender(text);
		pushMatrix();
		DisplayElementHelper.align(text.getHolder().getAlignmentTranslation(text));
		double scale = text.getActualScaling()[2];
		scale(scale, scale, scale);

		int i = 0;
		List<TextSelection> allSelection = getAllSelections();
		for (StyledStringLine s : text) {
			text.preRender(s);
			if (cursorPosition.validPosition() && i == cursorPosition.y) {
				renderCursor(s);
			}
			s.render();
			renderSelections(allSelection, s, i);
			text.postRender(s);
			i++;
		}
		popMatrix();
		text.getHolder().endElementRender(text);
	}

	public void renderCursor(StyledStringLine text) {
		if (cursorPosition.validPosition()) {
			// FIXME - RENDER CURSOR - DOESN'T CHECK FORMATTED STRINGS!

			String currentString = text.getCachedUnformattedString();
			int cursorPos = cursorPosition.x == 0 ? 0 : RenderHelper.fontRenderer.getStringWidth(currentString.substring(0, Math.min(cursorPosition.x, currentString.length())));
			GlStateManager.translate(0, 0, 0.1);
			GlStateManager.disableTexture2D();
			GlStateManager.enableBlend();
			GlStateManager.disableTexture2D();
			GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
			RenderHelper.drawRect((cursorPos), 0, (cursorPos + 1), 9);
			GlStateManager.disableBlend();
			GlStateManager.enableTexture2D();
			GlStateManager.translate(0, 0, -0.1);
		}
	}

	public void renderSelections(List<TextSelection> toRender, StyledStringLine line, int i) {
		if (toRender != null && !toRender.isEmpty()) {
			String string = line.getCachedUnformattedString();
			GlStateManager.translate(0, 0, 0.1);
			GlStateManager.disableTexture2D();
			GlStateManager.enableBlend();
			GlStateManager.disableTexture2D();
			GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
			GlStateManager.color(1F, 1F, 1F, 0.5F);
			for (TextSelection s : toRender) {
				int[] subSelect = s.getSubStringSize(string, i);
				if (subSelect[0] != -1 && subSelect[1] != -1) {
					int start = subSelect[0];
					int end = subSelect[1];
					int subIndex = 0;
					int subWidth = 0;
					for (StyledString ss : line.strings) {
						int selectStart = Math.max(subIndex, start);
						int selectEnd = Math.min(subIndex + ss.getStringLength(), end);
						int subStart = selectStart - subIndex;
						int subEnd = selectEnd - subIndex;
						if (subStart >= 0 && subStart < subEnd) {
							String text = ss.getUnformattedString();
							String before = subStart == 0 ? "" : text.substring(0, subStart);
							int beforeWidth = before.isEmpty() ? 0 : RenderHelper.fontRenderer.getStringWidth(ss.getTextFormattingStyle() + before);

							String formatString = text.substring(subStart, subEnd);

							RenderHelper.drawRect(beforeWidth + subWidth, 0, beforeWidth + subWidth + RenderHelper.fontRenderer.getStringWidth(ss.getTextFormattingStyle() + formatString), 7);
						}
						subIndex += ss.getStringLength();
						subWidth += ss.getStringWidth();
					}
				}

			}
			GlStateManager.color(1F, 1F, 1F, 1F);
			GlStateManager.disableBlend();
			GlStateManager.enableTexture2D();
			GlStateManager.translate(0, 0, -0.1);
		}
	}

	/** for every StyledStringCompound */
	public void forCompounds(Consumer<StyledStringLine> format) {
		text.forEach(t -> format.accept(t));
	}

	/** for every StyledString */
	public void forStrings(Consumer<StyledString> format) {
		text.forEach(t -> t.strings.forEach(ss -> format.accept(ss)));
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

	public void enableSpecialFormattingOnSelected(List<TextFormatting> formatting) {
		formatSelections(e -> {
			e.style.toggleSpecialFormatting(formatting, true);
			return e;
		});
	}

	public void disableSpecialFormattingOnSelected(List<TextFormatting> formatting) {
		formatSelections(e -> {
			e.style.toggleSpecialFormatting(formatting, false);
			return e;
		});
	}

	public void setTextColourOnSelected(int colour) {
		formatSelections(e -> {
			e.style.setFontColour(colour);
			return e;
		});
	}

	public void formatSelections(Function<StyledString, StyledString> action) {
		// int i = 0;
		List<TextSelection> allSelects = getAllSelections();
		for (TextSelection select : allSelects) {
			for (int y = select.startY; y <= select.endY; y++) {
				StyledStringLine c = getLine(y);
				if (c == null) {
					continue;
				}
				int[] subSelect = select.getSubStringSize(c.getCachedUnformattedString(), y);
				if (subSelect[0] != -1 && subSelect[1] != -1) {
					int start = subSelect[0];
					int end = subSelect[1];
					List<StyledString> newStrings = Lists.newArrayList();
					int subIndex = 0;
					for (StyledString ss : c.strings) {
						int stringStart = subIndex;
						int stringEnd = subIndex + ss.getStringLength();

						int selectStart = Math.max(stringStart, start);
						int selectEnd = Math.min(stringEnd, end);

						int subStart = selectStart - subIndex;
						int subEnd = selectEnd - subIndex;
						if (subStart >= 0 && subStart < subEnd) {
							String text = ss.getUnformattedString();
							String before = subStart == 0 ? "" : text.substring(0, subStart);
							String formatString = text.substring(subStart, subEnd);
							String after = subEnd == text.length() ? "" : text.substring(subEnd, text.length());

							addWithCombine(newStrings, new StyledString(before, ss.getStyle().copy()));
							addWithCombine(newStrings, action.apply(new StyledString(formatString, ss.getStyle().copy())));
							addWithCombine(newStrings, new StyledString(after, ss.getStyle().copy()));

						} else {
							addWithCombine(newStrings, ss);
						}

						subIndex += ss.getStringLength();
					}

					/* for (StyledString ss : c.strings) { String text = ss.getUnformattedString(); int subIndex = 0; int elementStart = index + subIndex; int elementEnd = elementStart + text.length() - subIndex; if (InteractionHelper.overlapX(elementStart, new double[] { next.getStartX(), 0, next.getEndX(), 0 }) || InteractionHelper.overlapX(elementEnd, new double[] { next.getStartX(), 0, next.getEndX(), 0 })) { int stringStart = next.getStartX() - elementStart; int stringEnd = Math.max(text.length(), next.getEndX() - elementEnd); String before = subIndex < stringStart ? text.substring(subIndex, stringStart) : ""; if (!before.isEmpty()) { addWithCombine(newStrings, new StyledString(before, ss.getStyle().copy())); } // if (actualStart >= 0 && actualStart < text.length()) { String select = (stringStart < 0 || stringEnd < 0) ? "" : text.substring(stringStart, stringEnd); if (!select.isEmpty()) { StyledString formatted = action.apply(new StyledString(select, ss.getStyle().copy())); addWithCombine(newStrings, formatted); } subIndex += before.length() + select.length(); } if (next.getEndX() == index + subIndex) { break; } String after = subIndex == text.length() - 1 ? "" : text.substring(subIndex); if (!after.isEmpty()) { addWithCombine(newStrings, new StyledString(after, ss.getStyle().copy())); } index += ss.getStringLength(); } */
					c.strings = newStrings;
					// newStrings = newStrings;
				}
			}
		}
		/* for (StyledStringCompound c : text) { List<StyledString> newStrings = Lists.newArrayList(); int index = 0; boolean started = false; boolean finished = false; for (StyledString ss : c.strings) { String text = ss.getUnformattedString(); Iterator<TextSelection> it = allSelects.iterator(); int subIndex = 0; while (it.hasNext()) { TextSelection next = it.next(); int elementStart = index + subIndex; int elementEnd = elementStart + text.length() - subIndex; if (InteractionHelper.overlapX(elementStart, new double[] { next.getStartX(), 0, next.getEndX(), 0 }) || InteractionHelper.overlapX(elementEnd, new double[] { next.getStartX(), 0, next.getEndX(), 0 })) { int stringStart = next.getStartX() - elementStart; int stringEnd = Math.max(text.length(), next.getEndX() - elementEnd); String before = subIndex < stringStart ? text.substring(subIndex, stringStart) : ""; if (!before.isEmpty()) { addWithCombine(newStrings, new StyledString(before, ss.getStyle().copy())); } // if (actualStart >= 0 && actualStart < text.length()) { String select = (stringStart < 0 || stringEnd < 0) ? "" : text.substring(stringStart, stringEnd); if (!select.isEmpty()) { StyledString formatted = action.apply(new StyledString(select, ss.getStyle().copy())); addWithCombine(newStrings, formatted); } subIndex += before.length() + select.length(); } if (next.getEndX() == index + subIndex) { break; } } String after = subIndex == text.length() - 1 ? "" : text.substring(subIndex); if (!after.isEmpty()) { addWithCombine(newStrings, new StyledString(after, ss.getStyle().copy())); } index += ss.getStringLength(); } c.strings = newStrings; i++; } */
		GuiActions.UPDATE_TEXT_SCALING.trigger(this);

	}

	/* public void addSelection(int indexY, int startX, int finishX, TextSelectionType type) { boolean ctrl_key = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL); if (ctrl_key) { combineAllSelections(); // if (type == TextSelectionType.SET_SELECTION) { // type = TextSelectionType.COMBINE; // } } switch (type) { /* case COMBINE: if (startX != finishX) { toCombine.putIfAbsent(indexY, Lists.newArrayList()); List<TextSelection> sortingSelections = Lists.newArrayList(toCombine.get(indexY)); sortingSelections.add(new TextSelection(startX, finishX)); sortingSelections.sort(new Comparator<TextSelection>() { public int compare(TextSelection str1, TextSelection str2) { return SonarHelper.compareWithDirection(str1.getStart(), str2.getStart(), SortingDirection.UP); } }); List<TextSelection> newSelections = Lists.newArrayList(); sortingSelections.forEach(s -> TextSelection.addWithCombine(newSelections, s)); toCombine.put(indexY, newSelections); } break; */
	/* case DESELECT_ALL: savedSelections.clear(); newSelections.clear(); break; case SELECT_ALL: savedSelections.clear(); int i = 0; for (StyledStringCompound e : text) { newSelections.put(i, Lists.newArrayList(new TextSelection(0, e.toString().length()))); i++; } break; case SET_LINE: newSelections.clear(); newSelections.put(indexY, Lists.newArrayList(new TextSelection(startX, finishX))); break; case SET_SELECTION: newSelections.clear(); newSelections.put(indexY, Lists.newArrayList(new TextSelection(startX, finishX))); break; default: break; } } */
	public void addWithCombine(List<StyledString> strings, StyledString ss) {
		if (ss == null || ss.getUnformattedString().isEmpty()) {
			return; // deletion
		}
		if (strings.isEmpty()) {
			strings.add(ss);
			return;
		} else {
			StyledString lastSS = strings.get(strings.size() - 1);
			if (lastSS.canCombine(ss)) {
				lastSS.combine(ss);
			} else {
				strings.add(ss);
			}
		}
	}

	public boolean doContainerClick(double clickX, double clickY, int key) {
		Tuple<IDisplayElement, double[]> element = getElementAtXY(clickX, clickY);
		Tuple<StyledStringLine, Integer> click = getStyledStringAtXY(clickX, clickY);
		if (click != null && click.getSecond() >= 0) {
			StyledStringLine ss = click.getFirst();
			if (isDoubleClick()) {
				GuiActions.SELECT_ALL.trigger(this);
			} else {
				String formatted = ss.getCachedUnformattedString();

				if (!GuiScreen.isShiftKeyDown()) {
					if (GuiScreen.isCtrlKeyDown() && cursorPosition.validPosition() && selectPosition.validPosition()) {
						TextSelection.addWithCombine(savedSelections, new TextSelection(cursorPosition, selectPosition));
					} else {
						savedSelections.clear();
					}
					selectPosition.removeCursor();
				}
				cursorPosition.setCursor(getIndexClick(formatted, element.getSecond()[0]), click.getSecond());
				lastCursorClick = mc.getSystemTime();
			}
			return true;
		} else if (cursorPosition.validPosition()) {
			lastCursorClick = -1;
			cursorPosition.removeCursor();
			selectPosition.removeCursor();
			GuiActions.DESELECT_ALL.trigger(this);
		}else{
			cursorPosition.setCursor(0, 0);
		}

		return false;
	}

	// TEXT DRAGGING

	public void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
		boolean setTextDrag = true;
		Tuple<Boolean, double[]> canClick = canClickContainer(x, y);
		if (canClick.getFirst() && lastCursorClick != -1) {
			if (isDragging) {
				boolean canContinue = Mouse.isButtonDown(0);
				if (!canContinue) {
					isDragging = false;
				} else {
					cursorPosition.setCursor(getDragPositionFromMouseXY(x, y));
				}
			} else if (lastCursorClick + 400 < mc.getSystemTime()) {
				isDragging = Mouse.isButtonDown(0);
				lastCursorClick = mc.getSystemTime();
				if (isDragging) {
					selectPosition.setCursor(cursorPosition.x, cursorPosition.y);
				}
			}
		}
		super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
	}

	public int[] getDragPositionFromMouseXY(int x, int y) {
		Tuple<Boolean, double[]> canClick = canClickContainer(x, y);
		if (!canClick.getFirst()) {
			return new int[] { 0, 0 };
		}
		return getDragPositionFromContainerXY(canClick.getSecond()[0], canClick.getSecond()[1]);
	}

	public int[] getDragPositionFromContainerXY(double xClick, double yClick) {
		Tuple<IDisplayElement, double[]> element = getElementAtXY(xClick, yClick);
		Tuple<StyledStringLine, Integer> click = getStyledStringAtXY(xClick, yClick);
		if (click != null && click.getSecond() >= 0) {
			String formatted = click.getFirst().getCachedUnformattedString();
			return new int[] { getIndexClick(formatted, element.getSecond()[0]), click.getSecond() };
		}
		double[] align = text.getHolder().getAlignmentTranslation(text);
		if (xClick < align[0] || yClick < align[1]) {
			return new int[] { 0, 0 };
		}
		return new int[] { getLineLength(getLineCount() - 1), getLineCount() - 1 };
	}

	public int getIndexClick(String eString, double eWidth) {
		return FontHelper.getIndexFromPixel(eString, (int) Math.ceil((InfoRenderer.getStringWidth(eString) * (eWidth / text.getActualScaling()[0])) + 3F));
	}

	/* public double[] getTextDragStart(int mouseX, int mouseY) { return textDragStart; } public double[] getTextDragEnd(int mouseX, int mouseY) { if (textDragEnd == null) { return findClosestValidTextDrag(mouseX, mouseY); } return textDragEnd; } public double[] findClosestValidTextDrag(int mouseX, int mouseY) { double startX = getAlignmentTranslation()[0]; double startY = getAlignmentTranslation()[1]; double endX = startX + getActualScaling()[0]; double endY = startY + getActualScaling()[1]; double validX = mouseX <= startX ? startX : mouseX >= endX ? endX : mouseX; double validY = mouseY <= startY ? startY : mouseY >= endY ? endY : mouseY; double dragX = (validX - startX) / getActualScaling()[2]; double dragY = (validY - startY) / getActualScaling()[2]; return new double[] { dragX, dragY }; } public void updateSelectedText(int mouseX, int mouseY) { double[] start = getTextDragStart(mouseX, mouseY); double[] end = getTextDragEnd(mouseX, mouseY); if (start != null && end != null) { setSelections(start, end); } } public void setSelections(double[] start, double[] end) { double[] guiAlign = getAlignmentTranslation(); double[] worldRenderOffset = c.getAlignmentTranslation(); double[] alignArray = c.getFullAlignmentTranslation(text); double startXC = alignArray[0] - worldRenderOffset[0]; double startYC = alignArray[1] - worldRenderOffset[1]; double endXC = alignArray[0] - worldRenderOffset[0] + text.getActualScaling()[0]; double endYC = alignArray[1] - worldRenderOffset[1] + text.getActualScaling()[1]; double[] eBox = new double[] { startXC, startYC, endXC, endYC }; boolean cursorAtTop = start[1] <= end[1]; // if false, cursor is at the bottom double[] clickBox = new double[] { Math.min(start[0], end[0]), Math.min(start[1], end[1]), Math.max(start[0], end[0]), Math.max(start[1], end[1]) }; if (InteractionHelper.checkOverlap(eBox, clickBox)) { double subStartX = Math.max(0, clickBox[0] - startXC); double subStartY = Math.max(0, clickBox[1] - startYC); double subEndX = Math.min(text.getActualScaling()[0], clickBox[2] - startXC); double subEndY = Math.min(text.getActualScaling()[1], clickBox[3] - startYC); double[] subBox = new double[] { subStartX, subStartY, subEndX, subEndY }; double height = 0; int i = 0; int newCursorY = -1; int newCursorIndex = -1; for (StyledStringCompound c : text) { // do x alignment double cHeight = c.getStringHeight() * text.getActualScaling()[2]; double[] compoundBox = new double[] { 0, height, text.getActualScaling()[0], height + cHeight }; if (InteractionHelper.checkOverlap(compoundBox, subBox)) { String formatted = c.getCachedUnformattedString(); int startX = FontHelper.getIndexFromPixel(formatted, (int) Math.ceil((InfoRenderer.getStringWidth(formatted) * (subBox[0] / text.getActualScaling()[0])) + 3F)); int finishX = FontHelper.getIndexFromPixel(formatted, (int) Math.ceil((InfoRenderer.getStringWidth(formatted) * (subBox[2] / text.getActualScaling()[0])) + 3F)); addSelection(i, startX, finishX, TextSelectionType.SET_LINE); if (newCursorY == -1 || cursorAtTop ? i < newCursorY : i > newCursorY) { newCursorY = i; newCursorIndex = cursorAtTop ? startX : finishX; } } height += cHeight; i++; } cursorY = newCursorY; cursorX = newCursorIndex; } } */

	//// CURSOR POSITION \\\\

	//// SELECTION POSITION \\\\

	/* FIXME - DO SHIFT ARROW SELECTION, HAVE TWO POSITIONS, CURSOR AND DRAG POSITIONpublic void trimSelections(int lineMove, int indexMove) { if (lineMove != 0) { int newLine = cursorY - lineMove; if (newLine >= 0) { } } Map<Integer, List<TextSelection>> combine_map = Maps.newHashMap(toCombine); for (Entry<Integer, List<TextSelection>> e : combine_map.entrySet()) { } } */

	public List<TextSelection> getAllSelections() {
		List<TextSelection> stored_map = savedSelections;
		boolean hasCursorSelection = cursorPosition.validPosition() && selectPosition.validPosition();
		if (hasCursorSelection) {
			stored_map = Lists.newArrayList();
			for (TextSelection s : savedSelections) {
				stored_map.add(new TextSelection(s.startX, s.endX, s.endY, s.endY));
			}
			TextSelection cursorSelection = new TextSelection(cursorPosition, selectPosition);
			TextSelection.addWithCombine(stored_map, cursorSelection);
		}
		return stored_map;
	}

	public List<TextSelection> combineAllSelections() {
		savedSelections = getAllSelections();
		// currentSelection = null;
		return savedSelections;
	}

	public void selectAll() {
		savedSelections.clear();
		selectPosition.setCursor(0, 0);
		cursorPosition.setCursor(getLineLength(getLineCount() - 1), getLineCount() - 1);
	}

	public void deselectAll() {
		savedSelections.clear();
		// cursorPosition.removeCursor(); // keep cursor pos?
		selectPosition.removeCursor();
	}

	public int moveLastSelectionIndex() {
		return 0;
	}

	/* public void setSelectedText(ITextElement e, double[] start, double[] end) { double[] guiAlign = getAlignmentTranslation(); double[] worldRenderOffset = c.getAlignmentTranslation(); double[] alignArray = c.getFullAlignmentTranslation(e); double startXC = alignArray[0] - worldRenderOffset[0]; double startYC = alignArray[1] - worldRenderOffset[1]; double endXC = alignArray[0] - worldRenderOffset[0] + e.getActualScaling()[0]; double endYC = alignArray[1] - worldRenderOffset[1] + e.getActualScaling()[1]; double[] eBox = new double[] { startXC, startYC, endXC, endYC }; double[] clickBox = new double[] { Math.min(start[0], end[0]), Math.min(start[1], end[1]), Math.max(start[0], end[0]), Math.max(start[1], end[1]) }; if (InteractionHelper.checkOverlap(eBox, clickBox)) { double subStartX = Math.max(0, clickBox[0] - startXC); double subStartY = Math.max(0, clickBox[1] - startYC); double subEndX = Math.min(e.getActualScaling()[0], clickBox[2] - startXC); double subEndY = Math.min(e.getActualScaling()[1], clickBox[3] - startYC); TextSelectionType type = (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) ? TextSelectionType.COMBINE : TextSelectionType.SET_SELECTION; addSelection(e, new double[] { subStartX, subStartY }, new double[] { subEndX, subEndY }, type); } else { addSelection(e, new double[2], new double[2], TextSelectionType.DESELECT_ALL); } } */
	@Override
	protected void keyTyped(char c, int i) throws IOException {
		if (cursorPosition.validPosition()) {
			StyledStringLine ss = cursorPosition.getTypingLine(this);
			boolean triggered = TypingKeyFunctions.checkFunction(this, ss, c, i);
			GuiActions.UPDATE_TEXT_SCALING.trigger(this);
			if (triggered) {
				return;
			} else if (ChatAllowedCharacters.isAllowedCharacter(c)) {
				// get end of selection and delete all selection.
				cursorPosition.x = ss.addText(cursorPosition.x, Character.toString(c));
				return;
			}

		} else if (HotKeyFunctions.checkFunction(this, c, i)) {
			GuiActions.UPDATE_TEXT_SCALING.trigger(this);
			return;
		}
		super.keyTyped(c, i);
	}

	public final Tuple<StyledStringLine, Integer> getStyledStringAtXY(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> element = getElementAtXY(clickX, clickY);
		if (element.getFirst() != null && element.getFirst() instanceof StyledTextElement) {
			return ((StyledTextElement) element.getFirst()).getStringClicked(element.getSecond()[0], element.getSecond()[1]);
		}
		return new Tuple(null, -1);
	}

	@Override
	public int getLineCount() {
		return text.textLines.size();
	}

	@Override
	public int getLineLength(int line) {
		if (line >= getLineCount()) {
			return 0;
		}
		return getUnformattedLine(line).length();
	}

	@Override
	public int getLineWidth(int line) {
		return RenderHelper.fontRenderer.getStringWidth(getFormattedLine(line));
	}

	@Override
	public String getUnformattedLine(int line) {
		StyledStringLine ss = getLine(line);
		return ss == null ? "" : ss.getCachedUnformattedString();
	}

	@Override
	public String getFormattedLine(int line) {
		StyledStringLine ss = getLine(line);
		return ss == null ? "" : ss.getCachedFormattedString();
	}

	@Override
	public StyledStringLine getLine(int line) {
		if (line >= getLineCount()) {
			return null;
		}
		return text.textLines.get(line);
	}
}