package sonar.logistics.base.requests.reference;

import net.minecraft.client.gui.GuiScreen;
import sonar.logistics.core.tiles.displays.info.references.InfoReference;

import java.util.ArrayList;
import java.util.List;

public class InfoReferenceRequest implements IInfoReferenceRequirement {

	public GuiScreen screen;
	public List<InfoReference> info;
	public int requestSize;
	
	public InfoReferenceRequest(GuiScreen screen, int requestSize){
		this(screen, new ArrayList<>(), requestSize);
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
			((IInfoReferenceRequirementGui) screen).onReferenceRequirementCompleted(selected);
		}
	}
	
}
