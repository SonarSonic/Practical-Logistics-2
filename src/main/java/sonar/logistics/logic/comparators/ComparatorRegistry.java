package sonar.logistics.logic.comparators;

import sonar.core.helpers.RegistryHelper;

public class ComparatorRegistry extends RegistryHelper<ILogicComparator> {

	@Override
	public void register() {
		registerObject(new BooleanComparator());
		registerObject(new NumberComparator());
		registerObject(new ObjectComparator());
	}

	@Override
	public String registeryType() {
		return "Logic Comparator";
	}

}
