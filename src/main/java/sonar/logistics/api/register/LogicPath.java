package sonar.logistics.api.register;

import java.util.ArrayList;

import com.google.common.collect.Lists;

public class LogicPath {
	public ArrayList path = Lists.newArrayList();
	public Class<?> start;
	public Object startObj;
	public RegistryType type;

	public void setRegistryType(RegistryType regType) {
		type = regType;
	}

	public void setStart(Object obj) {
		start = obj.getClass();
		startObj = obj;
	}

	public void addObject(Object obj) {
		path.add(obj);
	}

	public Object getStart(Object... available) {
		for (Object arg : available) {
			if (start.isInstance(arg)) {
				return arg;
			}
		}
		return start; // lets hope this doesn't happen to often :P
	}

	public LogicPath dupe() {
		LogicPath path = new LogicPath();
		path.path = (ArrayList) this.path.clone();
		path.start = start;
		path.startObj = startObj;
		return path;
	}
}
