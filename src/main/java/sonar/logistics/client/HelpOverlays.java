package sonar.logistics.client;

import java.awt.Color;

import sonar.core.client.gui.GuiHelpOverlay;
import sonar.core.client.gui.HelpOverlay;
import sonar.logistics.client.gui.GuiInfoReader;

public class HelpOverlays {

	public static GuiHelpOverlay<GuiInfoReader> infoReader = new GuiHelpOverlay<GuiInfoReader>() {

		{
			this.overlays.add(new HelpOverlay<GuiInfoReader>("select channel", 7, 5, 20, 19, Color.RED.getRGB()) {
				public boolean isCompletedSuccess(GuiInfoReader gui) {
					if (!gui.part.getChannels().isEmpty()) {
						return true;
					}
					return false;
				}

				public boolean canBeRendered(GuiInfoReader gui) {
					return true;
				}
			});
			this.overlays.add(new HelpOverlay<GuiInfoReader>("guide.Hammer.name", 4, 26, 231, 137, Color.RED.getRGB()) {
				public boolean isCompletedSuccess(GuiInfoReader gui) {
					if (gui.part.getSelectedInfo().get(0) != null) {
						return true;
					}
					return false;
				}

				public boolean canBeRendered(GuiInfoReader gui) {
					return true;
				}
			});
		}

	};
	
}
