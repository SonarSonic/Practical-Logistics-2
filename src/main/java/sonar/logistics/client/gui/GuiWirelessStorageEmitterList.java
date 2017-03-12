package sonar.logistics.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import mcmultipart.util.IWorldLocation;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import sonar.core.SonarCore;
import sonar.core.api.IFlexibleGui;
import sonar.core.helpers.FontHelper;
import sonar.core.inventory.ContainerEmpty;
import sonar.core.network.PacketFlexibleCloseGui;
import sonar.core.network.utils.ByteBufWritable;
import sonar.logistics.Logistics;
import sonar.logistics.api.readers.IWirelessStorageReader;
import sonar.logistics.api.wireless.ClientDataEmitter;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.client.gui.generic.GuiSelectionList;
import sonar.logistics.common.containers.ContainerDataReceiver;
import sonar.logistics.common.containers.ContainerEmitterList;
import sonar.logistics.common.items.WirelessStorageReader;
import sonar.logistics.common.multiparts.DataReceiverPart;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.network.PacketWirelessStorage;

public class GuiWirelessStorageEmitterList extends GuiSelectionList<ClientDataEmitter> {

	public EntityPlayer player;
	public ItemStack reader;

	public GuiWirelessStorageEmitterList(ItemStack reader, EntityPlayer player) {
		super(new ContainerEmitterList(player), null);
		this.reader = reader;
		this.xSize = 182 + 66;
		this.player = player;
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		FontHelper.textCentre(FontHelper.translate("Wireless Storage Reader"), xSize, 6, LogisticsColours.white_text);
		FontHelper.textCentre(String.format("Select the emitter you wish to connect to"), xSize, 18, LogisticsColours.grey_text);
	}

	public void selectionPressed(GuiButton button, int infoPos, int buttonID, ClientDataEmitter info) {
		if (buttonID == 1) {
			RenderBlockSelection.addPosition(info.coords.getCoords(), false);
		} else {
			final UUID usedUUID = info.uuid.getUUID();
			Logistics.network.sendToServer(new PacketWirelessStorage((IWirelessStorageReader) reader.getItem(), reader, player, 1, new ByteBufWritable(false) {

				@Override
				public void writeToBuf(ByteBuf buf) {
					buf.writeLong(usedUUID.getMostSignificantBits());
					buf.writeLong(usedUUID.getLeastSignificantBits());
				}

			}));
		}
	}

	public void setInfo() {
		infoList = (ArrayList<ClientDataEmitter>) Logistics.getClientManager().clientEmitters.clone();
	}

	@Override
	public boolean isCategoryHeader(ClientDataEmitter info) {
		if (!RenderBlockSelection.positions.isEmpty()) {
			if (RenderBlockSelection.isPositionRenderered(info.coords.getCoords())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSelectedInfo(ClientDataEmitter info) {
		ItemStack current = player.getHeldItemMainhand();
		if(current!=null && current.hasTagCompound()){
			UUID uuid = current.getTagCompound().getUniqueId(WirelessStorageReader.EMITTER_UUID);
			if(uuid.equals(info.uuid.getUUID())){
				return true;
			}
		}
		return false;
	}

	@Override
	public void renderInfo(ClientDataEmitter info, int yPos) {
		int colour = LogisticsColours.white_text.getRGB();
		FontHelper.text(info.name.getObject(), InfoRenderer.identifierLeft, yPos, colour);
		FontHelper.text(info.coords.getCoords().toString(), (int) ((1.0 / 0.75) * (130)), yPos, colour);
	}

	@Override
	public int getColour(int i, int type) {
		return LogisticsColours.getDefaultSelection().getRGB();
	}

	@Override
	public boolean isPairedInfo(ClientDataEmitter info) {
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
