package sonar.logistics.base.statements.comparators;

import sonar.logistics.PL2ASMLoader;

public class ComparatorHelper {

	public static ILogicComparator DEFAULT = new ObjectComparator();
	
	public static ILogicComparator getComparator(String name) {
		ILogicComparator comparator = PL2ASMLoader.comparatorClasses.get(name);
		return comparator == null ? DEFAULT : comparator;
	}

}
