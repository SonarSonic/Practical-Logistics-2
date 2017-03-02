package sonar.logistics.client;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.Gui;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.SonarHelper;
import sonar.core.utils.Pair;

public class GuiHelpOverlay<T extends GuiSonar> extends Gui {

	public static boolean enableHelp;

	public HelpOverlay<T> current;
	public ArrayList<HelpOverlay<T>> overlays = new ArrayList<HelpOverlay<T>>();

	public HelpOverlay<T> getCurrentOverlay() {
		return current;
	}

	public void initGui(T gui) {
		Pair<Integer, HelpOverlay<T>> overlay = getValidOverlay(gui);
		current = overlay.b;
	}

	public void drawOverlay(T gui, int x, int y) {
		if (current != null && enableHelp) {
			drawHorizontalLine(current.left + 1, current.left - 2 + current.width, current.top + 1, current.colour);
			drawHorizontalLine(current.left + 1, current.left - 2 + current.width, current.top + current.height - 1, current.colour);
			drawVerticalLine(current.left + 1, current.top + 1, current.top + current.height - 1, current.colour);
			drawVerticalLine(current.left - 2 + current.width, current.top + 1, current.top + current.height - 1, current.colour);

			if (isMouseOver(gui, x, y)) {
				gui.drawSpecialToolTip(current.description, x, y, null);
			}
		}
	}

	public void mouseClicked(T gui, int x, int y, int button) {
		if (enableHelp && current != null && button == 0 && isMouseOver(gui, x, y)) {
			onTileChanged(gui);
		}
	}

	public boolean isMouseOver(T gui, int x, int y) {
		int pos = x - gui.getGuiLeft();
		if (x - gui.getGuiLeft() >= current.left && x - gui.getGuiLeft() <= current.left + current.width && y - gui.getGuiTop() >= current.top && y - gui.getGuiTop() <= current.top + current.height) {
			return true;
		}
		return false;
	}

	public void onTileChanged(T gui) {
		if (enableHelp && current != null && current.isCompletedSuccess(gui)) {
			initGui(gui);
		}
	}

	public Pair<Integer, HelpOverlay<T>> getValidOverlay(T gui) {
		int pos = 0;
		for (HelpOverlay overlay : overlays) {
			if (!overlay.isCompletedSuccess(gui)) {
				return new Pair(pos, overlay);
			}
			pos++;
		}
		return new Pair(-1, null);
	}

}