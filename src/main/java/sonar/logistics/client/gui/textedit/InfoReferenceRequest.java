package sonar.logistics.client.gui.textedit;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.elements.IInfoReferenceRequirement;
import sonar.logistics.api.displays.elements.IInfoRequirement;
import sonar.logistics.api.displays.references.InfoReference;
import sonar.logistics.api.info.InfoUUID;

public class InfoReferenceRequest implements IInfoReferenceRequirement {

	public GuiScreen screen;
	public List<InfoReference> info;
	public int requestSize;
	
	public InfoReferenceRequest(GuiScreen screen, int requestSize){
		this(screen, Lists.newArrayList(), requestSize);
	}
	
	public InfoReferenceRequest(GuiScreen screen, List<InfoReference> list, int requestSize){
		this.screen = screen;
		this.info = list;
		this.requestSize = requestSize;
	}

	@Override
	public int getReferencesRequired() {
		return requestSize;
	}

	@Override
	public List<InfoReference> getSelectedReferences() {
		return info;
	}

	@Override
	public void onGuiClosed(List<InfoReference> selected) {
		if(screen instanceof IInfoReferenceRequirementGui){
			((IInfoReferenceRequirementGui) screen).onRequirementCompleted(selected);
		}
	}
	
}
