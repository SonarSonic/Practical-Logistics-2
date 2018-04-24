package sonar.logistics.client.gui;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import sonar.core.client.gui.SonarTextField;
import sonar.core.helpers.FontHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.common.multiparts.wireless.TileAbstractEmitter;
import sonar.logistics.common.multiparts.wireless.TileDataEmitter;

public class GuiWirelessEmitter extends GuiLogistics {
	public TileAbstractEmitter part;
	private SonarTextField nameField;

	public GuiWirelessEmitter(TileAbstractEmitter part) {
		super(new ContainerMultipartSync(part), part);
		this.part = part;
		this.ySize = 60;
	}

	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		nameField = new SonarTextField(0, this.fontRenderer, 8, 18, 160, 12);
		nameField.setMaxStringLength(20);
		nameField.setText(part.getEmitterName());
		fieldList.add(nameField);
		this.buttonList.add(new GuiButton(1, guiLeft + 116 - (18 * 6), guiTop + 34, 160, 20, part.getSecurity().getClientName()));
	}

	public void onTextFieldChanged(SonarTextField field) {
		final String text = field.getText();
		this.part.emitterName.setObject(text.isEmpty() ? TileDataEmitter.UNNAMED : text);
		part.sendByteBufPacket(2);
	}

	public void actionPerformed(GuiButton button) {
		if (button.id == 1) {
			part.security.incrementEnum();
			part.sendByteBufPacket(5);
			reset();
		}
	}

	@Override
	public void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		FontHelper.textCentre(part.getMultipart().getDisplayName(), xSize, 6, LogisticsColours.white_text.getRGB());
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
	}
}
