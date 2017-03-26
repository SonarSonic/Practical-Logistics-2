package sonar.logistics.common.multiparts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.IMultipart;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.api.utils.BlockCoords;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncUUID;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.Logistics;
import sonar.logistics.api.LogisticsAPI;
import sonar.logistics.api.cabling.ChannelType;
import sonar.logistics.api.connecting.IInfoManager;
import sonar.logistics.api.connecting.ILogisticsNetwork;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.readers.INetworkReader;
import sonar.logistics.api.readers.IdentifiedChannelsList;
import sonar.logistics.api.viewers.ViewerTally;
import sonar.logistics.api.viewers.ViewerType;
import sonar.logistics.api.viewers.ViewersList;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredBlockCoords;
import sonar.logistics.connections.monitoring.MonitoredEnergyStack;
import sonar.logistics.connections.monitoring.MonitoredEntity;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.network.sync.SyncMonitoredType;

public abstract class MonitorMultipart<T extends IMonitorInfo> extends SidedMultipart implements INetworkReader<T>, IByteBufTile, IFlexibleGui {

	public ViewersList viewers = new ViewersList(this, ViewerType.ALL);

	public static final PropertyBool hasDisplay = PropertyBool.create("display");
	protected IdentifiedChannelsList list = new IdentifiedChannelsList(this, this.channelType(), -2);
	public SyncTagType.BOOLEAN hasMonitor = new SyncTagType.BOOLEAN(-4);
	protected LogicMonitorHandler handler = null;
	protected String handlerID;
	public SyncMonitoredType<T> selectedInfo;
	public IMonitorInfo lastInfo = null;
	public int lastPos = -1;

	public SyncCoords lastSelected = new SyncCoords(-11);
	public SyncUUID lastSelectedUUID = new SyncUUID(-10);

	public MonitorMultipart(String handlerID, double width, double heightMin, double heightMax) {
		super(width, heightMin, heightMax);
		this.handlerID = handlerID;
		this.syncList.addParts(list, hasMonitor);
		selectedInfo = new SyncMonitoredType<T>(-5);
	}

	public MonitorMultipart(String handlerID, EnumFacing face, double width, double heightMin, double heightMax) {
		super(face, width, heightMin, heightMax);
		this.handlerID = handlerID;
		this.syncList.addParts(list, hasMonitor);
		selectedInfo = new SyncMonitoredType<T>(-5);
	}

	public void updateAllInfo() {
		for (int i = 0; i < getMaxInfo(); i++) {
			IMonitorInfo info = getMonitorInfo(i);
			InfoUUID id = new InfoUUID(getIdentity().hashCode(), i);
			Logistics.getServerManager().changeInfo(id, info);
		}
	}

	public void setLocalNetworkCache(INetworkCache network) {
		if (!this.network.isFakeNetwork() && this.network.getNetworkID() != network.getNetworkID()) {
			((ILogisticsNetwork) this.network).removeMonitor(this);
		}
		super.setLocalNetworkCache(network);
		if (network instanceof ILogisticsNetwork) {
			ILogisticsNetwork storageCache = (ILogisticsNetwork) network;
			storageCache.<T>addMonitor(this);
		}
	}

	//// ILogicMonitor \\\\

	@Override
	public MonitoredList<T> getUpdatedList(int infoID, Map<NodeConnection, MonitoredList<?>> channels, ArrayList<NodeConnection> usedChannels) {
		MonitoredList<T> updateList = MonitoredList.<T>newMonitoredList(getNetworkID());
		IdentifiedChannelsList readerChannels = getChannels(); // TODO
		for (Entry<NodeConnection, MonitoredList<?>> entry : channels.entrySet()) {
			if ((entry.getValue() != null && !entry.getValue().isEmpty()) && readerChannels.isMonitored(entry.getKey())) {
				for (T coordInfo : (MonitoredList<T>) entry.getValue()) {
					if (canMonitorInfo(coordInfo, infoID, channels, usedChannels))
						updateList.addInfoToList((T) coordInfo.copy(), (MonitoredList<T>) entry.getValue());
				}
				updateList.sizing.add(entry.getValue().sizing);
				usedChannels.add(entry.getKey());
				if (channelType() == ChannelType.SINGLE) {
					break;
				}
			}
		}
		return updateList;
	}

	public boolean canMonitorInfo(T info, int infoID, Map<NodeConnection, MonitoredList<?>> channels, ArrayList<NodeConnection> usedChannels) {
		return true;
	}

	@Override
	public LogicMonitorHandler[] getValidHandlers() {
		return new LogicMonitorHandler[] { handler == null ? handler = LogicMonitorHandler.instance(handlerID) : handler };
	}

	public MonitoredList<T> getMonitoredList() {
		InfoUUID id = new InfoUUID(this.getIdentity().hashCode(), 0);
		return getNetworkID() == -1 ? MonitoredList.newMonitoredList(getNetworkID()) : Logistics.getClientManager().getMonitoredList(getNetworkID(), id);
	}

	public int getMaxInfo() {
		return 4;
	}

	//// IChannelledTile \\\\

	@Override
	public IdentifiedChannelsList getChannels() {
		return list;
	}

	public UUID getIdentity() {
		return getUUID();
	}

	public void modifyCoords(IMonitorInfo info, int channelID) {
		if (info instanceof MonitoredBlockCoords) {
			lastSelected.setCoords(((MonitoredBlockCoords) info).syncCoords.getCoords());
			sendByteBufPacket(-3);
		}
		if (info instanceof MonitoredEntity) {
			lastSelectedUUID.setObject(((MonitoredEntity) info).uuid.getUUID());
			sendByteBufPacket(-4);
		}
	}

	//// ILogicViewable \\\\

	public ViewersList getViewersList() {
		return viewers;
	}

	@Override
	public void onViewerAdded(EntityPlayer player, List<ViewerTally> type) {
		SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
	}

	@Override
	public void onViewerRemoved(EntityPlayer player, List<ViewerTally> type) {
	}

	//// IOperatorProvider \\\\

	public void addInfo(List<String> info) {
		super.addInfo(info);
		info.add("Channels Configured: " + !list.hasChannels());
		info.add("Max Info: " + getMaxInfo());
	}

	//// EVENTS \\\\

	public void onLoaded() {
		super.onLoaded();
		Logistics.getInfoManager(this.getWorld().isRemote).addMonitor(this);
		if (isServer()) {
			updateAllInfo();
		}
	}

	public void onRemoved() {
		super.onRemoved();
		Logistics.getInfoManager(this.getWorld().isRemote).removeMonitor(this);
	}

	public void onUnloaded() {
		super.onUnloaded();
		Logistics.getInfoManager(this.getWorld().isRemote).removeMonitor(this);
	}

	public void onFirstTick() {
		super.onFirstTick();
		Logistics.getInfoManager(this.getWorld().isRemote).addMonitor(this);
		if (isServer()) {
			hasMonitor.setObject(LogisticsAPI.getCableHelper().getDisplayScreen(getCoords(), getFacing()) != null);
		}
	}

	@Override
	public boolean rotatePart(EnumFacing axis) {
		if (super.rotatePart(axis)) {
			hasMonitor.setObject(LogisticsAPI.getCableHelper().getDisplayScreen(getCoords(), getFacing()) != null);
			sendUpdatePacket(true);
			return true;
		}
		return false;
	}

	//// STATE \\\\

	@Override
	public IBlockState getActualState(IBlockState state) {
		return state.withProperty(ORIENTATION, getFacing()).withProperty(hasDisplay, this.hasMonitor.getObject());
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(MCMultiPartMod.multipart, new IProperty[] { ORIENTATION, hasDisplay });
	}

	//// PACKETS \\\\

	public final static int ADD = -9, PAIRED = -10, ALL = 100;

	@Override
	public void onPartChanged(IMultipart changedPart) {
		if (!this.getWorld().isRemote) {
			if (changedPart instanceof ScreenMultipart) {
				ScreenMultipart screen = (ScreenMultipart) changedPart;
				if (screen.face == getFacing()) {
					hasMonitor.setObject(!screen.wasRemoved());
					sendUpdatePacket(true);
				}
			}
		}
	}

	@Override
	public void writeUpdatePacket(PacketBuffer buf) {
		super.writeUpdatePacket(buf);
		hasMonitor.writeToBuf(buf);
	}

	@Override
	public void readUpdatePacket(PacketBuffer buf) {
		super.readUpdatePacket(buf);
		hasMonitor.readFromBuf(buf);
	}

	@Override
	public void writePacket(ByteBuf buf, int id) {
		switch (id) {
		case -4:
			lastSelectedUUID.writeToBuf(buf);
			break;
		case -3:
			lastSelected.writeToBuf(buf);
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		switch (id) {
		case -4:
			lastSelectedUUID.readFromBuf(buf);
			list.modifyUUID(lastSelectedUUID.getUUID());
			sendByteBufPacket(1);
			break;
		case -3:
			lastSelected.readFromBuf(buf);
			list.modifyCoords(lastSelected.getCoords());
			sendByteBufPacket(1);
			break;
		}
	}

	//// GUI \\\\

	public void onGuiOpened(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 1:
			viewers.addViewer(player, ViewerType.CHANNEL);
			break;
		}
	}
}
