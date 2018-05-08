package sonar.logistics.core.tiles.displays.info.elements;

import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.info.types.LogicInfoList;
import sonar.logistics.core.tiles.displays.info.types.energy.MonitoredEnergyStack;

import static net.minecraft.client.renderer.GlStateManager.*;

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

		double actualHeight = height * grid_fill_percentage;
		double scaling = (height / 16) * grid_fill_percentage;
		double offset = Math.min(0.02, actualHeight/10);
		double barScale = (getActualScaling()[WIDTH] - (16 * scaling) - offset);
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
		string = FontHelper.trimToWidthWithParentheses(string, "...", (int) Math.floor(barScale / scaling), 0.5);
		int finalWidth = (int) Math.floor(barScale / scaling)*2;
		
		FontHelper.textCentre(string, finalWidth, 4, this.text_colour);
		translate(0, 0, 0.05);
		
		//// DRAW ENERGY LEVEL \\\\
		FontHelper.textCentre(stack.getClientIdentifier() + " - " + stack.getClientObject(), finalWidth, 20, this.text_colour);
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
