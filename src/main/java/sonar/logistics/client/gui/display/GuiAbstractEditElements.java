package sonar.logistics.client.gui.display;

import com.google.common.collect.Lists;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.storage.DisplayElementContainer;
import sonar.logistics.client.gsi.GSIElementPacketHelper;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;

import java.util.List;

public class GuiAbstractEditElements extends GuiAbstractEditContainer {

	public List<IDisplayElement> editing;

	public GuiAbstractEditElements(IDisplayElement editing, DisplayElementContainer c, TileAbstractDisplay display) {
		this(Lists.newArrayList(editing), c, display);
	}

	public GuiAbstractEditElements(List<IDisplayElement> editing, DisplayElementContainer c, TileAbstractDisplay display) {
		super(c, display);
		this.editing = editing;
	}

	public void renderDisplayScreen(float partialTicks, int x, int y) {
		editing.forEach(e -> DisplayElementHelper.renderElementInHolder(e.getHolder(), e));
	}

	public void save() {
		super.save();
		if (!(this.origin instanceof GuiUnconfiguredInfoElement))
			editing.forEach(e -> GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createEditElementPacket(e), e.getElementIdentity(), e.getGSI()));
	}
}
