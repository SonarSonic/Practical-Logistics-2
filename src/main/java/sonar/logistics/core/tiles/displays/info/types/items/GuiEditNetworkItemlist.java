package sonar.logistics.core.tiles.displays.info.types.items;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.input.Keyboard;
import sonar.core.client.gui.IGuiOrigin;
import sonar.core.client.gui.SonarTextField;
import sonar.core.client.gui.widgets.ScrollerOrientation;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.helpers.FontHelper;
import sonar.logistics.base.gui.GuiLogistics;
import sonar.logistics.base.gui.buttons.LogisticsButton;
import sonar.logistics.base.requests.colour.CustomColourButton;
import sonar.logistics.base.requests.colour.GuiColourSelection;
import sonar.logistics.base.requests.colour.TextColourButton;
import sonar.logistics.core.tiles.displays.gsi.gui.GuiAbstractEditElements;
import sonar.logistics.core.tiles.displays.info.elements.NetworkGridElement;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

import java.io.IOException;

public class GuiEditNetworkItemlist extends GuiAbstractEditElements {

	public NetworkGridElement itemList;

	public GuiEditNetworkItemlist(NetworkGridElement element, TileAbstractDisplay display) {
		super(element, element.getHolder().getContainer(), display);
		this.itemList = element;
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
		this.buttonList.add(new LogisticsButton(this, 0, guiLeft + 198 + 32, guiTop + 150 + 40, 11 * 16, 112, "Font Colour", "Change the colour of the items text"));
		setSpacingScroller(Math.min(1, (float) (itemList.element_size / Math.min(c.getActualScaling()[0], c.getActualScaling()[1]))));
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
