package sonar.logistics.api.errors;

import java.util.List;

import com.google.common.collect.Lists;

import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.networking.ServerInfoHandler;

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
