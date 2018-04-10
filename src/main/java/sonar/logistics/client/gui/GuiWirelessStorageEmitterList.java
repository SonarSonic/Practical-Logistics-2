package sonar.logistics.client.gui;

import java.io.IOException;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import sonar.core.SonarCore;
import sonar.core.helpers.FontHelper;
import sonar.core.network.PacketFlexibleCloseGui;
import sonar.core.network.utils.ByteBufWritable;
import sonar.logistics.PL2;
import sonar.logistics.PL2Translate;
import sonar.logistics.api.tiles.readers.IWirelessStorageReader;
import sonar.logistics.api.wireless.ClientWirelessEmitter;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.client.gui.generic.GuiSelectionList;
import sonar.logistics.common.containers.ContainerEmitterList;
import sonar.logistics.common.items.WirelessStorageReader;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.networking.ClientInfoHandler;
import sonar.logistics.packets.PacketWirelessStorage;

public class GuiWirelessStorageEmitterList extends GuiSelectionList<ClientWirelessEmitter> {

	public EntityPlayer player;
	public ItemStack reader;
	public int clickedIdentity = -1;

	public GuiWirelessStorageEmitterList(ItemStack reader, EntityPlayer player) {
		super(new ContainerEmitterList(player), null);
		this.reader = reader;
		this.xSize = 182 + 66;
		this.player = player;
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		FontHelper.textCentre(PL2Translate.WIRELESS_STORAGE_READER.t(), xSize, 6, LogisticsColours.white_text);
		FontHelper.textCentre(PL2Translate.DATA_RECEIVER_HELP.t(), xSize, 18, LogisticsColours.grey_text);
	}

	public void selectionPressed(GuiButton button, int infoPos, int buttonID, ClientWirelessEmitter info) {
		if (buttonID == 1) {
			RenderBlockSelection.addPosition(info.coords.getCoords(), false);
		} else {
			final int identity = info.getIdentity();
			PL2.network.sendToServer(new PacketWirelessStorage((IWirelessStorageReader) reader.getItem(), reader, player, 1, new ByteBufWritable(false) {

				@Override
				public void writeToBuf(ByteBuf buf) {
					buf.writeInt(identity);
				}

			}));
			clickedIdentity = identity;
		}
	}

	public void setInfo() {
		infoList = Lists.newArrayList(ClientInfoHandler.instance().clientDataEmitters);
	}

	@Override
	public boolean isCategoryHeader(ClientWirelessEmitter info) {
		if (!RenderBlockSelection.positions.isEmpty()) {
			if (RenderBlockSelection.isPositionRenderered(info.coords.getCoords())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSelectedInfo(ClientWirelessEmitter info) {
		if (info.getIdentity() == clickedIdentity) {
			return true;
		}

		ItemStack current = player.getHeldItemMainhand();
		if (current != null && current.hasTagCompound()) {
			int uuid = current.getTagCompound().getInteger(WirelessStorageReader.EMITTER_UUID);
			if (uuid == info.getIdentity()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void renderInfo(ClientWirelessEmitter info, int yPos) {
		int colour = LogisticsColours.white_text.getRGB();
		FontHelper.text(info.name.getObject(), InfoRenderer.left_offset, yPos, colour);
		FontHelper.text(info.coords.getCoords().toString(), (int) ((1.0 / 0.75) * (130)), yPos, colour);
	}

	@Override
	public int getColour(int i, int type) {
		return LogisticsColours.getDefaultSelection().getRGB();
	}

	@Override
	public boolean isPairedInfo(ClientWirelessEmitter info) {
		return false;
	}

	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if ((keyCode == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) && SonarCore.instance.guiHandler.lastScreen != null) {
			SonarCore.network.sendToServer(new PacketFlexibleCloseGui(player.getPosition()));
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}
}
