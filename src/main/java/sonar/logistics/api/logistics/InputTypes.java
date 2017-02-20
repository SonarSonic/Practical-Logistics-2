package sonar.logistics.api.logistics;

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
