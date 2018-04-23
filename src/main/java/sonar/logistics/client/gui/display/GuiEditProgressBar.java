package sonar.logistics.client.gui.display;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import sonar.core.client.gui.IGuiOrigin;
import sonar.core.client.gui.widgets.ScrollerOrientation;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.SonarHelper;
import sonar.logistics.api.displays.elements.types.ProgressBarElement;
import sonar.logistics.api.displays.elements.types.ProgressBarElement.ProgressBarDirection;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.gui.GuiColourSelection;
import sonar.logistics.client.gui.GuiLogistics;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;

public class GuiEditProgressBar extends GuiAbstractEditElements {

	public ProgressBarElement element;
	//public int currentColour = -1;

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
			this.buttonList.add(new TextColourButton(this, 16 + i, guiLeft + 2 + i * 14, guiTop + 210, format) {
				public boolean isSelected() {
					return GuiLogistics.getCurrentColour() == colourRGB;
				}
			});
		}
		this.buttonList.add(new CustomColourButton(this, 15, guiLeft + 8 + 16 * 14, guiTop + 210, "Configure Custom Colour") {
			public boolean isSelected() {
				return false;
			}
		});

		spacing_scroller = new SonarScroller(this.guiLeft + 90, this.guiTop + 151 + 20, 16, 80);
		spacing_scroller.setOrientation(ScrollerOrientation.HORIZONTAL);
		setSpacingScroller((float) element.border_thickness);
		this.buttonList.add(new LogisticsButton(this, 0, guiLeft + 4 + 176 + 54, guiTop + 152, 12 * 16, 32 + (element.direction.ordinal() * 16), "Orientation", ""));
		this.buttonList.add(new LogisticsButton(this, 1, guiLeft + 4 + 176 + 54, guiTop + 152 + 18 * 1, 13 * 16, (element.barType.ordinal() * 16), "Progress Bar Type", ""));
		this.buttonList.add(new LogisticsButton(this, 2, guiLeft + 4 + 176 + 54 - 18, guiTop + 152 + 18 * 0, 13 * 16, 14 * 16, "SET BAR COLOUR", ""));
		this.buttonList.add(new LogisticsButton(this, 3, guiLeft + 4 + 176 + 54 - 18, guiTop + 152 + 18 * 1, 13 * 16, 13 * 16, "SET BORDER COLOUR", ""));
		this.buttonList.add(new LogisticsButton(this, 4, guiLeft + 4 + 176 + 54 - 18, guiTop + 152 + 18 * 2, 13 * 16, 15 * 16, "SET Background COLOUR", ""));
		this.buttonList.add(new LogisticsButton(this, 5, guiLeft + 4 + 176 + 54, guiTop + 152 + 18 * 2, 2 * 16, 6 * 16, "Remove Background", ""));
	}

	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button instanceof TextColourButton) {
			GuiLogistics.setCurrentColour(FontHelper.getColourFromFormatting(((TextColourButton) button).colour));
			return;
		}
		if (button instanceof CustomColourButton) {
			FMLCommonHandler.instance().showGuiScreen(IGuiOrigin.withOrigin(new GuiColourSelection(inventorySlots, entity), this));
			return;
		}
		switch (button.id) {
		case 0:
			element.direction = SonarHelper.incrementEnum(element.direction, ProgressBarDirection.values());
			reset();
			break;
		case 1:
			element.barType = SonarHelper.incrementEnum(element.barType, ProgressBarElement.ProgressBarType.values());
			reset();
			break;
		case 2:
			element.colour = GuiLogistics.getCurrentColour();
			break;
		case 3:
			element.border_colour = GuiLogistics.getCurrentColour();
			break;
		case 4:
			element.background_colour = GuiLogistics.getCurrentColour();
			break;
		case 5:
			element.background_colour = 0;
			break;
		}

	}

	public void setSpacingScroller(float scaling) {
		element.border_thickness = scaling;
		spacing_scroller.currentScroll = scaling;
	}

}
