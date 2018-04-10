package sonar.logistics.client.gui.textedit;

import static net.minecraft.client.renderer.GlStateManager.popMatrix;
import static net.minecraft.client.renderer.GlStateManager.pushMatrix;
import static net.minecraft.client.renderer.GlStateManager.translate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.xml.ws.Holder;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextFormatting;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.api.displays.WidthAlignment;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.text.IStyledString;
import sonar.logistics.api.displays.elements.text.SonarStyling;
import sonar.logistics.api.displays.elements.text.StyledInfo;
import sonar.logistics.api.displays.elements.text.StyledString;
import sonar.logistics.api.displays.elements.text.StyledStringHelper;
import sonar.logistics.api.displays.elements.text.StyledStringLine;
import sonar.logistics.api.displays.elements.text.StyledTextElement;
import sonar.logistics.api.displays.elements.text.TextSelection;
import sonar.logistics.client.gui.display.GuiAbstractEditElements;
import sonar.logistics.client.gui.textedit.hotkeys.GuiActions;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;

public class GuiStyledStringFunctions extends GuiAbstractEditElements implements ILineCounter {

	public final StyledTextElement text;
	public final CursorPosition cursorPosition = CursorPosition.newInvalid();
	public final CursorPosition selectPosition = CursorPosition.newInvalid();
	public List<TextSelection> savedSelections = new ArrayList<>();
	public List<TextFormatting> specials = new ArrayList<>();
	public int currentColour = -1;

	public GuiStyledStringFunctions(StyledTextElement text, TileAbstractDisplay display) {
		super(text, text.getHolder().getContainer(), display);
		this.text = text;
	}

	//// SELECTION POSITIONS \\\\

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
		if (text.getLines().isEmpty()) {
			StyledStringLine l = new StyledStringLine(text);
			text.addNewLine(0, l);
			l.addWithCombine(new StyledString("", createStylingFromEnabled()));
			return new int[] { 0, 0 };
		}

		Tuple<StyledStringLine, Integer> line = getLineClicked(clickX, clickY);
		Tuple<IStyledString, Integer> string = getStringClicked(clickX, clickY);
		int yPosition = Math.min(getLineCount() - 1, line.getSecond() == -1 ? getLineCount() - 1 : line.getSecond());
		int xPosition = string.getSecond() == StyledTextElement.AFTER || string.getSecond() == -1 ? getLineLength(yPosition) : 0;
		if (yPosition < 0) {
			return new int[] { 0, 0 };
		}
		return new int[] { xPosition, yPosition };
	}

	/// special key actions

	public void onCarriageReturn() {
		if (cursorPosition.validPosition()) {
			GuiActions.DELETE_SELECTED.action.trigger(this);

			TextSelection afterCursor = new TextSelection(cursorPosition.x, Integer.MAX_VALUE, cursorPosition.y, cursorPosition.y);
			StyledStringLine line = new StyledStringLine(text);
			formatSelections(Lists.newArrayList(afterCursor), (l, ss) -> {
				line.addWithCombine(ss);
				return null;
			});
			text.addNewLine(cursorPosition.y + 1, line);
			cursorPosition.setCursor(0, cursorPosition.y + 1);
		}
	}

	public void addText(String toAppend) {
		if (!cursorPosition.validPosition()) {
			cursorPosition.setYToLast(this);
			cursorPosition.setXToLast(this);
		}
		/// SHOULD WE JUST USE ADD STYLED STRING FOR TYPING?
		if (hasSelections()) {
			GuiActions.DELETE_SELECTED.action.trigger(this);
		}
		StyledStringLine line = cursorPosition.getTypingLine(this);
		if (line != null) {
			if (cursorPosition.x == 0) {
				if (line.getStrings().isEmpty()) {
					line.setStrings(Lists.newArrayList(new StyledString(toAppend, createStylingFromEnabled())));
				} else {
					IStyledString addTo = line.getStrings().get(0);
					if (addTo instanceof StyledString) {
						addTo.setUnformattedString(toAppend + addTo.getUnformattedString());
					} else {
						IStyledString toAdd = new StyledString(toAppend, addTo.getStyle().copy());
						addStyledStrings(Lists.newArrayList(toAdd));
					}
				}
				cursorPosition.moveX(this, toAppend.length());
			} else {
				Holder<Boolean> hold = new Holder(false);
				Holder<IStyledString> string = new Holder();
				formatSelections(Lists.newArrayList(getTypeBox()), (l, ss) -> {
					if (ss instanceof StyledString) {
						ss.setUnformattedString(ss.getUnformattedString() + toAppend);
						hold.value = true;
						string.value = null;
					} else {
						string.value = ss;
					}
					return ss;
				});
				if (string.value != null) {
					addStyledStrings(Lists.newArrayList(new StyledString(toAppend, string.value.getStyle().copy())));
					/* List<IStyledString> newStrings = new ArrayList<>(); for(IStyledString ss : string.value.getLine()){ newStrings.add(ss); if(ss==string.value){ newStrings.add(new StyledString(toAppend, ss.getStyle().copy())); } } string.value.getLine().setStrings(newStrings); */
					hold.value = true;
				}

				if (hold.value) {
					cursorPosition.moveX(this, toAppend.length());
				}
			}
		} else {

			// FIXME should we add another line instead?
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
						cursorPosition.moveY(this, -1);
						cursorPosition.setXToLast(this);
					}
					return;
				}
				Holder<Boolean> hold = new Holder(false);
				if (key == Keyboard.KEY_DELETE) {
					formatSelections(Lists.newArrayList(getDeleteBox()), (l, ss) -> {
						hold.value = true;
						return null;
					});
				} else {
					if (cursorPosition.x == 0) {
						if (cursorPosition.y != 0) {
							int newPosition = getLineLength(cursorPosition.y - 1);
							StyledStringLine upLine = getLine(cursorPosition.y - 1);
							line.forEach(string -> upLine.addWithCombine(string));
							text.deleteLine(cursorPosition.y);
							cursorPosition.setY(cursorPosition.y - 1);
							cursorPosition.setX(newPosition);
						}
					} else {
						formatSelections(Lists.newArrayList(getBackspaceBox()), (l, ss) -> {
							hold.value = true;
							return null;
						});
					}
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

	public boolean checkAndCreateSelection() {
		if (selectPosition.validPosition()) {
			return true;
		}
		if (cursorPosition.validPosition()) {
			selectPosition.setCursor(cursorPosition.x, cursorPosition.y);
			return true;
		}
		return false;
	}

	public void cut() {
		copy();
		formatSelections((line, ss) -> null);
	}

	public void copy() {
		StringBuilder copyText = new StringBuilder();
		formatSelections((line, ss) -> {
			copyText.append(ss.getFormattedString());
			// FIXME way to tell what line it's on
			return ss;
		});
		// FIXME - HOW TO SAVE ALL COLOURS? - STYLED STRING LINE
		setClipboardString(copyText.toString());
	}

	public void paste() {
		String paste = getClipboardString();
		if (!paste.isEmpty()) {
			List<StyledStringLine> lines = getStyledLinesFromString(paste);
			if (!lines.isEmpty()) {
				addStyledLines(lines, true);
			}
		}
	}

	public List<StyledStringLine> getStyledLinesFromString(String string) {
		List<StyledStringLine> lines = new ArrayList<>();
		String[] splits = string.split("\n");
		for (String s : splits) {
			StyledStringLine line = new StyledStringLine(text);
			StyledStringHelper.getStyledStringsFromText(s).forEach(ss -> line.addWithCombine(ss));
			lines.add(line);
		}
		return lines;
	}

	public SonarStyling createStylingFromEnabled() {
		SonarStyling styling = new SonarStyling();
		styling.rgb = currentColour;
		styling.toggleSpecialFormatting(specials, true);
		return styling;
	}

	//// RENDERING \\\\

	public void renderDisplayScreen(float partialTicks, int x, int y) {
		/* double[] scaling = DisplayElementHelper.getScaling(text.getUnscaledWidthHeight(), text.getMaxScaling(), 100); GlStateManager.scale(scaling[2], scaling[2], scaling[2]); text.getHolder().startElementRender(text); pushMatrix(); DisplayElementHelper.align(text.getHolder().getAlignmentTranslation(text)); double scale = text.getActualScaling()[2]; scale(scale, scale, scale); int i = 0; List<TextSelection> allSelection = getAllSelections(); for (StyledStringLine s : text) { text.preRender(s); if (i == cursorPosition.y) { renderCursor(s); } s.render(); renderSelections(allSelection, s, i); text.postRender(s); i++; } popMatrix(); text.getHolder().endElementRender(text); */

		text.getHolder().startElementRender(text);
		pushMatrix();
		DisplayElementHelper.align(text.getHolder().getAlignmentTranslation(text));
		double scale = text.getActualScaling()[2];
		List<TextSelection> allSelection = getAllSelections();

		double[] scaling = DisplayElementHelper.getScaling(text.getUnscaledWidthHeight(), text.getMaxScaling(), 100);
		double max_width = text.getMaxScaling()[0];

		int i = 0;
		for (StyledStringLine s : text) {
			text.preRender(s);
			GlStateManager.pushMatrix();
			double element = s.getStringWidth() * scaling[2];
			if (s.getAlign() == WidthAlignment.CENTERED)
				translate((max_width / 2) - (element / 2), 0, 0);
			if (s.getAlign() == WidthAlignment.RIGHT)
				translate(max_width - element, 0, 0);
			GlStateManager.scale(scaling[2], scaling[2], 1);

			//GlStateManager.disableBlend();
			GlStateManager.disableLighting();
			GlStateManager.color(1F, 1F, 1F, 1F);
			s.render();
			if (i == cursorPosition.y) {
				renderCursor(s);
			}
			renderSelections(allSelection, s, i);
			GlStateManager.scale(1 / scaling[2], 1 / scaling[2], 1);
			GlStateManager.popMatrix();
			GL11.glTranslated(0, s.getStringHeight() * scaling[2], 0);
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
			//GlStateManager.color(1F, 1F, 1F, 1F);
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
			for (IStyledString ss : c.getStrings()) {

				int subEnd = Math.min(index_count + ss.getStringLength(), end) - index_count;
				if (subEnd > 0) {
					if (ss instanceof StyledInfo) {
						i += ss.getStringWidth();
						index_count += 1;
					} else {
						String unformatted = ss.getTextFormattingStyle() + ss.getUnformattedString().substring(0, subEnd);
						i += RenderHelper.fontRenderer.getStringWidth(unformatted);
						index_count += ss.getStringLength();
					}
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
					for (IStyledString ss : line.getStrings()) {
						int selectStart = Math.max(subIndex, start);
						int selectEnd = Math.min(subIndex + ss.getStringLength(), end);
						int subStart = selectStart - subIndex;
						int subEnd = selectEnd - subIndex;

						// DO STYLEDINFo
						if (subStart >= 0 && subStart < subEnd) {
							if (ss instanceof StyledInfo) {
								RenderHelper.drawRect(subWidth + 0, 0, subWidth + ss.getStringWidth(), line.getStringHeight());
							} else {
								String text = ss.getUnformattedString();
								String before = subStart == 0 ? "" : text.substring(0, subStart);
								int beforeWidth = before.isEmpty() ? 0 : RenderHelper.fontRenderer.getStringWidth(ss.getTextFormattingStyle() + before);
								String formatString = text.substring(subStart, subEnd);
								RenderHelper.drawRect(beforeWidth + subWidth, 0, beforeWidth + subWidth + RenderHelper.fontRenderer.getStringWidth(ss.getTextFormattingStyle() + formatString), line.getStringHeight());
							}
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

	//// STYLING \\\\

	public final void changeSelectedColour(TextFormatting colour) {
		setTextColourOnSelected(currentColour = FontHelper.getColourFromFormatting(colour));
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
		if (enabled) {
			enableSpecialFormattingOnSelected(Lists.newArrayList(format));
		} else {
			disableSpecialFormattingOnSelected(Lists.newArrayList(format));
		}
	}

	//// SELECTIONS \\\\

	/** a list of all selections, including what the cursor has currently selected. */
	public List<TextSelection> getAllSelections() {
		List<TextSelection> stored_map = savedSelections;
		boolean hasCursorSelection = cursorPosition.validPosition() && selectPosition.validPosition();
		if (hasCursorSelection) {
			stored_map = new ArrayList<>();
			for (TextSelection s : savedSelections) {
				stored_map.add(new TextSelection(s.startX, s.endX, s.endY, s.endY));
			}
			TextSelection cursorSelection = new TextSelection(cursorPosition, selectPosition);
			TextSelection.addWithCombine(stored_map, cursorSelection);
		}
		return stored_map;
	}

	/** replaced the saved selection list with one containing the cursor position also */
	public List<TextSelection> combineAllSelections() {
		savedSelections = getAllSelections();
		// currentSelection = null;
		return savedSelections;
	}

	/** clears the current saved selections and positions the select position at the beginning of the text and the cursor position at the end of the text */
	public void selectAll() {
		savedSelections.clear();
		selectPosition.setCursor(0, 0);
		cursorPosition.setCursor(getLineLength(getLineCount() - 1), getLineCount() - 1);
	}

	/** removes all saved positions and removes the select position */
	public void deselectAll() {
		savedSelections.clear();
		// cursorPosition.removeCursor(); // keep cursor pos?
		selectPosition.removeCursor();
	}

	/** if all selections would return any selections */
	public boolean hasSelections() {
		return selectPosition.validPosition() || !savedSelections.isEmpty();
	}

	public void addStyledStrings(List<IStyledString> strings) {
		if (cursorPosition.validPosition()) {
			StyledStringLine newLine = new StyledStringLine(text);
			if (cursorPosition.x != 0) {
				TextSelection before = new TextSelection(0, cursorPosition.x, cursorPosition.y, cursorPosition.y);
				formatSelections(Lists.newArrayList(before), (line, ss) -> {
					newLine.addWithCombine(ss);
					return ss;
				});
			}
			strings.forEach(ss -> newLine.addWithCombine(ss));
			if (cursorPosition.x != getLineLength(cursorPosition.y)) {
				TextSelection after = new TextSelection(cursorPosition.x, Integer.MAX_VALUE, cursorPosition.y, cursorPosition.y);
				formatSelections(Lists.newArrayList(after), (line, ss) -> {
					newLine.addWithCombine(ss);
					return ss;
				});
			}
			text.setLine(cursorPosition.y, newLine);
		}
	}

	public void addStyledLines(List<StyledStringLine> lines, boolean combineFirst) {
		if (cursorPosition.validPosition()) {
			int yPos = cursorPosition.y;
			final StyledStringLine beforeLine = new StyledStringLine(text);
			boolean combinedFirst = cursorPosition.x == 0 || !combineFirst;
			if (cursorPosition.x != 0) {
				TextSelection before = new TextSelection(0, cursorPosition.x, cursorPosition.y, cursorPosition.y);
				formatSelections(Lists.newArrayList(before), (line, ss) -> {
					beforeLine.addWithCombine(ss);
					return ss;
				});
				text.addNewLine(yPos, beforeLine);
				yPos++;
			}
			StyledStringLine afterLines = new StyledStringLine(text);
			if (cursorPosition.x != getLineLength(cursorPosition.y)) {
				TextSelection after = new TextSelection(cursorPosition.x, Integer.MAX_VALUE, cursorPosition.y, cursorPosition.y);
				formatSelections(Lists.newArrayList(after), (line, ss) -> {
					afterLines.addWithCombine(ss);
					return ss;
				});
			}

			for (StyledStringLine line : lines) {
				if (!combinedFirst) {
					line.getStrings().forEach(ss -> beforeLine.addWithCombine(ss));
					combinedFirst = true;
				} else {
					text.addNewLine(yPos, line);
					yPos++;
				}
			}
			if (!afterLines.getStrings().isEmpty()) {
				text.addNewLine(yPos, afterLines);
				yPos++;
			}

		}

	}

	/** for every StyledStringCompound */
	public void forCompounds(Consumer<StyledStringLine> format) {
		text.forEach(t -> format.accept(t));
	}

	/** for every StyledString */
	public void forStrings(Consumer<IStyledString> format) {
		text.forEach(t -> t.getStrings().forEach(ss -> format.accept(ss)));
	}

	//// QUICK FORMATTING METHODS \\\\

	public void enableSpecialFormattingOnSelected(List<TextFormatting> formatting) {
		formatSelections((line, ss) -> {
			ss.getStyle().toggleSpecialFormatting(formatting, true);
			ss.onStyleChanged();
			return ss;
		});
	}

	public void disableSpecialFormattingOnSelected(List<TextFormatting> formatting) {
		formatSelections((line, ss) -> {
			ss.getStyle().toggleSpecialFormatting(formatting, false);
			ss.onStyleChanged();
			return ss;
		});
	}

	public void setTextColourOnSelected(int colour) {
		formatSelections((line, ss) -> {
			ss.getStyle().setFontColour(colour);
			ss.onStyleChanged();
			return ss;
		});
	}

	public void deleteAllSelected() {
		formatSelections((line, ss) -> null);
		formatSelectedLines(ss -> ss.getStrings().isEmpty() ? null : ss);
		selectPosition.removeCursor();
		savedSelections.clear();
	}

	//// COMPLEX FORMATTING METHODS \\\\

	public void formatSelections(BiFunction<Integer, IStyledString, IStyledString> action) {
		formatSelections(getAllSelections(), action);
	}

	public void formatSelections(List<TextSelection> selects, BiFunction<Integer, IStyledString, IStyledString> action) {
		for (TextSelection select : selects) {
			for (int y = select.startY; y <= select.endY; y++) {
				StyledStringLine c = getLine(y);
				if (c == null) {
					continue;
				}
				int[] subSelect = select.getSubStringSize(c.getCachedUnformattedString(), y);
				int start = subSelect[0], end = subSelect[1];
				if (start != -1 && end != -1) {
					List<IStyledString> formatted_strings = new ArrayList<>();

					int index_count = 0;
					for (IStyledString ss : c.getStrings()) {
						int subStart = Math.max(index_count, start) - index_count;
						int subEnd = Math.min(index_count + ss.getStringLength(), end) - index_count;

						if (subStart >= 0 && subStart < subEnd) {
							if (ss instanceof StyledInfo) {
								StyledStringHelper.addWithCombine(formatted_strings, action.apply(y, ss.copy()));
							} else {
								String[] subStrings = StyledStringHelper.getSubStrings(subStart, subEnd, ss.getUnformattedString());
								StyledStringHelper.addWithCombine(formatted_strings, new StyledString(subStrings[0], ss.getStyle().copy()));
								StyledStringHelper.addWithCombine(formatted_strings, action.apply(y, new StyledString(subStrings[1], ss.getStyle().copy())));
								StyledStringHelper.addWithCombine(formatted_strings, new StyledString(subStrings[2], ss.getStyle().copy()));
							}
						} else {
							StyledStringHelper.addWithCombine(formatted_strings, ss);
						}
						index_count += ss.getStringLength();
					}

					c.setStrings(formatted_strings);
				}
			}
		}
		// formatSelectedLines(selects, line -> line.getStrings().isEmpty() ? null : line);
		if (text.getLines().isEmpty()) {
			text.addNewLine(0, new StyledStringLine(text, ""));
		}
		GuiActions.UPDATE_TEXT_SCALING.trigger(this);
		text.getHolder().getContainer().updateActualScaling();
	}

	public void formatSelectedLines(Function<StyledStringLine, StyledStringLine> action) {
		formatSelectedLines(getAllSelections(), action);
	}

	public void formatSelectedLines(List<TextSelection> selects, Function<StyledStringLine, StyledStringLine> action) {
		Map<Integer, StyledStringLine> lines = new HashMap<>();
		for (TextSelection select : selects) {
			for (int y = select.startY; y <= select.endY; y++) {
				StyledStringLine c = getLine(y);
				if (c != null) {
					lines.put(y, c);
				}
			}
		}
		List<StyledStringLine> toRemove = new ArrayList<>();
		for (Entry<Integer, StyledStringLine> line : lines.entrySet()) {
			StyledStringLine newLine = action.apply(line.getValue());
			if (newLine == null) {
				toRemove.add(text.getLines().get(line.getKey()));
			} else {
				text.setLine(line.getKey(), newLine);
			}
		}
		for (StyledStringLine line : toRemove) {
			if (line != null) {
				text.getLines().remove(line);
			}
		}
	}

	//// CLICK HELPERS \\\\

	public final int[] getIndexClicked(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> element = c.getClickBoxes(new double[] { 0, 0, 0 }, clickX, clickY);
		if (element != null && element.getFirst() != null && element.getFirst() instanceof StyledTextElement) {
			int[] clicked = ((StyledTextElement) element.getFirst()).getIndexClicked(element.getSecond()[0], element.getSecond()[1]);
			return clicked;
		}
		return null;
	}

	public final Tuple<StyledStringLine, Integer> getLineClicked(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> element = c.getClickBoxes(new double[] { 0, 0, 0 }, clickX, clickY);
		if (element != null && element.getFirst() != null && element.getFirst() instanceof StyledTextElement) {
			return ((StyledTextElement) element.getFirst()).getLineClicked(element.getSecond()[0], element.getSecond()[1]);
		}
		return new Tuple(null, -1);
	}

	public final Tuple<IStyledString, Integer> getStringClicked(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> element = c.getClickBoxes(new double[] { 0, 0, 0 }, clickX, clickY);
		if (element != null && element.getFirst() != null && element.getFirst() instanceof StyledTextElement) {
			return ((StyledTextElement) element.getFirst()).getStringClicked(element.getSecond()[0], element.getSecond()[1]);
		}
		return new Tuple(null, -1);
	}

	public final Tuple<Character, Integer> getCharClicked(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> element = c.getClickBoxes(new double[] { 0, 0, 0 }, clickX, clickY);
		if (element != null && element.getFirst() != null && element.getFirst() instanceof StyledTextElement) {
			return ((StyledTextElement) element.getFirst()).getCharClicked(element.getSecond()[0], element.getSecond()[1]);
		}
		return new Tuple(null, -1);
	}

	//// LINE COUNTING \\\\

	@Override
	public int getLineCount() {
		return text.getLines().size();
	}

	@Override
	public int getLineLength(int line) {
		if (line >= getLineCount() || line < 0) {
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
