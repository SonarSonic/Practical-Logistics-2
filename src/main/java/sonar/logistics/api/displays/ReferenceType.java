package sonar.logistics.api.displays;

import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.info.ISuffixable;

public enum ReferenceType {
	INFO_TYPE, OBJECT_TYPE, IDENTIFIER, CLIENT_INFO, RAW_INFO, SUFFIX, PREFIX;

	public String getRefString(IInfo info) {
		if (info instanceof INameableInfo) {
			INameableInfo name = (INameableInfo) info;
			switch (this) {
			case IDENTIFIER:
				return name.getClientIdentifier();
			case INFO_TYPE:
				return name.getClientType();
			case OBJECT_TYPE:
				break;
			case CLIENT_INFO:
			default:
				break;
			}
		}
		if (info instanceof ISuffixable) {
			ISuffixable suffix = (ISuffixable) info;
			switch (this) {
			case PREFIX:
				return suffix.getPrefix();
			case RAW_INFO:
				return suffix.getRawData();
			case SUFFIX:
				return suffix.getSuffix();
			default:
				break;
			}
		}

		return "";
	}
}