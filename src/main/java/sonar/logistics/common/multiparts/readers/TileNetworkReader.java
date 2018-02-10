package sonar.logistics.common.multiparts.readers;

import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.logistics.PL2;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.utils.ChannelType;
import sonar.logistics.client.gui.GuiInfoReader;
import sonar.logistics.client.gui.generic.GuiChannelSelection;
import sonar.logistics.common.containers.ContainerChannelSelection;
import sonar.logistics.common.containers.ContainerInfoReader;
import sonar.logistics.info.types.InfoError;
import sonar.logistics.info.types.ProgressInfo;
import sonar.logistics.networking.info.InfoHelper;
import sonar.logistics.networking.subnetworks.NetworkWatcherHandler;

public class TileNetworkReader extends TileAbstractLogicReader<IProvidableInfo> {

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
				if (pair != null && ProgressInfo.isStorableInfo(pair.getSaveableInfo()) && ProgressInfo.isStorableInfo(info.getSaveableInfo())) {
					latestInfo = new ProgressInfo(info.getSaveableInfo(), pair.getSaveableInfo());
				} else {
					latestInfo = info != null ? info.getSaveableInfo() : InfoError.noData;
				}
			}
			PL2.getServerManager().changeInfo(this, id, latestInfo);
		}
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
