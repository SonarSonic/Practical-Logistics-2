package sonar.logistics.api.wireless;

import sonar.core.helpers.FontHelper;

public enum DataEmitterSecurity {
	PUBLIC,
	PRIVATE;
	
	public String getClientName(){
		switch(this){
		case PUBLIC:
			return FontHelper.translate("pl.emitter.public");
		case PRIVATE:
			return FontHelper.translate("pl.emitter.private");
		}
		return "ERROR";
	}
}
