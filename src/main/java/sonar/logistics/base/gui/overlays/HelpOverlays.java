package sonar.logistics.base.gui.overlays;

import sonar.core.client.gui.GuiHelpOverlay;
import sonar.core.client.gui.HelpOverlay;
import sonar.logistics.core.tiles.readers.info.GuiInfoReader;

import java.awt.*;

public class HelpOverlays {

	public static GuiHelpOverlay<GuiInfoReader> infoReader = new GuiHelpOverlay<GuiInfoReader>() {

		{
			this.overlays.add(new HelpOverlay<GuiInfoReader>("select channel", 7, 5, 20, 19, Color.RED.getRGB()) {
				public boolean isCompletedSuccess(GuiInfoReader gui) {
                    return !gui.part.getChannels().hasChannels();
                }

				public boolean canBeRendered(GuiInfoReader gui) {
					return true;
				}
			});
			this.overlays.add(new HelpOverlay<GuiInfoReader>("guide.Hammer.name", 4, 26, 231, 137, Color.RED.getRGB()) {
				public boolean isCompletedSuccess(GuiInfoReader gui) {
                    return gui.part.getSelectedInfo().get(0) != null;
                }

				public boolean canBeRendered(GuiInfoReader gui) {
					return true;
				}
			});
		}

	};
	
}
