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

public class GSINoData extends AbstractGSI<IInfo> {

	public void initButtons(List<GSIButton> buttons) {
		super.initButtons(buttons);
		//buttons.add(new GSIButton(0, 0.0625 * 2, 0.0625 * 2, 4 * 0.0625, 4 * 0.0625, 2, 2, "NO DATA"));
		//buttons.add(new GSIButton(1, 0.0625 * 6, 0.0625 * 2, 4 * 0.0625, 4 * 0.0625, 4, 2, "NO DATA"));
		//buttons.add(new GSIButton(2, 0.0625 * 10, 0.0625 * 2, 4 * 0.0625, 4 * 0.0625, 3, 2, "NO DATA"));
	}

	@Override
	public void renderGSIForeground(IInfo info, InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		super.renderGSIForeground(info, container, displayInfo, width, height, scale, infoPos);
		if (!displayInfo.getFormattedStrings().isEmpty()) {
			InfoRenderer.renderNormalInfo(container.display.getDisplayType(), width, height, scale, displayInfo.getFormattedStrings());
		} else {
			InfoRenderer.renderNormalInfo(container.display.getDisplayType(), width, height, scale, DisplayConstants.formatText(InfoError.noData.error, displayInfo));
		}

	}

}
