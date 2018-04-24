package sonar.logistics.api.errors;

import java.util.List;

import sonar.logistics.api.displays.DisplayGSI;

public class ErrorHelper {

	public static void addError(DisplayGSI gsi, IInfoError newError){
		if(gsi.getWorld().isRemote){
			return;
		}
		List<IInfoError> errors = gsi.getCurrentErrors();
		for (IInfoError error : errors) {
			if(error.canCombine(newError)){
				error.addError(newError);
				gsi.sendInfoContainerPacket();
				return;
			}
		}
		gsi.addInfoError(newError);
	}
	
	public static void removeError(DisplayGSI gsi, IInfoError toRemove){
		if(gsi.getWorld().isRemote){
			return;
		}
		List<IInfoError> errors = gsi.getCurrentErrors();
		for (IInfoError error : errors) {
			if(error.canCombine(toRemove)){
				error.removeError(toRemove);
				if(!error.isValid()){
					gsi.removeInfoError(error);
				}
				return;
			}
		}
	}

}
