package sonar.logistics.common.multiparts.readers;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.networks.INetworkListHandler;
import sonar.logistics.api.states.TileMessage;
import sonar.logistics.api.utils.ChannelType;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.client.gui.GuiInfoReader;
import sonar.logistics.client.gui.generic.GuiChannelSelection;
import sonar.logistics.common.containers.ContainerChannelSelection;
import sonar.logistics.common.containers.ContainerInfoReader;
import sonar.logistics.connections.handlers.InfoNetworkHandler;
import sonar.logistics.helpers.InfoHelper;

public class InfoReaderPart extends AbstractInfoReaderPart<IProvidableInfo> implements IByteBufTile {

	public static final TileMessage[] validStates = new TileMessage[] { TileMessage.NO_NETWORK, TileMessage.NO_DATA_SELECTED};

	@Override
	public List<INetworkListHandler> addValidHandlers(List<INetworkListHandler> handlers) {
		handlers.add(InfoNetworkHandler.INSTANCE);
		return handlers;
	}

	//// ILogicReader \\\\
	@Override
	public MonitoredList<IProvidableInfo> sortMonitoredList(MonitoredList<IProvidableInfo> updateInfo, int channelID) {
		updateInfo.setInfo(InfoHelper.sortInfoList(updateInfo));
		return updateInfo;
	}

	//// IChannelledTile \\\\
	@Override
	public ChannelType channelType() {
		return ChannelType.SINGLE;
	}

	//// GUI \\\\
	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:	return new ContainerInfoReader(player, this);
		case 1:	return new ContainerChannelSelection(this);
		default: return null;}
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:	return new GuiInfoReader(player, this);
		case 1:	return new GuiChannelSelection(player, this, 0);
		default: return null;}
	}

	@Override
	public TileMessage[] getValidMessages() {
		return validStates;
	}

	@Override
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.INFO_READER;
	}

}
