package sonar.logistics.helpers;

import sonar.logistics.PL2ASMLoader;
import sonar.logistics.api.tiles.signaller.ComparableObject;
import sonar.logistics.logic.comparators.ILogicComparator;
import sonar.logistics.logic.comparators.ObjectComparator;

public class ComparatorHelper {

	public static ILogicComparator DEFAULT = new ObjectComparator();
	
	public static ILogicComparator getComparator(String name) {
		ILogicComparator comparator = PL2ASMLoader.comparatorClasses.get(name);
		return comparator == null ? DEFAULT : comparator;
	}

}
