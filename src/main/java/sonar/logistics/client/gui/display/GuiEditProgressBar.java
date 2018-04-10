package sonar.logistics.client.gui.display;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextFormatting;
import sonar.core.client.gui.widgets.ScrollerOrientation;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.SonarHelper;
import sonar.logistics.api.displays.elements.types.ProgressBarElement;
import sonar.logistics.api.displays.elements.types.ProgressBarElement.ProgressBarDirection;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;

public class GuiEditProgressBar extends GuiAbstractEditElements {

	public ProgressBarElement element;
	public int currentColour = -1;

	public GuiEditProgressBar(ProgressBarElement element, TileAbstractDisplay display) {
		super(element, element.getHolder().getContainer(), display);
		this.element = element;
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		for (int i = 0; i < 16; i++) {
			TextFormatting format = TextFormatting.values()[i];
			this.buttonList.add(new TextColourButton(this, 16 + i, guiLeft + 2 + i * 16, guiTop + 210, format) {
				public boolean isSelected() {
					return currentColour == colourRGB;
				}
			});
		}
		spacing_scroller = new SonarScroller(this.guiLeft + 90, this.guiTop + 151 + 20, 16, 80);
		spacing_scroller.setOrientation(ScrollerOrientation.HORIZONTAL);
		setSpacingScroller((float) element.border_thickness);
		this.buttonList.add(new LogisticsButton(this, 0, guiLeft + 4 + 176 + 54, guiTop + 152, 12 * 16, 32 + (element.direction.ordinal() * 16), "Orientation", ""));
		this.buttonList.add(new LogisticsButton(this, 1, guiLeft + 4 + 176 + 54, guiTop + 152 + 18*1, 13 * 16, (element.barType.ordinal() * 16), "Progress Bar Type", ""));
	}

	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button instanceof TextColourButton) {
			currentColour = FontHelper.getColourFromFormatting(((TextColourButton) button).colour);
			element.colour = currentColour;
		} else {
			switch (button.id) {
			case 0:
				element.direction = SonarHelper.incrementEnum(element.direction, ProgressBarDirection.values());
				reset();
				break;
			case 1:
				element.barType = SonarHelper.incrementEnum(element.barType, ProgressBarElement.ProgressBarType.values());
				reset();
				break;
			}

		}
	}

	public void setSpacingScroller(float scaling) {
		element.border_thickness = scaling;
		spacing_scroller.currentScroll = scaling;
	}

}
