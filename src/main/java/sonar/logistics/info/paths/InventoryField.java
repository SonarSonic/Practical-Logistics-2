package sonar.logistics.info.paths;

import sonar.logistics.api.info.register.RegistryType;

public class InventoryField {

	public String key;
	public Integer value;
	public RegistryType type;

	public InventoryField(String key, Integer value, RegistryType type) {
		this.key = key;
		this.value = value;
		this.type = type;
	}
}
