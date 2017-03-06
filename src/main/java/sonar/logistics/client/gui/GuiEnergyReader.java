package sonar.logistics.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.utils.CustomColour;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.common.containers.ContainerEnergyReader;
import sonar.logistics.common.multiparts.EnergyReaderPart;
import sonar.logistics.common.multiparts.MonitorMultipart;
import sonar.logistics.connections.monitoring.MonitoredEnergyStack;

public class GuiEnergyReader extends GuiSelectionList<MonitoredEnergyStack> {

	public EnergyReaderPart part;

	public GuiEnergyReader(EntityPlayer player, EnergyReaderPart tile) {
		super(new ContainerEnergyReader(player, tile), tile);
		this.part = tile;
		this.xSize = 182 + 66;
		this.listHeight = 18;
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		FontHelper.textCentre(FontHelper.translate("item.EnergyReader.name"), xSize, 6, LogisticsColours.white_text);
		FontHelper.textCentre(String.format("Select the energy you wish to monitor"), xSize, 18, LogisticsColours.grey_text);
	}

	public double listScale() {
		return 1;
	}

	public void setInfo() {
		infoList = part.getMonitoredList().cloneInfo();
	}

	@Override
	public void selectionPressed(GuiButton button, int infoPos, int buttonID, MonitoredEnergyStack info) {
		if (buttonID == 0) {
			if (info.isValid() && !info.isHeader()) {
				part.selected.setCoords(info.coords.getMonitoredInfo().syncCoords.getCoords());
				part.sendByteBufPacket(buttonID == 0 ? MonitorMultipart.ADD : MonitorMultipart.PAIRED);
			}
		} else {
			RenderBlockSelection.addPosition(info.coords.getMonitoredInfo().syncCoords.getCoords(), false);
		}
	}

	@Override
	public boolean isCategoryHeader(MonitoredEnergyStack info) {
		if (!RenderBlockSelection.positions.isEmpty()) {
			if (RenderBlockSelection.isPositionRenderered(info.coords.getMonitoredInfo().syncCoords.getCoords())) {
				return true;
			}
		}
		return info.isHeader();
	}

	@Override
	public boolean isSelectedInfo(MonitoredEnergyStack info) {
		if (!info.isValid() || info.isHeader()) {
			return false;
		}//X: -468 Y: 4 Z: -975 D: 0
		return part.selected.getCoords()!=null && part.selected.getCoords().equals(info.coords.getMonitoredInfo().syncCoords.getCoords());
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

		int l = (int) (info.energyStack.obj.stored * 231 / info.energyStack.obj.capacity);
		if (l != 0) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderHelper.saveBlendState();
			this.drawTransparentRect(25, (yPos) + 6, 231, (yPos) + 14, new CustomColour(0, 10, 5).getRGB());
			this.drawTransparentRect(25, (yPos) + 6, l, (yPos) + 14, new CustomColour(0, 100, 50).getRGB());
			RenderHelper.restoreBlendState();
		}

		StoredItemStack storedStack = info.dropStack.getObject();
		if (storedStack != null) {

			net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderHelper.saveBlendState();
			ItemStack stack = storedStack.item;
			RenderHelper.renderItem(this, 8, yPos - 2, stack);
			RenderHelper.renderStoredItemStackOverlay(stack, 0, 8, yPos - 2, null, true);
			RenderHelper.restoreBlendState();
		}
		GL11.glScaled(0.75, 0.75, 0.75);
		FontHelper.text(info.coords.getMonitoredInfo().getClientIdentifier() + " - " + info.coords.getMonitoredInfo().getClientObject(), 35, (int) (yPos * 1 / 0.75) - 1, LogisticsColours.white_text.getRGB());
		FontHelper.text(info.getClientIdentifier(), 35, (int) (yPos * 1 / 0.75) + 10, 1);
		FontHelper.text(info.getClientObject(), 160, (int) (yPos * 1 / 0.75) + 10, 1);
		GL11.glScaled(1 / 0.75, 1 / 0.75, 1 / 0.75);

	}

	@Override
	public int getColour(int i, int type) {
		IMonitorInfo info = (IMonitorInfo) infoList.get(i + start);
		if (info == null || info.isHeader()) {
			return LogisticsColours.layers[1].getRGB();
		}
		/* ArrayList<IMonitorInfo> selectedInfo = type == 0 ? part.getSelectedInfo() : part.getPairedInfo(); int pos = 0; for (IMonitorInfo selected : selectedInfo) { if (selected != null && !selected.isHeader() && info.isMatchingType(selected) && info.isMatchingInfo(selected)) { return LogisticsColours.infoColours[pos].getRGB(); } pos++; } */
		return LogisticsColours.getDefaultSelection().getRGB();
	}
}
