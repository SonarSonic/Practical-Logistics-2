package sonar.logistics.common.multiparts.readers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.networks.INetworkListHandler;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.ChannelList;
import sonar.logistics.api.utils.ChannelType;
import sonar.logistics.client.gui.GuiInfoReader;
import sonar.logistics.client.gui.generic.GuiChannelSelection;
import sonar.logistics.common.containers.ContainerChannelSelection;
import sonar.logistics.common.containers.ContainerInfoReader;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.networking.handlers.InfoNetworkHandler;
import sonar.logistics.networking.handlers.NetworkWatcherHandler;

public class TileNetworkReader extends TileAbstractLogicReader<IProvidableInfo> {

	@Override
	public AbstractChangeableList<IProvidableInfo> getViewableList(AbstractChangeableList<IProvidableInfo> updateList, InfoUUID uuid, Map<NodeConnection, AbstractChangeableList<IProvidableInfo>> channels, List<NodeConnection> usedChannels) {
		//everything should be viewable
		
		/*ChannelList readerChannels = getChannels();
		
		AbstractChangeableList<IProvidableInfo> list = channels
		for (Entry<NodeConnection, AbstractChangeableList<T>> entry : channels.entrySet()) {
			if (readerChannels.isMonitored(entry.getKey())) {
				for (IMonitoredValue<T> coordInfo : entry.getValue().getList()) {
					if (canMonitorInfo(coordInfo, uuid, channels, usedChannels)) {
						updateList.add(coordInfo.getSaveableInfo());
					}
				}
				usedChannels.add(entry.getKey());
				if (channelType() == ChannelType.SINGLE) {
					break;
				}
			}
		}
		*/
		return updateList;
	}
	
	@Override
	public List<INetworkHandler> addValidHandlers(List<INetworkHandler> handlers) {
		handlers.add(NetworkWatcherHandler.INSTANCE);
		return handlers;
	}

	@Override
	public AbstractChangeableList<IProvidableInfo> sortMonitoredList(AbstractChangeableList<IProvidableInfo> updateInfo, int channelID) {
		return InfoHelper.sortInfoList(updateInfo);
	}

	@Override
	public ChannelType channelType() {
		return ChannelType.SINGLE;
	}

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
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.NETWORK_READER;
	}

}
