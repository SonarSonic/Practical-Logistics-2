package sonar.logistics.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import org.lwjgl.input.Keyboard;
import sonar.core.client.gui.SonarTextField;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.helpers.FontHelper;
import sonar.core.utils.CustomColour;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.client.gui.display.GuiHolographicRescaling;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GuiColourSelection extends GuiLogistics {

	public SonarTextField r, g, b;
	public SonarScroller r_scroller, g_scroller, b_scroller;
	public List<GuiHolographicRescaling.HolographicScroller> scrollers;
	public Consumer<Integer> onClose;
	public int configured = -1;

	public GuiColourSelection(Container container, IWorldPosition entity) {
		this(container, entity, getCurrentColour(), i -> setCurrentColourAndSaveLast(i));
	}

	public GuiColourSelection(Container container, IWorldPosition entity, int current, Consumer<Integer> onClose) {
		super(container, entity);
		this.configured = current;
		this.ySize = 64;
		this.onClose = onClose;
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		CustomColour colour = new CustomColour(configured);
		r = new SonarTextField(2, fontRenderer, 18, 28, 28, 12).setBoxOutlineColour(Color.DARK_GRAY.getRGB()).setDigitsOnly(true);
		r.setMaxStringLength(3);
		r.setText(String.valueOf(colour.red));
		r.setDigitsOnly(true);
		fieldList.add(r);

		g = new SonarTextField(3, fontRenderer, 74, 28, 28, 12).setBoxOutlineColour(Color.DARK_GRAY.getRGB()).setDigitsOnly(true);
		g.setMaxStringLength(3);
		g.setText(String.valueOf(colour.green));
		g.setDigitsOnly(true);
		fieldList.add(g);

		b = new SonarTextField(4, fontRenderer, 130, 28, 28, 12).setBoxOutlineColour(Color.DARK_GRAY.getRGB()).setDigitsOnly(true);
		b.setMaxStringLength(3);
		b.setText(String.valueOf(colour.blue));
		b.setDigitsOnly(true);
		fieldList.add(b);

		scrollers = new ArrayList<>();

	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException{
		super.actionPerformed(button);
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
		super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
		scrollers.forEach(s -> renderScroller(s));
	}

	public void drawScreen(int x, int y, float var) {
		super.drawScreen(x, y, var);
		scrollers.forEach(s -> s.drawScreen(x, y, true));
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		drawRect(5, 5, xSize - 5, 20, configured);
	}

	@Override
	public void onTextFieldChanged(SonarTextField field) {
		super.onTextFieldChanged(field);
		configured = FontHelper.getIntFromColor(r.getIntegerFromText(), g.getIntegerFromText(), b.getIntegerFromText());
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException {
		super.keyTyped(c, i);
		if (isCloseKey(i)) {
			onClose.accept(configured);
		}
	}
}
