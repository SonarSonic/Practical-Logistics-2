package sonar.logistics.base.guidance.errors;

import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayGSISaveHandler;

import java.util.List;

public class ErrorHelper {

	public static void addError(DisplayGSI gsi, IInfoError newError){
		if(gsi.getWorld().isRemote){
			return;
		}
		List<IInfoError> errors = gsi.getCurrentErrors();
		for (IInfoError error : errors) {
			if(error.canCombine(newError)){
				error.addError(newError);
				gsi.sendInfoContainerPacket(DisplayGSISaveHandler.DisplayGSISavedData.ERRORS);
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
