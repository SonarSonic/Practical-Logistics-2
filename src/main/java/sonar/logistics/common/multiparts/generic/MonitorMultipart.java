package sonar.logistics.common.multiparts.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

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
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.listener.ListenerList;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncUUID;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2;
import sonar.logistics.api.LogisticsAPI;
import sonar.logistics.api.cabling.ChannelType;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.readers.INetworkReader;
import sonar.logistics.api.readers.IdentifiedChannelsList;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredBlockCoords;
import sonar.logistics.connections.monitoring.MonitoredEntity;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.network.sync.SyncMonitoredType;

public abstract class MonitorMultipart<T extends IMonitorInfo> extends SidedMultipart implements INetworkReader<T>, IByteBufTile, IFlexibleGui {

	public ListenerList<PlayerListener> listeners = new ListenerList(this, ListenerType.ALL.size());

	public static final PropertyBool hasDisplay = PropertyBool.create("display");
	protected IdentifiedChannelsList list = new IdentifiedChannelsList(getIdentity(), this.channelType(), -2);
	public SyncTagType.BOOLEAN hasMonitor = new SyncTagType.BOOLEAN(-4);
	protected LogicMonitorHandler handler = null;
	protected String handlerID;
	public SyncMonitoredType<T> selectedInfo;
	public IMonitorInfo lastInfo = null;
	public int lastPos = -1;

	public SyncCoords lastSelected = new SyncCoords(-11);
	public SyncUUID lastSelectedUUID = new SyncUUID(-10);

	public MonitorMultipart(String handlerID) {
		super();
		this.handlerID = handlerID;
		this.syncList.addParts(list, hasMonitor);
		selectedInfo = new SyncMonitoredType<T>(-5);
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
		InfoUUID id = new InfoUUID(getIdentity(), 0);
		return getNetworkID() == -1 ? MonitoredList.newMonitoredList(getNetworkID()) : PL2.getClientManager().getMonitoredList(getNetworkID(), id);
	}

	public int getMaxInfo() {
		return 4;
	}

	//// IChannelledTile \\\\

	@Override
	public IdentifiedChannelsList getChannels() {
		return list;
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

	public ListenerList<PlayerListener> getListenerList() {
		return listeners;
	}

	@Override
	public void onListenerAdded(ListenerTally<PlayerListener> tally){
		SonarMultipartHelper.sendMultipartSyncToPlayer(this, tally.listener.player);
	}

	public void onListenerRemoved(ListenerTally<PlayerListener> tally){}

	//// IOperatorProvider \\\\

	public void addInfo(List<String> info) {
		super.addInfo(info);
		info.add("Channels Configured: " + !list.hasChannels());
		info.add("Max Info: " + getMaxInfo());
		//info.add("UUID: " + getIdentity());
	}

	//// EVENTS \\\\

	public void onLoaded() {
		super.onLoaded();
		PL2.getInfoManager(this.getWorld().isRemote).addInfoProvider(this);
	}

	public void onRemoved() {
		super.onRemoved();
		PL2.getInfoManager(this.getWorld().isRemote).removeInfoProvider(this);
	}

	public void onUnloaded() {
		super.onUnloaded();
		PL2.getInfoManager(this.getWorld().isRemote).removeInfoProvider(this);
	}

	public void onFirstTick() {
		super.onFirstTick();
		PL2.getInfoManager(this.getWorld().isRemote).addInfoProvider(this);
		if (isServer()) {
			hasMonitor.setObject(LogisticsAPI.getCableHelper().getDisplayScreen(getCoords(), getCableFace()) != null);
		}
	}

	@Override
	public boolean rotatePart(EnumFacing axis) {
		if (super.rotatePart(axis)) {
			hasMonitor.setObject(LogisticsAPI.getCableHelper().getDisplayScreen(getCoords(), getCableFace()) != null);
			sendUpdatePacket(true);
			return true;
		}
		return false;
	}

	//// STATE \\\\

	@Override
	public IBlockState getActualState(IBlockState state) {
		return state.withProperty(ORIENTATION, getCableFace()).withProperty(hasDisplay, this.hasMonitor.getObject());
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
				if (screen.face == getCableFace()) {
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
			listeners.addListener(player, ListenerType.CHANNEL);
			break;
		}
	}
}
