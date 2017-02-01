package sonar.logistics.api.nodes;

public enum NodeSetting {
	FULL, ADD_REMOVE, ADD, REMOVE, READ, NONE;

	public boolean canRead() {
		return this != NONE;
	}

	public boolean canAdd() {
		return this == FULL || this == ADD_REMOVE || this == ADD;
	}

	public boolean canRemove() {
		return this == FULL || this == ADD_REMOVE || this == REMOVE;
	}

	public boolean canTransfer(NodeTransferMode mode) {
		switch (mode) {
		case ADD:
			return canAdd();
		case REMOVE:
			return canRemove();
		default:
			break;
		}
		return false;
	}
}
