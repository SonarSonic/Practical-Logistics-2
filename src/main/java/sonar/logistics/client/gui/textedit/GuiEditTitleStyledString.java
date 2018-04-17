package sonar.logistics.client.gui.textedit;

import net.minecraft.util.Tuple;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.text.IStyledString;
import sonar.logistics.api.displays.elements.text.StyledString;
import sonar.logistics.api.displays.elements.text.StyledStringLine;
import sonar.logistics.api.displays.elements.text.StyledTextElement;
import sonar.logistics.api.displays.elements.text.StyledTitleElement;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;

public class GuiEditTitleStyledString extends GuiEditStyledStrings {
	
	
	public GuiEditTitleStyledString(StyledTextElement text, TileAbstractDisplay display) {
		super(text, display);
		// TODO Auto-generated constructor stub
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
		int yPosition = Math.min(text.getLineCount() - 1, line.getSecond() == -1 ? text.getLineCount() - 1 : line.getSecond());
		int xPosition = string.getSecond() == StyledTextElement.AFTER || string.getSecond() == -1 ? text.getLineLength(yPosition) : 0;
		if (yPosition < 0) {
			return new int[] { 0, 0 };
		}
		return new int[] { xPosition, yPosition };
	}

	//// CLICK HELPERS \\\\
	public int[] getIndexClicked(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> element = c.getClickBoxes(new double[] { 0, 0, 0 }, clickX, clickY);
		if (element != null && element.getFirst() != null && element.getFirst() instanceof StyledTitleElement) {
			int[] clicked = ((StyledTitleElement) element.getFirst()).getIndexClicked(element.getSecond()[0], element.getSecond()[1]);
			return clicked;
		}
		return null;
	}

	public Tuple<StyledStringLine, Integer> getLineClicked(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> element = c.getClickBoxes(new double[] { 0, 0, 0 }, clickX, clickY);
		if (element != null && element.getFirst() != null && element.getFirst() instanceof StyledTitleElement) {
			return ((StyledTitleElement) element.getFirst()).getLineClicked(element.getSecond()[0], element.getSecond()[1]);
		}
		return new Tuple(null, -1);
	}

	public Tuple<IStyledString, Integer> getStringClicked(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> element = c.getClickBoxes(new double[] { 0, 0, 0 }, clickX, clickY);
		if (element != null && element.getFirst() != null && element.getFirst() instanceof StyledTitleElement) {
			return ((StyledTitleElement) element.getFirst()).getStringClicked(element.getSecond()[0], element.getSecond()[1]);
		}
		return new Tuple(null, -1);
	}

	public Tuple<Character, Integer> getCharClicked(double clickX, double clickY) {
		Tuple<IDisplayElement, double[]> element = c.getClickBoxes(new double[] { 0, 0, 0 }, clickX, clickY);
		if (element != null && element.getFirst() != null && element.getFirst() instanceof StyledTitleElement) {
			return ((StyledTitleElement) element.getFirst()).getCharClicked(element.getSecond()[0], element.getSecond()[1]);
		}
		return new Tuple(null, -1);
	}	

}
