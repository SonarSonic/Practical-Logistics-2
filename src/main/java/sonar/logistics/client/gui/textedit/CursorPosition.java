package sonar.logistics.client.gui.textedit;

import sonar.logistics.api.displays.elements.text.StyledStringLine;

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

	public void setYToLast(ILineCounter counter) {
		this.y = counter.getLineCount() - 1;
	}

	public void setXToLast(ILineCounter counter) {
		this.x = y == -1 ? -1 : counter.getLineLength(y);
	}

	public void moveX(ILineCounter counter, int moveX) {
		if (x == -1 || y == -1) {
			removeCursor();
			return;
		}
		int length = counter.getLineLength(y);
		int newIndex = x + moveX;
		if (newIndex > length) {
			int oldY = y;
			moveY(counter, 1);
			if (y != oldY)
				setXToFirst();
			return;
		}
		if (newIndex < 0) {
			int oldY = y;
			moveY(counter, -1);
			if (y != oldY)
				setXToLast(counter);
			return;
		}
		x = newIndex < 0 ? 0 : newIndex;
	}

	public void moveY(ILineCounter counter, int moveY) {
		int newY = Math.min(counter.getLineCount() - 1, y + moveY);
		y = newY < 0 ? 0 : newY;
	}

	public StyledStringLine getTypingLine(ILineCounter counter) {
		return !validPosition() ? null : counter.getLine(y);
	}

	public int getTypingIndex(ILineCounter counter) {
		return Math.min(x, counter.getLineLength(y));
	}
}
