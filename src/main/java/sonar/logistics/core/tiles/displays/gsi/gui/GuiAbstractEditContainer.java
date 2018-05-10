package sonar.logistics.core.tiles.displays.gsi.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Tuple;
import sonar.core.client.gui.SonarTextField;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.gsi.packets.GSIElementPacketHelper;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayElementContainer;
import sonar.logistics.core.tiles.displays.info.elements.DisplayElementHelper;
import sonar.logistics.core.tiles.displays.info.elements.base.IDisplayElement;
import sonar.logistics.core.tiles.displays.tiles.DisplayVectorHelper;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

import java.io.IOException;

public class GuiAbstractEditContainer extends GuiAbstractEditScreen {

	public DisplayElementContainer c;
	public SonarScroller scaling_scroller;
	public SonarScroller spacing_scroller;
	public SonarTextField scaling_field;
	public SonarTextField spacing_field;

	public GuiAbstractEditContainer(DisplayElementContainer c, TileAbstractDisplay display) {
		super(c.getGSI(), display);
		this.c = c;
	}

	public void save() {
		GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createResizeContainerPacket(c.containerIdentity, c.getTranslation(), c.getContainerMaxScaling(), c.percentageScale), -1, c.getGSI());
	}

	public void renderDisplayScreen(float partialTicks, int x, int y) {
		DisplayElementHelper.renderElementStorageHolder(c);
	}

	public double[] getUnscaled() {
		return new double[] { c.getContainerMaxScaling()[0], c.getContainerMaxScaling()[1], 1 };
	}

	public boolean doDisplayScreenClick(double clickX, double clickY, int key) {
		Tuple<IDisplayElement, double[]> click = c.getClickBoxes(new double[] { 0, 0, 0 }, clickX, clickY); // remove adjustment
		if (click != null) {
			DisplayScreenClick fakeClick = DisplayVectorHelper.createFakeClick(gsi, c.getTranslation()[0] + clickX, c.getTranslation()[1] + clickY, isDoubleClick(), key);
			onDisplayElementClicked(click.getFirst(), fakeClick, click.getSecond());
			return true;
		}
		return false;
	}

	public void onDisplayElementClicked(IDisplayElement e, DisplayScreenClick fakeClick, double[] subClick) {
		/* could be uncommented and used for display clicking within a gui
		if (e instanceof IClickableElement) {
			((IClickableElement) e).onGSIClicked(fakeClick, mc.player, subClick[0], subClick[1]);
		}
		*/
	}

	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		this.fieldList.clear();
		/* scaling_scroller = new SonarScroller(this.guiLeft + 90, this.guiTop + 151, 16, 80); scaling_scroller.setOrientation(ScrollerOrientation.HORIZONTAL); setScalingScroller((float) c.percentageScale); spacing_scroller = new SonarScroller(this.guiLeft + 90, this.guiTop + 151 + 20, 16, 80); spacing_scroller.setOrientation(ScrollerOrientation.HORIZONTAL); scaling_field = new SonarTextField(0, fontRenderer, 20, 153, 40, 11); scaling_field.setDigitsOnly(true); scaling_field.setMaxStringLength(3); scaling_field.setText(String.valueOf((int) (scaling_scroller.currentScroll * 100))); fieldList.add(scaling_field); spacing_field = new SonarTextField(1, fontRenderer, 20, 153 + 20, 40, 11); spacing_field.setDigitsOnly(true); spacing_field.setMaxStringLength(3); spacing_field.setText(String.valueOf((int) (spacing_scroller.currentScroll * 100))); fieldList.add(spacing_field); */
		// setSpacingScroller(text.spacing);
	}

	@Override
	public void drawScreen(int x, int y, float var) {
		super.drawScreen(x, y, var);
		if (scaling_scroller != null) {
			scaling_scroller.drawScreen(x, y, true);
			setScalingScroller(scaling_scroller.currentScroll);
		}

		if (spacing_scroller != null) {
			spacing_scroller.drawScreen(x, y, true);
			setSpacingScroller(spacing_scroller.currentScroll);
		}
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		// FontHelper.text("TEXT SCALING", 10, 154, -1);
		// FontHelper.text("TEXT SPACING", 10, 154 + 20, -1);
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
		super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
		GlStateManager.disableLighting();
		if (scaling_scroller != null)
			renderScroller(scaling_scroller);
		if (spacing_scroller != null)
			renderScroller(spacing_scroller);
	}

	public void setScalingScroller(float scaling) {
		if (scaling == 0) {
			scaling = 0.01F;
		}
		if (c.percentageScale != scaling || scaling_scroller.currentScroll != scaling) {
			c.percentageScale = scaling;
			scaling_scroller.currentScroll = scaling;
			setContainerScaling();
			if (scaling_field != null) {
				scaling_field.setText(String.valueOf((int) (scaling_scroller.currentScroll * 100)));
			}
		}
	}

	public void setSpacingScroller(float scaling) {}

	@Override
	public void onTextFieldChanged(SonarTextField field) {
		super.onTextFieldChanged(field);
		if (scaling_field != null && field == scaling_field) {
			int value = field.getIntegerFromText();
			if (value > 100) {
				value = 100;
			}
			setScalingScroller(value / 100F);
			scaling_field.setText(String.valueOf(value));
		} else if (spacing_field != null && field == spacing_field) {
			int value = field.getIntegerFromText();
			if (value > 100) {
				value = 100;
			}
			setSpacingScroller(value / 100F);
			spacing_field.setText(String.valueOf(value));
		}
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException {
		if (isCloseKey(i)) {
			save();
		}
		super.keyTyped(c, i);
	}
}
