package sonar.logistics.api.tiles.signaller;

public enum InputTypes {
	STRING("obj"), BOOLEAN("bool"), NUMBER("num"), INFO("obj");

	public String comparatorID;

	InputTypes(String comparatorID) {
		this.comparatorID = comparatorID;
	}

	public boolean usesInfo() {
		return this == INFO;
	}
}
