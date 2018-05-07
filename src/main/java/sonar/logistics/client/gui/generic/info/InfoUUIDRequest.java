package sonar.logistics.client.gui.generic.info;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.elements.IInfoRequirement;
import sonar.logistics.api.info.InfoUUID;

import java.util.ArrayList;
import java.util.List;

public class InfoUUIDRequest implements IInfoRequirement {

	public GuiScreen screen;
	public List<InfoUUID> info;
	public int requestSize;
	
	public InfoUUIDRequest(GuiScreen screen, int requestSize){
		this.screen = screen;
		this.info = new ArrayList<>();
		this.requestSize = requestSize;
	}

	@Override
	public int getRequired() {
		return requestSize;
	}

	@Override
	public List<InfoUUID> getSelectedInfo() {
		return info;
	}

	@Override
	public void onGuiClosed(List<InfoUUID> selected) {
		if(screen instanceof IInfoUUIDRequirementGui){
			((IInfoUUIDRequirementGui) screen).onInfoUUIDRequirementCompleted(selected);
		}
	}

	@Override
	public void doInfoRequirementPacket(DisplayGSI gsi, EntityPlayer player, List<InfoUUID> require) {}
	
}
