package sonar.logistics.api.register;

public class InvField {

	public String key;
	public Integer value;
	public RegistryType type;

	public InvField(String key, Integer value, RegistryType type) {
		this.key = key;
		this.value = value;
		this.type = type;
	}
}
