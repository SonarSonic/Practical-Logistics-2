package sonar.logistics.client.gsi.info;

import java.util.List;

import net.minecraft.util.EnumHand;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.render.RenderInfoProperties;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.gsi.AbstractGSI;
import sonar.logistics.client.gsi.GSIButton;
import sonar.logistics.client.gsi.GSIClickPacketHelper;
import sonar.logistics.client.gsi.GSIClickPackets;
import sonar.logistics.helpers.InfoRenderer;

public class GSINoData extends AbstractGSI<IInfo> {

	public void initButtons(List<GSIButton> buttons) {
		super.initButtons(buttons);
		if (!InfoUUID.valid(renderInfo.getInfoUUID())) {
			RenderInfoProperties props = renderInfo.getRenderProperties();
			double p = 0.0625;
			double bSize = Math.min(Math.min(props.scaling[0] - p, props.scaling[1] - p), p * 8);
			buttons.add(new GSIButton(0, (props.scaling[0] / 2) - ((bSize / 2) - p), (props.scaling[1] / 2) - ((bSize / 2) - p), bSize, bSize, 2, 15, "Select Info Source"));
		}
		// buttons.add(new GSIButton(1, 0.0625 * 6, 0.0625 * 2, 4 * 0.0625, 4 * 0.0625, 4, 2, "POTATO"));
		// buttons.add(new GSIButton(2, 0.0625 * 10, 0.0625 * 2, 4 * 0.0625, 4 * 0.0625, 3, 2, "SAVE ME"));
	}

	@Override
	public void renderGSIForeground(IInfo info, InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		super.renderGSIForeground(info, container, displayInfo, width, height, scale, infoPos);
		if (InfoUUID.valid(renderInfo.getInfoUUID())) {
			InfoRenderer.renderNormalInfo(width, height, scale, "WAITING FOR SERVER");
		}
		/* if (!displayInfo.getFormattedStrings().isEmpty()) { InfoRenderer.renderNormalInfo(width, height, scale, displayInfo.getFormattedStrings()); } else { InfoRenderer.renderNormalInfo(width, height, scale, DisplayConstants.formatText(InfoError.noData.error, displayInfo)); } */

	}

	@Override
	public void onButtonClicked(IInfo info, GSIButton button, DisplayScreenClick click, EnumHand hand) {
		switch (button.buttonID) {
		case 0:
			sendGSIPacket(GSIClickPacketHelper.createBasicPacket(GSIClickPackets.SOURCE_BUTTON), info, click, hand);
			break;
		default:
			return;
		}

	}
}
