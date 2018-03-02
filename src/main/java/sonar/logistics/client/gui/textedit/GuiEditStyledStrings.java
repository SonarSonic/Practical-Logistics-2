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

import javax.xml.ws.Holder;

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
			if (i == cursorPosition.y) {
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
			int cursorPos = getCursorRenderPos(text);
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

	public int getCursorRenderPos(StyledStringLine c) {
		return getCursorRenderPos(c, cursorPosition.getTypingIndex(this));
	}

	public int getCursorRenderPos(StyledStringLine c, int index) {
		int i = 0;
		if (!c.getStrings().isEmpty()) {
			int start = 0, end = index;
			if (end == 0) {
				return 0;
			}
			int index_count = 0;
			for (StyledString ss : c.getStrings()) {
				int subEnd = Math.min(index_count + ss.getStringLength(), end) - index_count;
				if (subEnd > 0) {
					String unformatted = ss.getTextFormattingStyle() + ss.getUnformattedString().substring(0, subEnd);
					i += RenderHelper.fontRenderer.getStringWidth(unformatted);
					index_count += ss.getStringLength();
				} else {
					// System.out.println("whyo");
				}
			}
		}
		return i;
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
					for (StyledString ss : line.getStrings()) {
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
		text.forEach(t -> t.getStrings().forEach(ss -> format.accept(ss)));
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

	public void deleteAllSelected() {
		formatSelections(ss -> null);
		selectPosition.removeCursor();
		savedSelections.clear();
	}

	public void formatSelections(Function<StyledString, StyledString> action) {
		formatSelections(getAllSelections(), action);
	}

	public void formatSelections(List<TextSelection> selects, Function<StyledString, StyledString> action) {
		for (TextSelection select : selects) {
			for (int y = select.startY; y <= select.endY; y++) {
				StyledStringLine c = getLine(y);
				if (c == null) {
					continue;
				}
				int[] subSelect = select.getSubStringSize(c.getCachedUnformattedString(), y);
				int start = subSelect[0], end = subSelect[1];
				if (start != -1 && end != -1) {
					List<StyledString> formatted_strings = Lists.newArrayList();

					int index_count = 0;
					for (StyledString ss : c.getStrings()) {
						int subStart = Math.max(index_count, start) - index_count;
						int subEnd = Math.min(index_count + ss.getStringLength(), end) - index_count;

						if (subStart >= 0 && subStart < subEnd) {
							String[] subStrings = getSubStrings(subStart, subEnd, ss.getUnformattedString());
							addWithCombine(formatted_strings, new StyledString(subStrings[0], ss.getStyle().copy()));
							addWithCombine(formatted_strings, action.apply(new StyledString(subStrings[1], ss.getStyle().copy())));
							addWithCombine(formatted_strings, new StyledString(subStrings[2], ss.getStyle().copy()));
						} else {
							addWithCombine(formatted_strings, ss);
						}

						index_count += ss.getStringLength();
					}

					c.setStrings(formatted_strings);
				}
			}
		}
		GuiActions.UPDATE_TEXT_SCALING.trigger(this);
		text.getHolder().getContainer().updateActualScaling();

	}

	public String[] getSubStrings(int subStart, int subEnd, String s) {
		String before_string = subStart == 0 ? "" : s.substring(0, subStart);
		String format_string = s.substring(subStart, subEnd);
		String after_string = subEnd == s.length() ? "" : s.substring(subEnd, s.length());
		return new String[] { before_string, format_string, after_string };
	}

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
		Tuple<StyledStringLine, Integer> click = getLineClicked(clickX, clickY);
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
				cursorPosition.setCursor(getDragPositionFromContainerXY(clickX, clickY));
				lastCursorClick = mc.getSystemTime();
			}
			return true;
		} else if (cursorPosition.validPosition()) {
			lastCursorClick = -1;
			cursorPosition.removeCursor();
			selectPosition.removeCursor();
			GuiActions.DESELECT_ALL.trigger(this);
		} else {
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
			} else if (lastCursorClick + 100 < mc.getSystemTime()) {
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

	public int[] getDragPositionFromContainerXY(double clickX, double clickY) {
		int[] index = getIndexClicked(clickX, clickY);
		if (index != null) {
			return index;
		}
		double[] align = text.getHolder().getAlignmentTranslation(text);
		if (clickX < align[0] || clickY < align[1]) {
			return new int[] { 0, 0 };
		}
		Tuple<StyledStringLine, Integer> click = getLineClicked(clickX, clickY);
		int closestLine = Math.min(getLineCount() - 1, click.getSecond() == -1 ? getLineCount() - 1 : click.getSecond());
		return new int[] { getLineLength(closestLine), closestLine };
	}

	/* public int getIndexClick(String eString, double eWidth) { return FontHelper.getIndexFromPixel(eString, (int) Math.ceil((InfoRenderer.getStringWidth(eString) * (eWidth / text.getActualScaling()[0])) + 3F)); } */

	/* public double[] getTextDragStart(int mouseX, int mouseY) { return textDragStart; } public double[] getTextDragEnd(int mouseX, int mouseY) { if (textDragEnd == null) { return findClosestValidTextDrag(mouseX, mouseY); } return textDragEnd; } public double[] findClosestValidTextDrag(int mouseX, int mouseY) { double startX = getAlignmentTranslation()[0]; double startY = getAlignmentTranslation()[1]; double endX = startX + getActualScaling()[0]; double endY = startY + getActualScaling()[1]; double validX = mouseX <= startX ? startX : mouseX >= endX ? endX : mouseX; double validY = mouseY <= startY ? startY : mouseY >= endY ? endY : mouseY; double dragX = (validX - startX) / getActualScaling()[2]; double dragY = (validY - startY) / getActualScaling()[2]; return new double[] { dragX, dragY }; } public void updateSelectedText(int mouseX, int mouseY) { double[] start = getTextDragStart(mouseX, mouseY); double[] end = getTextDragEnd(mouseX, mouseY); if (start != null && end != null) { setSelections(start, end); } } public void setSelections(double[] start, double[] end) { double[] guiAlign = getAlignmentTranslation(); double[] worldRenderOffset = c.getAlignmentTranslation(); double[] alignArray = c.getFullAlignmentTranslation(text); double startXC = alignArray[0] - worldRenderOffset[0]; double startYC = alignArray[1] - worldRenderOffset[1]; double endXC = alignArray[0] - worldRenderOffset[0] + text.getActualScaling()[0]; double endYC = alignArray[1] - worldRenderOffset[1] + text.getActualScaling()[1]; double[] eBox = new double[] { startXC, startYC, endXC, endYC }; boolean cursorAtTop = start[1] <= end[1]; // if false, cursor is at the bottom double[] clickBox = new double[] { Math.min(start[0], end[0]), Math.min(start[1], end[1]), Math.max(start[0], end[0]), Math.max(start[1], end[1]) }; if (InteractionHelper.checkOverlap(eBox, clickBox)) { double subStartX = Math.max(0, clickBox[0] - startXC); double subStartY = Math.max(0, clickBox[1] - startYC); double subEndX = Math.min(text.getActualScaling()[0], clickBox[2] - startXC); double subEndY = Math.min(text.getActualScaling()[1], clickBox[3] - startYC); double[] subBox = new double[] { subStartX, subStartY, subEndX, subEndY }; double height = 0; int i = 0; int newCursorY = -1; int newCursorIndex = -1; for (StyledStringCompound c : text) { // do x alignment double cHeight = c.getStringHeight() * text.getActualScaling()[2]; double[] compoundBox = new double[] { 0, height, text.getActualScaling()[0], height + cHeight }; if (InteractionHelper.checkOverlap(compoundBox, subBox)) { String formatted = c.getCachedUnformattedString(); int startX = FontHelper.getIndexFromPixel(formatted, (int) Math.ceil((InfoRenderer.getStringWidth(formatted) * (subBox[0] / text.getActualScaling()[0])) + 3F)); int finishX = FontHelper.getIndexFromPixel(formatted, (int) Math.ceil((InfoRenderer.getStringWidth(formatted) * (subBox[2] / text.getActualScaling()[0])) + 3F)); addSelection(i, startX, finishX, TextSelectionType.SET_LINE); if (newCursorY == -1 || cursorAtTop ? i < newCursorY : i > newCursorY) { newCursorY = i; newCursorIndex = cursorAtTop ? startX : finishX; } } height += cHeight; i++; } cursorY = newCursorY; cursorX = newCursorIndex; } } */

	//// CURSOR POSITION \\\\

	//// SELECTION POSITION \\\\

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

	public boolean hasSelections() {
		return selectPosition.validPosition() || !savedSelections.isEmpty();
	}

	public int moveLastSelectionIndex() {
		return 0;
	}

	/// special key actions

	public void onCarriageReturn() {
		if (cursorPosition.validPosition()) {
			GuiActions.DELETE_SELECTED.action.trigger(this);

			TextSelection afterCursor = new TextSelection(cursorPosition.x, cursorPosition.y, Integer.MAX_VALUE, cursorPosition.y);
			StyledStringLine line = new StyledStringLine();
			formatSelections(ss -> {
				line.addWithCombine(ss);
				return null;
			});
			text.addNewLine(cursorPosition.y + 1, line);
			cursorPosition.setCursor(0, cursorPosition.y + 1);
		}
	}

	public void addText(String toAppend) {
		if (cursorPosition.validPosition()) {
			if (hasSelections()) {
				GuiActions.DELETE_SELECTED.action.trigger(this);
			}
			StyledStringLine line = cursorPosition.getTypingLine(this);
			if (line != null) {
				if (cursorPosition.x == 0) {
					if (line.getStrings().isEmpty()) {
						line.setStrings(Lists.newArrayList(new StyledString(toAppend, createStylingFromEnabled())));
					} else {
						StyledString addTo = line.getStrings().get(0);
						addTo.setUnformattedString(toAppend + addTo.getUnformattedString());
					}
					cursorPosition.moveX(this, toAppend.length());
				} else {
					Holder<Boolean> hold = new Holder(false);
					formatSelections(Lists.newArrayList(getTypeBox()), ss -> {
						ss.setUnformattedString(ss.getUnformattedString() + toAppend);
						hold.value = true;
						return ss;
					});
					if (hold.value) {
						cursorPosition.moveX(this, toAppend.length());
					}
				}
			} else {

				// FIXME should we add another line instead?
			}
		}
	}

	public void removeText(int key) {
		if (cursorPosition.validPosition()) {
			if (hasSelections()) {
				GuiActions.DELETE_SELECTED.action.trigger(this);
			} else {
				StyledStringLine line = cursorPosition.getTypingLine(this);
				if (line == null) {
					return;
				}
				if (line.getStrings().isEmpty()) {
					if (text.getLines().size() != 1) {
						text.deleteLine(cursorPosition.y);
						cursorPosition.setCursor(Integer.MAX_VALUE, Math.max(0, cursorPosition.y));
					}
					return;
				}
				Holder<Boolean> hold = new Holder(false);
				if (key == Keyboard.KEY_DELETE) {
					formatSelections(Lists.newArrayList(getDeleteBox()), ss -> {
						hold.value = true;
						return null;
					});
				} else {
					formatSelections(Lists.newArrayList(getBackspaceBox()), ss -> {
						hold.value = true;
						return null;
					});
				}
				if (hold.value) {
					cursorPosition.moveX(this, key == Keyboard.KEY_DELETE ? 0 : -1);
				}
			}
		}
	}

	public TextSelection getTypeBox() {
		int typeIndex = cursorPosition.getTypingIndex(this);
		return new TextSelection(typeIndex - 1, typeIndex, cursorPosition.y, cursorPosition.y);
	}

	public TextSelection getBackspaceBox() {
		int typeIndex = cursorPosition.getTypingIndex(this);
		return new TextSelection(typeIndex - 1, typeIndex, cursorPosition.y, cursorPosition.y);
	}

	public TextSelection getDeleteBox() {
		int typeIndex = cursorPosition.getTypingIndex(this);
		return new TextSelection(typeIndex, typeIndex + 1, cursorPosition.y, cursorPosition.y);
	}

	public SonarStyling createStylingFromEnabled() {
		SonarStyling styling = new SonarStyling();

		styling.rgb = currentColour;
		styling.toggleSpecialFormatting(specials, true);

		return styling;
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
				addText(Character.toString(c));
				return;
			}

		} else if (HotKeyFunctions.checkFunction(this, c, i)) {
			GuiActions.UPDATE_TEXT_SCALING.trigger(this);
			return;
		}
		super.keyTyped(c, i);
	}

	public final int[] getIndexClicked(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> element = getElementAtXY(clickX, clickY);
		if (element.getFirst() != null && element.getFirst() instanceof StyledTextElement) {
			int[] clicked = ((StyledTextElement) element.getFirst()).getIndexClicked(element.getSecond()[0], element.getSecond()[1]);
			return clicked;
		}
		return null;
	}

	public final Tuple<StyledStringLine, Integer> getLineClicked(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> element = getElementAtXY(clickX, clickY);
		if (element.getFirst() != null && element.getFirst() instanceof StyledTextElement) {
			return ((StyledTextElement) element.getFirst()).getLineClicked(element.getSecond()[0], element.getSecond()[1]);
		}
		return new Tuple(null, -1);
	}

	public final Tuple<StyledString, Integer> getStringClicked(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> element = getElementAtXY(clickX, clickY);
		if (element.getFirst() != null && element.getFirst() instanceof StyledTextElement) {
			return ((StyledTextElement) element.getFirst()).getStringClicked(element.getSecond()[0], element.getSecond()[1]);
		}
		return new Tuple(null, -1);
	}

	public final Tuple<Character, Integer> getCharClicked(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> element = getElementAtXY(clickX, clickY);
		if (element.getFirst() != null && element.getFirst() instanceof StyledTextElement) {
			return ((StyledTextElement) element.getFirst()).getCharClicked(element.getSecond()[0], element.getSecond()[1]);
		}
		return new Tuple(null, -1);
	}

	@Override
	public int getLineCount() {
		return text.getLines().size();
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
		return text.getLines().get(line);
	}
}