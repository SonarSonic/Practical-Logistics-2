package sonar.logistics.client.gsi.info;

import java.util.List;

import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.render.IDisplayInfo;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.tiles.displays.DisplayConstants;
import sonar.logistics.client.gsi.AbstractGSI;
import sonar.logistics.client.gsi.GSIButton;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.InfoError;

public class GSIBasicInfo extends AbstractGSI<IInfo> {

	@Override
	public void renderGSIForeground(IInfo info, InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		super.renderGSIForeground(info, container, displayInfo, width, height, scale, infoPos);
		InfoRenderer.renderNormalInfo(container.display.getDisplayType(), width, height, scale, displayInfo.getFormattedStrings());

	}

}
