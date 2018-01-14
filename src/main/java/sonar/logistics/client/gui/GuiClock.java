package sonar.logistics.client.gui;

import java.text.SimpleDateFormat;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.common.multiparts.misc.TileClock;

public class GuiClock extends GuiLogistics {

	public TileClock part;
	public SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss:SSS");

	public GuiClock(TileClock tileClock) {
		super(new ContainerMultipartSync(tileClock), tileClock);
		this.part = tileClock;
		this.xSize = 176;
		this.ySize = 80;
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		this.buttonList.add(new GuiButton(2, guiLeft + 130 - 3, guiTop + 25, 40, 20, "+0.1 s"));
		this.buttonList.add(new GuiButton(3, guiLeft + 130 - 3, guiTop + 50, 40, 20, "-0.1 s"));
		this.buttonList.add(new GuiButton(4, guiLeft + 90 - 3, guiTop + 25, 40, 20, "+1 s"));
		this.buttonList.add(new GuiButton(5, guiLeft + 90 - 3, guiTop + 50, 40, 20, "-1 s"));
		this.buttonList.add(new GuiButton(6, guiLeft + 50 - 3, guiTop + 25, 40, 20, "+1 min"));
		this.buttonList.add(new GuiButton(7, guiLeft + 50 - 3, guiTop + 50, 40, 20, "-1 min"));
		this.buttonList.add(new GuiButton(8, guiLeft + 10 - 3, guiTop + 25, 40, 20, "+1 hr"));
		this.buttonList.add(new GuiButton(9, guiLeft + 10 - 3, guiTop + 50, 40, 20, "-1 hr"));

	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		RenderHelper.restoreBlendState();
		super.drawGuiContainerForegroundLayer(x, y);
		GL11.glPushMatrix();
		GL11.glScaled(1.5, 1.5, 1.5);
		FontHelper.textCentre(timeFormat.format(getLong() - (60 * 60 * 1000)).substring(0, 11), (int) (xSize / (1.5)), 6, -1);
		GL11.glPopMatrix();

	}
	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
		RenderHelper.restoreBlendState();
	}

	protected void actionPerformed(GuiButton button) {
		if (button != null) 
			part.sendByteBufPacket(button.id);		
		reset();
	}

	public long getLong() {
		return part.tickTime.getObject();
	}

	public void setLong(String string) {
		part.tickTime.setObject(Long.parseLong(string));
		part.sendByteBufPacket(1);
	}
}