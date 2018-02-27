package sonar.logistics.client.gui.textedit;

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
		setCursor(set[0], set[1]);
	}

	public void setCursor(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void moveX(ILineCounter counter, int moveX) {
		if (x == -1 || y == -1) {
			removeCursor();
			return;
		}
		int length = counter.getLineLength(y);
		int newIndex = x + moveX;
		if (newIndex > length) {
			moveY(counter, 1);
			return;
		}
		if (newIndex < 0) {
			moveY(counter, -1);
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
