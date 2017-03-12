package sonar.logistics.client.gui.generic;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import sonar.core.SonarCore;
import sonar.core.api.IFlexibleGui;
import sonar.core.client.gui.GuiHelpOverlay;
import sonar.core.client.gui.HelpOverlay;
import sonar.core.helpers.FontHelper;
import sonar.core.network.PacketFlexibleCloseGui;
import sonar.logistics.Logistics;
import sonar.logistics.api.cabling.IChannelledTile;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.common.containers.ContainerChannelSelection;
import sonar.logistics.connections.monitoring.MonitoredBlockCoords;
import sonar.logistics.connections.monitoring.MonitoredEntity;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.InfoRenderer;

public class GuiChannelSelection extends GuiSelectionList<IMonitorInfo> {

	public EntityPlayer player;
	public IChannelledTile tile;
	public int channelID;

	public GuiHelpOverlay<GuiChannelSelection> overlay = new GuiHelpOverlay<GuiChannelSelection>() {

		{
			this.overlays.add(new HelpOverlay<GuiChannelSelection>("select channel", 7, 5, 20, 19, Color.RED.getRGB()) {
				public boolean isCompletedSuccess(GuiChannelSelection gui) {
					// if (!gui.part.getChannels().isEmpty()) {
					// return true;
					// }
					return false;
				}

				public boolean canBeRendered(GuiChannelSelection gui) {
					return true;
				}
			});
			this.overlays.add(new HelpOverlay<GuiChannelSelection>("guide.Hammer.name", 4, 26, 231, 137, Color.RED.getRGB()) {
				public boolean isCompletedSuccess(GuiChannelSelection gui) {
					// if (gui.part.getSelectedInfo().get(0) != null) {
					// return true;
					// }
					return false;
				}

				public boolean canBeRendered(GuiChannelSelection gui) {
					return true;
				}
			});
		}

	};

	public GuiChannelSelection(EntityPlayer player, IChannelledTile tile, int channelID) {
		super(new ContainerChannelSelection(tile), tile);
		this.player = player;
		this.tile = tile;
		this.channelID = channelID;
		this.xSize = 182 + 66;
	}

	/// CREATE \\\

	public void initGui() {
		super.initGui();
		overlay.initGui(this);
	}

	public void setInfo() {
		infoList = (ArrayList<IMonitorInfo>) Logistics.getClientManager().channelMap.getOrDefault(tile.getNetworkID(), MonitoredList.<IMonitorInfo>newMonitoredList(tile.getNetworkID())).clone();
	}

	/// INTERACTION \\\

	@Override
	public void mouseClicked(int x, int y, int button) throws IOException {
		super.mouseClicked(x, y, button);
		overlay.mouseClicked(this, x, y, button);
	}

	public void selectionPressed(GuiButton button, int infoPos, int buttonID, IMonitorInfo info) {
		if (buttonID == 0) {
			tile.modifyCoords(info, channelID);
		} else {
			if (info instanceof MonitoredBlockCoords)
				RenderBlockSelection.addPosition(((MonitoredBlockCoords) info).syncCoords.getCoords(), false);
		}
	}

	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if ((keyCode == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) && tile instanceof IFlexibleGui && SonarCore.instance.guiHandler.lastScreen != null) {
			SonarCore.network.sendToServer(new PacketFlexibleCloseGui(tile.getCoords().getBlockPos()));
		} else {
			super.keyTyped(typedChar, keyCode);
			overlay.onTileChanged(this);
		}
	}

	/// DRAWING \\\

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		FontHelper.textCentre(FontHelper.translate("Channel Selection"), xSize, 6, LogisticsColours.white_text);
		FontHelper.textCentre(String.format("Select the channels you wish to monitor"), xSize, 18, LogisticsColours.grey_text);
		overlay.drawOverlay(this, x, y);
	}

	@Override
	public void renderInfo(IMonitorInfo info, int yPos) {
		InfoRenderer.renderMonitorInfoInGUI(info, yPos + 1, LogisticsColours.white_text.getRGB());
	}

	/// COLOURS \\\

	@Override
	public int getColour(int i, int type) {
		return LogisticsColours.getDefaultSelection().getRGB();
	}

	@Override
	public boolean isCategoryHeader(IMonitorInfo info) {
		if (info instanceof MonitoredBlockCoords) {
			if (!RenderBlockSelection.positions.isEmpty()) {
				if (RenderBlockSelection.isPositionRenderered(((MonitoredBlockCoords) info).syncCoords.getCoords())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isSelectedInfo(IMonitorInfo info) {
		if (info instanceof MonitoredBlockCoords) {
			if (info.isValid() && !info.isHeader() && tile.getChannels().coordList.contains(((MonitoredBlockCoords) info).syncCoords.getCoords())) {
				return true;
			}
		}
		if (info instanceof MonitoredEntity) {
			if (info.isValid() && !info.isHeader() && tile.getChannels().uuidList.contains(((MonitoredEntity) info).uuid.getUUID())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isPairedInfo(IMonitorInfo info) {
		return false;
	}
}
