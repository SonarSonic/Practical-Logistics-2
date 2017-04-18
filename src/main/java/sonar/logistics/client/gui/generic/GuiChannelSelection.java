package sonar.logistics.client.gui.generic;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import sonar.core.SonarCore;
import sonar.core.api.IFlexibleGui;
import sonar.core.client.gui.GuiHelpOverlay;
import sonar.core.client.gui.HelpOverlay;
import sonar.core.helpers.FontHelper;
import sonar.core.network.PacketFlexibleCloseGui;
import sonar.logistics.PL2;
import sonar.logistics.PL2Translate;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.tiles.IChannelledTile;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.common.containers.ContainerChannelSelection;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.MonitoredBlockCoords;
import sonar.logistics.info.types.MonitoredEntity;

public class GuiChannelSelection extends GuiSelectionList<IInfo> {

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
		infoList = (List<IInfo>) PL2.getClientManager().channelMap.getOrDefault(tile.getNetworkID(), MonitoredList.<IInfo>newMonitoredList(tile.getNetworkID())).clone();
	}

	/// INTERACTION \\\

	@Override
	public void mouseClicked(int x, int y, int button) throws IOException {
		super.mouseClicked(x, y, button);
		overlay.mouseClicked(this, x, y, button);
	}

	public void selectionPressed(GuiButton button, int infoPos, int buttonID, IInfo info) {
		if (buttonID == 0) {
			tile.sendCoordsToServer(info, channelID);
		} else if (info instanceof MonitoredBlockCoords)
			RenderBlockSelection.addPosition(((MonitoredBlockCoords) info).getCoords(), false);

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
		FontHelper.textCentre(PL2Translate.CHANNELS_SELECTION.t(), xSize, 6, LogisticsColours.white_text);
		FontHelper.textCentre(PL2Translate.CHANNELS_SELECTION_HELP.t(), xSize, 18, LogisticsColours.grey_text);
		overlay.drawOverlay(this, x, y);
	}

	@Override
	public void renderInfo(IInfo info, int yPos) {
		InfoRenderer.renderMonitorInfoInGUI(info, yPos + 1, LogisticsColours.white_text.getRGB());
	}

	/// COLOURS \\\

	@Override
	public int getColour(int i, int type) {
		return LogisticsColours.getDefaultSelection().getRGB();
	}

	@Override
	public boolean isCategoryHeader(IInfo info) {
		if (info instanceof MonitoredBlockCoords) {
			if (!RenderBlockSelection.positions.isEmpty()) {
				if (RenderBlockSelection.isPositionRenderered(((MonitoredBlockCoords) info).getCoords())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isSelectedInfo(IInfo info) {
		if (info instanceof MonitoredBlockCoords) {
			if (info.isValid() && !info.isHeader() && tile.getChannels().coordList.contains(((MonitoredBlockCoords) info).getCoords())) {
				return true;
			}
		}
		if (info instanceof MonitoredEntity) {
			if (info.isValid() && !info.isHeader() && tile.getChannels().uuidList.contains(((MonitoredEntity) info).getUUID())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isPairedInfo(IInfo info) {
		return false;
	}
}
