package sonar.logistics.api.displays.elements.text;

import sonar.logistics.client.gui.textedit.CursorPosition;

import java.util.List;

public class TextSelection {

	public int startY, endY; // line index
	public int startX;
	public int endX;

	public TextSelection(int startX, int endX, int startY, int endY) {
		this.startX = startX;
		this.endX = endX;
		this.startY = startY;
		this.endY = endY;
	}

	public TextSelection(CursorPosition cursorPosition, CursorPosition selectPosition) {
		if (cursorPosition.y == selectPosition.y) {
			this.startY = cursorPosition.y;
			this.endY = cursorPosition.y;
			this.startX = Math.min(cursorPosition.x, selectPosition.x);
			this.endX = Math.max(cursorPosition.x, selectPosition.x);
		} else if (cursorPosition.y < selectPosition.y) {
			this.startY = cursorPosition.y;
			this.endY = selectPosition.y;
			this.startX = cursorPosition.x;
			this.endX = selectPosition.x;
		} else {
			this.startY = selectPosition.y;
			this.endY = cursorPosition.y;
			this.startX = selectPosition.x;
			this.endX = cursorPosition.x;
		}
	}

	public boolean canCombine(TextSelection selection) {
		return canCombine(selection.startX, selection.endX, selection.startY, selection.endY);
	}

	public boolean canCombine(int sX, int eX, int sY, int eY) {
		return check(startY, endY, sY, eY) && check(startX, endX, sX, eX);
	}

	public static boolean check(int start, int end, int s, int e) {

		if (start == s || start == e || end == s || end == e) {

			return true; // if they start or finish on the same points combine them
		}
		if (s > start && s < end) {

			return true;
		}

		if (e > start && e < end) {

			return true;
		}

		if (start > s && start < e) {

			return true;
		}

        return end > s && end < e;

    }

	public void combine(TextSelection selection) {
		combine(selection.startX, selection.endX, selection.startY, selection.endY);
	}

	public void combine(int sX, int eX, int sY, int eY) {
		if (sY == startY) {
			startX = Math.min(startX, sX);
		} else if (sY < startY) {
			startX = sX;
		}
		if (eY == endY) {
			endX = Math.max(endX, eX);
		} else if (sY > endX) {
			endX = eX;
		}
	}

	public int getStartX() {
		return startX;
	}

	public int getEndX() {
		return endX;
	}

	public int getStartY() {
		return startY;
	}

	public int getEndY() {
		return endY;
	}

	public int[] getSubStringSize(String string, int yPos) {
		if (!(getStartY() <= yPos && getEndY() >= yPos)) {
			return new int[] { -1, -1 }; // invalid
		}
		int length = string.length();
		int start = 0;
		int end = length;
		if (yPos == getStartY()) {
			start = Math.min(length, getStartX());
		}
		if (yPos == getEndY()) {
			end = Math.min(length, getEndX());
		}
		if (start == end) { //no substring selection
			return new int[] { -1, -1 };
		}
		return new int[] { start, end };
	}

	public static void addWithCombine(List<TextSelection> selections, TextSelection select) {
		if (selections.isEmpty()) {
			selections.add(select);
			return;
		} else {
			for (TextSelection ss : selections) {
				if (ss.canCombine(select)) {
					ss.combine(select);
					return;
				}
			}
			selections.add(select);

		}
	}

}