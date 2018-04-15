package sonar.logistics.client.gui.textedit;

import sonar.logistics.api.displays.elements.text.StyledStringLine;
import sonar.logistics.api.displays.elements.text.StyledTextElement;

public class CursorPosition {

	public static CursorPosition newInvalid() {
		return new CursorPosition(-1, -1);
	};

	public int x, y;

	public CursorPosition(int[] pos) {
		setCursor(pos);
	}

	public CursorPosition(int x, int y) {
		setCursor(x, y);
	}

	public boolean validPosition() {
		return this.x != -1 && this.y != -1;
	}

	public void removeCursor() {
		setCursor(-1, -1);
	}

	public void setCursor(int[] set) {
		if (set == null) {
			removeCursor();
		} else {
			setCursor(set[0], set[1]);
		}
	}

	public void setCursor(int x, int y) {
		setX(x);
		setY(y);
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setYToFirst() {
		this.y = 0;
	}

	public void setXToFirst() {
		this.x = 0;
	}

	public void setYToLast(StyledTextElement text) {
		this.y = text.getLineCount() - 1;
	}

	public void setXToLast(StyledTextElement text) {
		this.x = y == -1 ? -1 : text.getLineLength(y);
	}

	public void moveX(StyledTextElement text, int moveX) {
		if (x == -1 || y == -1) {
			removeCursor();
			return;
		}
		int length = text.getLineLength(y);
		int newIndex = x + moveX;
		if (newIndex > length) {
			int oldY = y;
			moveY(text, 1);
			if (y != oldY)
				setXToFirst();
			return;
		}
		if (newIndex < 0) {
			int oldY = y;
			moveY(text, -1);
			if (y != oldY)
				setXToLast(text);
			return;
		}
		x = newIndex < 0 ? 0 : newIndex;
	}

	public void moveY(StyledTextElement text, int moveY) {
		int newY = Math.min(text.getLineCount() - 1, y + moveY);
		y = newY < 0 ? 0 : newY;
	}

	public StyledStringLine getTypingLine(StyledTextElement text) {
		return !validPosition() ? null : text.getLine(y);
	}

	public int getTypingIndex(StyledTextElement text) {
		return Math.min(x, text.getLineLength(y));
	}
}
