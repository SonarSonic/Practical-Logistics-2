package sonar.logistics.client.gui.textedit;

import java.util.function.Consumer;

public enum CompoundActions implements IStyledCompoundAction, Consumer<StyledStringCompound> {


	public IStyledCompoundAction action;

	CompoundActions(IStyledCompoundAction action) {
		this.action = action;
	}

	@Override
	public void trigger(StyledStringCompound trigger) {
		action.trigger(trigger);
	}

	@Override
	public void accept(StyledStringCompound t) {
		trigger(t);
	}

}
