package sonar.logistics.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.client.gui.GuiHelpOverlay;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.network.FlexibleGuiHandler;
import sonar.core.utils.CustomColour;
import sonar.logistics.PL2Translate;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.tiles.readers.EnergyReader.Modes;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.client.gui.generic.GuiSelectionList;
import sonar.logistics.common.containers.ContainerEnergyReader;
import sonar.logistics.common.multiparts.readers.TileAbstractReader;
import sonar.logistics.common.multiparts.readers.TileEnergyReader;
import sonar.logistics.info.types.MonitoredEnergyStack;

public class GuiEnergyReader extends GuiSelectionList<MonitoredEnergyStack> {

	public TileEnergyReader part;
	public EntityPlayer player;

	public GuiEnergyReader(EntityPlayer player, TileEnergyReader tileEnergyReader) {
		super(new ContainerEnergyReader(player, tileEnergyReader), tileEnergyReader);
		this.part = tileEnergyReader;
		this.player = player;
		this.xSize = 182 + 66;
		this.listHeight = 18;
	}

	public void initGui() {
		super.initGui();
		initButtons();
	}

	public void initButtons() {
		int start = 8;
		this.buttonList.add(new LogisticsButton.CHANNELS(this, 0, guiLeft + start, guiTop + 9));
		this.buttonList.add(new LogisticsButton.HELP(this, 1, guiLeft + start + 18 * 1, guiTop + 9));
		this.buttonList.add(new LogisticsButton(this, 2, guiLeft + start + 18 * 2, guiTop + 9, 64 + 64 + 16, 16 * part.setting.getObject().ordinal(), part.setting.getObject().getName(), part.setting.getObject().getDescription()));
		if (part.setting.getObject() != Modes.STORAGES) {
			this.buttonList.add(new GuiButton(3, guiLeft + 190, guiTop + 6, 40, 20, part.energyType.getEnergyType().getStorageSuffix()));
		}
	}

	public void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if (button != null) {
			switch (button.id) {
			case 0:
				FlexibleGuiHandler.changeGui(part, 1, 0, player.getEntityWorld(), player);
				break;
			case 1:
				GuiHelpOverlay.enableHelp = !GuiHelpOverlay.enableHelp;
				reset();
				break;
			case 2:
				part.setting.incrementEnum();
				part.sendByteBufPacket(3);
				reset();
				break;
			case 3:
				part.energyType.incrementType();
				part.sendByteBufPacket(4);
				reset();
				break;
			}
		}
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		FontHelper.textCentre(PL2Translate.ENERGY_READER.t(), xSize, 10, LogisticsColours.white_text);
	}

	public double listScale() {
		return 1;
	}

	public void setInfo() {
		infoList = part.getMonitoredList().createSaveableList(part.getSorter());
	}

	@Override
	public void selectionPressed(GuiButton button, int infoPos, int buttonID, MonitoredEnergyStack info) {
		if (buttonID == 0) {
			if (info.isValid() && !info.isHeader()) {
				part.selected.setCoords(info.getMonitoredCoords().getCoords());
				part.sendByteBufPacket(buttonID == 0 ? TileAbstractReader.ADD : TileAbstractReader.PAIRED);
			}
		} else {
			RenderBlockSelection.addPosition(info.getMonitoredCoords().getCoords(), false);
		}
	}

	@Override
	public boolean isCategoryHeader(MonitoredEnergyStack info) {
		if (!RenderBlockSelection.positions.isEmpty()) {
			if (RenderBlockSelection.isPositionRenderered(info.getMonitoredCoords().getCoords())) {
				return true;
			}
		}
		return info.isHeader();
	}

	@Override
	public boolean isSelectedInfo(MonitoredEnergyStack info) {
		if (!info.isValid() || info.isHeader()) {
			return false;
		}
		return part.selected.getCoords() != null && part.selected.getCoords().equals(info.getMonitoredCoords().getCoords());
	}

	@Override
	public boolean isPairedInfo(MonitoredEnergyStack info) {
		if (!info.isValid() || info.isHeader()) {
			return false;
		}
		return false;
	}

	@Override
	public void renderInfo(MonitoredEnergyStack info, int yPos) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		if (info.getEnergyStack().capacity > 0) {
			int l = (int) (info.getEnergyStack().stored * 231 / info.getEnergyStack().capacity);
			this.drawTransparentRect(25, (yPos) + 6, 231, (yPos) + 14, new CustomColour(0, 10, 5).getRGB());
			if (l != 0)
				this.drawTransparentRect(25, (yPos) + 6, l, (yPos) + 14, new CustomColour(0, 100, 50).getRGB());
		}
		StoredItemStack storedStack = info.getDropStack();
		if (storedStack != null) {

			net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			ItemStack stack = storedStack.item;
			RenderHelper.renderItem(this, 8, yPos - 2, stack);
			RenderHelper.renderStoredItemStackOverlay(stack, 0, 8, yPos - 2, null, true);
		}
		GL11.glScaled(0.75, 0.75, 0.75);
		FontHelper.text(info.getMonitoredCoords().getClientIdentifier() + " - " + info.getMonitoredCoords().getClientObject(), 35, (int) (yPos * 1 / 0.75) - 1, LogisticsColours.white_text.getRGB());
		FontHelper.text(info.getClientIdentifier(), 35, (int) (yPos * 1 / 0.75) + 10, 1);
		FontHelper.text(info.getClientObject(), 160, (int) (yPos * 1 / 0.75) + 10, 1);
		GL11.glScaled(1 / 0.75, 1 / 0.75, 1 / 0.75);

	}

	@Override
	public int getColour(int i, int type) {
		IInfo info = (IInfo) infoList.get(i + start);
		if (info == null || info.isHeader()) {
			return LogisticsColours.layers[1].getRGB();
		}
		return LogisticsColours.getDefaultSelection().getRGB();
	}
}
