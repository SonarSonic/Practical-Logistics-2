package sonar.logistics.client.gui.display;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import sonar.core.client.gui.GuiSonar;
import sonar.core.client.gui.SonarTextField;
import sonar.core.client.gui.widgets.ScrollerOrientation;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.helpers.FontHelper;
import sonar.logistics.api.displays.elements.types.NetworkGridElement;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.gui.GuiColourSelection;
import sonar.logistics.client.gui.GuiLogistics;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;

public class GuiEditNetworkFluidlist extends GuiAbstractEditElements {

	public NetworkGridElement itemList;
	public int currentColour = -1;

	public GuiEditNetworkFluidlist(NetworkGridElement itemList, TileAbstractDisplay display) {
		super(itemList, itemList.getHolder().getContainer(), display);
		this.itemList = itemList;
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
		*/

		spacing_scroller = new SonarScroller(this.guiLeft + 14, this.guiTop + 157, 16, 180);
		spacing_scroller.setOrientation(ScrollerOrientation.HORIZONTAL);

		spacing_field = new SonarTextField(1, fontRenderer, 206, 158, 40, 12);
		spacing_field.setDigitsOnly(true);
		spacing_field.setMaxStringLength(3);
		spacing_field.setText(String.valueOf((int) (spacing_scroller.currentScroll * 100)));
		fieldList.add(spacing_field);
		
		
		
		
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
		this.buttonList.add(new LogisticsButton(this, 0, guiLeft + 198 + 32, guiTop + 150 + 40, 11 * 16, 112, "Font Colour", "Change the colour of the fluid text"));
		setSpacingScroller(Math.min(1, (float) (itemList.element_size / Math.min(c.getActualScaling()[0], c.getActualScaling()[1]))));
	}

	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button instanceof TextColourButton) {
			GuiLogistics.setCurrentColour(FontHelper.getColourFromFormatting(((TextColourButton) button).colour));
		}
		if (button instanceof CustomColourButton) {
			FMLCommonHandler.instance().showGuiScreen(GuiSonar.withOrigin(new GuiColourSelection(inventorySlots, entity), this));
			return;
		}
		if(button instanceof LogisticsButton){
			switch(button.id){
			case 0: 
				itemList.text_colour = GuiLogistics.getCurrentColour();
				break;
			}
		}
	}

	public void setSpacingScroller(float scaling) {
		scaling = ((int)(scaling*100F))/100F;
		itemList.element_size = scaling * Math.min(c.getActualScaling()[0], c.getActualScaling()[1]);
		spacing_scroller.currentScroll = scaling;
		this.spacing_field.setText(String.valueOf((int)(scaling*100F)));
	}

}
