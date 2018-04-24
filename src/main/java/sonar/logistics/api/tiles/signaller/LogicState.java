package sonar.logistics.api.tiles.signaller;

//this may need expanding at some point
public enum LogicState {
	TRUE, FALSE, INVALID_KEY, INVALID_OBJECT;

	public boolean getBool() {
		return this == TRUE;
	}

	public static LogicState getState(boolean bool) {
		return bool ? TRUE : FALSE;
	}
}
