package sonar.logistics.api.displays.elements.types;

import static net.minecraft.client.renderer.GlStateManager.*;

import net.minecraft.client.renderer.GlStateManager;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.LogicInfoList;
import sonar.logistics.info.types.MonitoredEnergyStack;
import sonar.logistics.info.types.MonitoredItemStack;

public class NetworkEnergyListElement extends NetworkListElement<MonitoredEnergyStack> {

	public NetworkEnergyListElement() {
		super();
	}

	public NetworkEnergyListElement(InfoUUID uuid) {
		super(uuid);
	}

	@Override
	public double getRenderHeight() {
		return Math.min(Math.min(element_size, getActualScaling()[1]), getActualScaling()[0] / 8);
	}

	@Override
	public void renderGridElement(MonitoredEnergyStack stack, int index) {
		enableLighting();

		double offset = 0.02;
		double actualHeight = height * grid_fill_percentage;
		double scaling = (height / 16) * grid_fill_percentage;
		double barScale = (getActualScaling()[0] - (16 * scaling) - offset);
		double barWidth = stack.getEnergyStack().stored * barScale / stack.getEnergyStack().capacity;
		double left = 16 * scaling + offset;
		double top = (actualHeight / 2) + offset;
		double right = (16 * scaling) + barWidth;
		double bottom = actualHeight - offset;
		
		//// DRAW ENERGY BAR \\\\
		DisplayElementHelper.drawRect(left, top, right, bottom, FontHelper.getIntFromColor(80, 180, 80));
		translate(0, 0, -0.001);
		DisplayElementHelper.drawRect(left - offset, top - offset, left - offset + barScale + offset, bottom + offset, FontHelper.getIntFromColor(20, 60, 20));
		translate(0, 0, 0.001);

		scale(scaling, scaling, 0.001);

		translate(16, 0, 0);
		scale(0.5, 0.5, 0.5);
		disableLighting();

		//// DRAW NAME \\\\
		String string = stack.getMonitoredCoords().getClientIdentifier() + " - " + stack.getMonitoredCoords().getCoords().toString();
		String parentheses = "...";

		int specialWidth = RenderHelper.fontRenderer.getStringWidth(parentheses);
		int fullWidth = (((int) Math.floor(barScale / scaling)) - specialWidth) * 2;
		String trimmed = RenderHelper.fontRenderer.trimStringToWidth(string, fullWidth);
		if (string.length() != trimmed.length()) {
			trimmed = trimmed + parentheses;
		}
		FontHelper.textCentre(trimmed, fullWidth, 4, this.text_colour);
		translate(0, 0, 0.05);
		
		//// DRAW ENERGY LEVEL \\\\
		FontHelper.textCentre(stack.getClientIdentifier() + " - " + stack.getClientObject(), fullWidth, 20, this.text_colour);
		translate(0, 0, -0.05);

		scale(1 / 0.5, 1 / 0.5, 1 / 0.5);
		translate(-16, 0, 0);
		
		disableLighting();
		RenderHelper.renderItemIntoGUI(stack.getDropStack().getItemStack(), 0, 0);
		enableLighting();
		enableAlpha();
	}

	@Override
	public void onGridElementClicked(DisplayScreenClick click, LogicInfoList list, MonitoredEnergyStack stack) {

	}

	public static final String REGISTRY_NAME = "n_energy_l";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

}
