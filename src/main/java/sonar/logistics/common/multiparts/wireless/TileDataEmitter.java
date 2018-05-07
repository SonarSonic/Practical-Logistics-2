package sonar.logistics.common.multiparts.wireless;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import sonar.core.api.IFlexibleGui;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.networking.INetworkChannels;
import sonar.logistics.api.networking.INetworkHandler;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IWirelessManager;
import sonar.logistics.info.types.MonitoredFluidStack;
import sonar.logistics.info.types.MonitoredItemStack;
import sonar.logistics.networking.PL2ListenerList;
import sonar.logistics.networking.cabling.WirelessDataManager;
import sonar.logistics.networking.common.ListNetworkChannels;
import sonar.logistics.networking.fluids.FluidNetworkHandler;
import sonar.logistics.networking.items.ItemNetworkHandler;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TileDataEmitter extends TileAbstractEmitter implements IDataEmitter, IFlexibleGui, IByteBufTile {

	public PL2ListenerList listeners = new PL2ListenerList(this, ListenerType.ALL.size());
	@Override
	public IWirelessManager getWirelessHandler() {
		return WirelessDataManager.instance();
	}

	public static int STATIC_ITEM_ID = -16;
	public static int STATIC_FLUID_ID = -17;
	public List<INetworkHandler> validHandlers;

	//// IDataEmitter \\\\
	
	@Override
	public AbstractChangeableList<MonitoredItemStack> getServerItems() {
		return PL2.proxy.getInfoManager(isClient()).getMonitoredList(new InfoUUID(this.getIdentity(), STATIC_ITEM_ID));
	}

	@Override
	public AbstractChangeableList<MonitoredFluidStack> getServerFluids() {
		return PL2.proxy.getInfoManager(isClient()).getMonitoredList(new InfoUUID(this.getIdentity(), STATIC_FLUID_ID));
	}

	//// LIST READER \\\\

	@Override
	public AbstractChangeableList sortMonitoredList(AbstractChangeableList updateInfo, int channelID) {
		return updateInfo;
	}

	public void sendRapidUpdate(EntityPlayer player) {
		SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
		for (INetworkHandler handler : getValidHandlers()) {
			INetworkChannels list = network.getNetworkChannels(handler.getChannelsType());
			if (list instanceof ListNetworkChannels) {
				((ListNetworkChannels) list).sendLocalRapidUpdate(this, player);
			}
		}
	}

	@Override
	public AbstractChangeableList<IInfo> getViewableList(AbstractChangeableList<IInfo> updateList, InfoUUID uuid, Map<NodeConnection, AbstractChangeableList<IInfo>> channels, List<NodeConnection> usedChannels) {
		for (Entry<NodeConnection, AbstractChangeableList<IInfo>> entry : channels.entrySet()) {
			if (!entry.getValue().getList().isEmpty()) {
				for (IMonitoredValue<IInfo> coordInfo : entry.getValue().getList()) {
					updateList.add(coordInfo.getSaveableInfo());
				}
				usedChannels.add(entry.getKey());
			}
		}
		return updateList;
	}

	@Override
	public List<INetworkHandler> getValidHandlers() {
		if (validHandlers == null) {
			validHandlers = Lists.newArrayList(ItemNetworkHandler.INSTANCE, FluidNetworkHandler.INSTANCE);
		}
		return validHandlers;
	}

	@Override
	public List<NodeConnection> getUsedChannels(Map<NodeConnection, AbstractChangeableList<IInfo>> channels) {
		return Lists.newArrayList(channels.keySet());
	}

	public PL2ListenerList getListenerList() {
		return listeners;
	}

}
