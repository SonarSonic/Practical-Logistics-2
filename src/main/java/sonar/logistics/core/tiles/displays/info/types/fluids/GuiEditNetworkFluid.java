package sonar.logistics.core.tiles.displays.info.types.fluids;

import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.input.Keyboard;
import sonar.core.client.gui.IGuiOrigin;
import sonar.core.helpers.FontHelper;
import sonar.logistics.base.gui.GuiLogistics;
import sonar.logistics.base.gui.buttons.LogisticsButton;
import sonar.logistics.base.requests.colour.CustomColourButton;
import sonar.logistics.base.requests.colour.GuiColourSelection;
import sonar.logistics.base.requests.colour.TextColourButton;
import sonar.logistics.core.tiles.displays.gsi.gui.GuiAbstractEditElements;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

import java.io.IOException;

public class GuiEditNetworkFluid extends GuiAbstractEditElements {

	public ElementNetworkFluid fluid_element;

	public GuiEditNetworkFluid(ElementNetworkFluid element, TileAbstractDisplay display) {
		super(element, element.getHolder().getContainer(), display);
		this.fluid_element = element;
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		
		this.buttonList.clear();
		this.fieldList.clear();
		/*
		scaling_scroller = new SonarScroller(this.guiLeft + 90, this.guiTop + 151, 16, 80);
		scaling_scroller.setOrientation(ScrollerOrientation.HORIZONTAL);
		setScalingScroller((float) c.percentageScale);

		scaling_field = new SonarTextField(0, fontRenderer, 20, 153, 40, 11);
		scaling_field.setDigitsOnly(true);
		scaling_field.setMaxStringLength(3);
		scaling_field.setText(String.valueOf((int) (scaling_scroller.currentScroll * 100)));
		fieldList.add(scaling_field);		
		
		
		
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
		this.buttonList.add(new LogisticsButton(this, 0, guiLeft + 198 + 32, guiTop + 150 + 40, 11 * 16, 112, "Font Colour", "Change the colour of the items text"));
		*/
	}

	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button instanceof TextColourButton) {
			GuiLogistics.setCurrentColour(FontHelper.getColourFromFormatting(((TextColourButton) button).colour));
		}
		if (button instanceof CustomColourButton) {
			FMLCommonHandler.instance().showGuiScreen(IGuiOrigin.withOrigin(new GuiColourSelection(inventorySlots, entity), this));
			return;
		}
		if(button instanceof LogisticsButton){
			switch(button.id){
			case 0: 
				fluid_element.text_colour = GuiLogistics.getCurrentColour();
				break;
			}
		}
	}
}
