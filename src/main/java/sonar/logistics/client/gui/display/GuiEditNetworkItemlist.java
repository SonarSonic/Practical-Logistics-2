package sonar.logistics.client.gui.display;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextFormatting;
import sonar.core.client.gui.widgets.ScrollerOrientation;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.helpers.FontHelper;
import sonar.logistics.api.displays.elements.types.NetworkItemListElement;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;

public class GuiEditNetworkItemlist extends GuiAbstractEditElements {

	public NetworkItemListElement itemList;
	public int currentColour = -1;

	public GuiEditNetworkItemlist(NetworkItemListElement itemList, TileAbstractDisplay display) {
		super(itemList, itemList.getHolder().getContainer(), display);
		this.itemList = itemList;
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
		setSpacingScroller(Math.min(1, (float) (itemList.sharedSize / Math.min(c.getActualScaling()[0], c.getActualScaling()[1]))));
	}

	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button instanceof TextColourButton) {
			currentColour = FontHelper.getColourFromFormatting(((TextColourButton) button).colour);
			itemList.colour = currentColour;
		}
	}

	public void setSpacingScroller(float scaling) {
		itemList.sharedSize = scaling * Math.min(c.getActualScaling()[0], c.getActualScaling()[1]);
		spacing_scroller.currentScroll = scaling;
	}

}
