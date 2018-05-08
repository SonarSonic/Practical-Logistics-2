package sonar.logistics.core.tiles.readers.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.IProvidableInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValue;
import sonar.logistics.api.core.tiles.readers.ILogicListSorter;
import sonar.logistics.api.core.tiles.readers.channels.INetworkHandler;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.channels.ChannelType;
import sonar.logistics.base.channels.ContainerChannelSelection;
import sonar.logistics.base.channels.GuiChannelSelection;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.core.tiles.displays.info.types.progress.InfoProgressBar;
import sonar.logistics.core.tiles.readers.base.TileAbstractLogicReader;
import sonar.logistics.core.tiles.readers.info.ContainerInfoReader;
import sonar.logistics.core.tiles.readers.info.GuiInfoReader;
import sonar.logistics.core.tiles.readers.info.InfoSorter;
import sonar.logistics.core.tiles.readers.network.handling.NetworkWatcherHandler;

import java.util.List;
import java.util.Map;

public class TileNetworkReader extends TileAbstractLogicReader<IProvidableInfo> {

	public InfoSorter info_sorter = new InfoSorter();
	
	@Override
	public int getMaxInfo() {
		return 4;
	}
	
	@Override
	public AbstractChangeableList<IProvidableInfo> getViewableList(AbstractChangeableList<IProvidableInfo> updateList, InfoUUID uuid, Map<NodeConnection, AbstractChangeableList<IProvidableInfo>> channels, List<NodeConnection> usedChannels) {
		//everything should be viewable
		return updateList;
	}

	@Override
	public void setMonitoredInfo(AbstractChangeableList<IProvidableInfo> updateInfo, List<NodeConnection> usedChannels, InfoUUID uuid) {
		List<IProvidableInfo> selected = getSelectedInfo();
		List<IProvidableInfo> paired = getPairedInfo();
		for (int i = 0; i < getMaxInfo(); i++) {
			IInfo latestInfo = null;
			InfoUUID id = new InfoUUID(getIdentity(), i);
			IMonitoredValue<IProvidableInfo> info = updateInfo.find(selected.get(i));
			IMonitoredValue<IProvidableInfo> pair = updateInfo.find(paired.get(i));
			if (info != null) {
				if (pair != null && InfoProgressBar.isStorableInfo(pair.getSaveableInfo()) && InfoProgressBar.isStorableInfo(info.getSaveableInfo())) {
					latestInfo = new InfoProgressBar(info.getSaveableInfo(), pair.getSaveableInfo());
				} else {
					latestInfo = info.getSaveableInfo();
				}
			}
			ServerInfoHandler.instance().changeInfo(this, id, latestInfo);
		}
	}
	@Override
	public List<INetworkHandler> addValidHandlers(List<INetworkHandler> handlers) {
		handlers.add(NetworkWatcherHandler.INSTANCE);
		return handlers;
	}

	@Override
	public AbstractChangeableList<IProvidableInfo> sortMonitoredList(AbstractChangeableList<IProvidableInfo> updateInfo, int channelID) {
		return info_sorter.sortSaveableList(updateInfo);
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

	@Override
	public ILogicListSorter getSorter() {
		return info_sorter;
	}

}
