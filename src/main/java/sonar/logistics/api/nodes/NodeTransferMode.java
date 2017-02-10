package sonar.logistics.api.nodes;

import net.minecraft.util.IStringSerializable;

public enum NodeTransferMode implements IStringSerializable {
	ADD, REMOVE, ADD_REMOVE, PASSIVE;

	public boolean shouldRemove() {
		return this == REMOVE || this == ADD_REMOVE;
	}

	public boolean shouldAdd() {
		return this == ADD || this == ADD_REMOVE;
	}

	public boolean isPassive() {
		return this == PASSIVE;
	}

	public boolean matches(NodeTransferMode type) {

		return type.shouldRemove() == shouldRemove() || type.shouldAdd() == shouldAdd();
	}

	@Override
	public String getName() {
		return name().toLowerCase();
	}
}
