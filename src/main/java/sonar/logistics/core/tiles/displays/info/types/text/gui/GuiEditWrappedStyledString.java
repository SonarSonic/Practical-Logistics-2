package sonar.logistics.core.tiles.displays.info.types.text.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Tuple;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.core.tiles.displays.info.elements.DisplayElementHelper;
import sonar.logistics.core.tiles.displays.info.elements.base.IDisplayElement;
import sonar.logistics.core.tiles.displays.info.types.text.StyledTextElement;
import sonar.logistics.core.tiles.displays.info.types.text.StyledWrappedTextElement;
import sonar.logistics.core.tiles.displays.info.types.text.styling.IStyledString;
import sonar.logistics.core.tiles.displays.info.types.text.styling.StyledString;
import sonar.logistics.core.tiles.displays.info.types.text.styling.StyledStringLine;
import sonar.logistics.core.tiles.displays.info.types.text.utils.StyledStringRenderer;
import sonar.logistics.core.tiles.displays.info.types.text.utils.StyledStringRenderer.SimpleIndex;
import sonar.logistics.core.tiles.displays.info.types.text.utils.TextSelection;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static net.minecraft.client.renderer.GlStateManager.*;

public class GuiEditWrappedStyledString extends GuiEditStyledStrings {

	public GuiEditWrappedStyledString(StyledWrappedTextElement text, TileAbstractDisplay display) {
		super(text, display);
	}

	public void renderDisplayScreen(float partialTicks, int x, int y) {
		text.updateRender();
		text.getHolder().startElementRender(text);
		pushMatrix();
		DisplayElementHelper.align(text.getHolder().getAlignmentTranslation(text));
		double scale = text.getActualScaling()[DisplayElementHelper.SCALE];
		scale(scale, scale, scale);

		GlStateManager.scale(text.textScale / 100D, text.textScale / 100D, 1D);

		int startLine = 0;
		int endLine = ((StyledWrappedTextElement)text).handler.linesPerPage;
		Map<Integer, List<SimpleIndex>> page = ((StyledWrappedTextElement)text).handler.lines;
		// Pair<IStyledString, Integer> cursor = null;

		StyledStringLine cursor_line = text.getLine(cursorPosition.y);
		// if (cursorPosition.validPosition()) {
		// if (c != null) {
		// cursor = StyledStringHelper.indexStyledLine(c, cursorPosition.x-1);
		// }
		// }
		List<TextSelection> select = this.getAllSelections();
		if (page != null && !page.isEmpty()) {
			StyledStringRenderer.instance().setRenderPositions(0, 0);
			for (Entry<Integer, List<SimpleIndex>> line : page.entrySet()) {
				if (line.getKey() >= startLine && line.getKey() < endLine) {
					for (SimpleIndex index : line.getValue()) {
						String s = index.string.getUnformattedString().substring(index.start, index.end);
						StyledStringRenderer.instance().startStyledStringRender(index.string);
						List<TextSelection> selections = new ArrayList<>();
						int line_index = text.getLineIndex(index.string.getLine());
						if (!select.isEmpty()) {
							for (TextSelection validS : select) {
								if (validS.startY <= line_index && validS.endY >= line_index) {
									selections.add(validS);
								}
							}
						}
						for (int i = 0; i < s.length(); ++i) {

							double posX = StyledStringRenderer.instance().getPosX();
							char current_char = s.charAt(i);
							StyledStringRenderer.instance().renderChar(index.string, current_char);
							double char_move = StyledStringRenderer.instance().getPosX() - posX;
							int actualIndex = index.lineIndex + 1 + i + index.start;
							if (cursor_line != null && cursor_line == index.string.getLine() && ((cursorPosition.x == 0 && actualIndex == 0) || (cursorPosition.x == actualIndex))) {
								GlStateManager.translate(StyledStringRenderer.instance().getPosX(), StyledStringRenderer.instance().getPosY(), 0);
								renderCursorAtPosition();
								GlStateManager.translate(-StyledStringRenderer.instance().getPosX(), -StyledStringRenderer.instance().getPosY(), 0);
							}

							int value = index.start + i + index.lineIndex;
							for (TextSelection toRender : selections) {
								if ((toRender.startY != line_index || toRender.startX <= value) && (toRender.endY != line_index || toRender.endX > value)) {
									GlStateManager.disableTexture2D();
									GlStateManager.enableBlend();
									GlStateManager.color(1F, 1F, 1F, 0.5F);
									GlStateManager.disableTexture2D();
									GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
									RenderHelper.drawRect(StyledStringRenderer.instance().getPosX() + (float) -char_move, StyledStringRenderer.instance().getPosY(), StyledStringRenderer.instance().getPosX(), StyledStringRenderer.instance().getPosY() + StyledStringRenderer.instance().FONT_HEIGHT);
									GlStateManager.disableBlend();
									GlStateManager.enableTexture2D();
									// reset colour
									StyledStringRenderer.instance().startStyledStringRender(index.string);
									break;
								}
							}
						}
					}
					StyledStringRenderer.instance().setRenderPositions(0, StyledStringRenderer.instance().getPosY() + ((StyledWrappedTextElement)text).handler.totalLineSize());
				}
			}
		}

		GlStateManager.scale(1D / (text.textScale / 100D), 1D / (text.textScale / 100D), 1D / 1D);
		popMatrix();
		text.getHolder().endElementRender(text);
	}


	public int[] getDragPositionFromContainerXY(double clickX, double clickY) {
		Tuple<SimpleIndex, Integer> string = ((StyledWrappedTextElement)text).getStringClicked(clickX, clickY);
		Tuple<Character, Integer> charClicked = getCharClicked(clickX, clickY);

		if (string.getFirst() != null) {
			int xPos = string.getFirst().lineIndex + string.getFirst().start + charClicked.getSecond();
			int yPos = text.getLineIndex(string.getFirst().string.getLine());
			return new int[]{xPos, yPos};
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
		int yPosition = Math.min(text.getLineCount() - 1, line.getSecond() == -1 ? text.getLineCount() - 1 : line.getSecond());
		int xPosition = string.getSecond() == StyledTextElement.AFTER || string.getSecond() == -1 ? text.getLineLength(yPosition) : 0;
		if (yPosition < 0) {
			return new int[] { 0, 0 };
		}
		return new int[] { xPosition, yPosition };
	}



	public int[] getIndexClicked(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> element = c.getClickBoxes(new double[] { 0, 0, 0 }, clickX, clickY);
		if (element != null && element.getFirst() != null && element.getFirst() instanceof StyledWrappedTextElement) {
			int[] clicked = ((StyledWrappedTextElement) element.getFirst()).getIndexClicked(element.getSecond()[0], element.getSecond()[1]);
			return clicked;
		}
		return null;
	}

	public Tuple<StyledStringLine, Integer> getLineClicked(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> element = c.getClickBoxes(new double[] { 0, 0, 0 }, clickX, clickY);
		if (element != null && element.getFirst() != null && element.getFirst() instanceof StyledWrappedTextElement) {
			Tuple<SimpleIndex, Integer> string = ((StyledWrappedTextElement) element.getFirst()).getStringClicked(element.getSecond()[0], element.getSecond()[1]);
			if (string != null && string.getFirst() != null) {
				return new Tuple(string.getFirst().string.getLine(), text.getLineIndex(string.getFirst().string.getLine()));
			}
		}
		return new Tuple(null, -1);
	}

	public Tuple<IStyledString, Integer> getStringClicked(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> element = c.getClickBoxes(new double[] { 0, 0, 0 }, clickX, clickY);
		if (element != null && element.getFirst() != null && element.getFirst() instanceof StyledWrappedTextElement) {

			Tuple<SimpleIndex, Integer> string = ((StyledWrappedTextElement) element.getFirst()).getStringClicked(element.getSecond()[0], element.getSecond()[1]);
			if (string != null && string.getFirst() != null) {
				return new Tuple(string.getFirst().string, string.getSecond());
			}

		}
		return new Tuple(null, -1);
	}

	public Tuple<Character, Integer> getCharClicked(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> element = c.getClickBoxes(new double[] { 0, 0, 0 }, clickX, clickY);
		if (element != null && element.getFirst() != null && element.getFirst() instanceof StyledWrappedTextElement) {
			return ((StyledWrappedTextElement) element.getFirst()).getCharClicked(element.getSecond()[0], element.getSecond()[1]);
		}
		return new Tuple(null, -1);
	}
}
