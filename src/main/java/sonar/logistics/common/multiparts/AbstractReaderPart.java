package sonar.logistics.common.multiparts;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import mcmultipart.MCMultiPartMod;
import mcmultipart.api.multipart.IMultipart;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.listener.ListenableList;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncUUID;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2;
import sonar.logistics.api.PL2API;
import sonar.logistics.api.PL2Properties;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.networks.INetworkListHandler;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.ChannelList;
import sonar.logistics.api.tiles.readers.INetworkReader;
import sonar.logistics.api.utils.ChannelType;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.info.types.MonitoredBlockCoords;
import sonar.logistics.info.types.MonitoredEntity;
import sonar.logistics.network.sync.SyncMonitoredType;

public abstract class AbstractReaderPart<T extends IInfo> extends SidedPart implements INetworkReader<T>, IByteBufTile, IFlexibleGui {

	public static final int ADD = -9, PAIRED = -10, ALL = 100;

	public final ListenableList<PlayerListener> listeners = new ListenableList(this, ListenerType.ALL.size());
	public final ChannelList list = new ChannelList(getIdentity(), this.channelType(), -2);
	protected List<INetworkListHandler> validHandlers = null;
	public SyncMonitoredType<T> selectedInfo = new SyncMonitoredType<T>(-5);
	public SyncTagType.BOOLEAN hasMonitor = new SyncTagType.BOOLEAN(-4);
	public int lastPos = -1;

	public SyncUUID lastSelectedUUID = new SyncUUID(-10);
	public SyncCoords lastSelected = new SyncCoords(-11);
	{
		syncList.addParts(list, hasMonitor);
	}

	public abstract List<INetworkListHandler> addValidHandlers(List<INetworkListHandler> handlers);

	//// ILogicMonitor \\\\

	@Override
	public MonitoredList<T> getUpdatedList(InfoUUID uuid, Map<NodeConnection, MonitoredList<T>> channels, List<NodeConnection> usedChannels) {
		MonitoredList<T> updateList = MonitoredList.<T>newMonitoredList(getNetworkID());
		ChannelList readerChannels = getChannels();
		for (Entry<NodeConnection, MonitoredList<T>> entry : channels.entrySet()) {
			if ((entry.getValue() != null && !entry.getValue().isEmpty()) && readerChannels.isMonitored(entry.getKey())) {
				for (T coordInfo : (MonitoredList<T>) entry.getValue()) {
					if (canMonitorInfo(coordInfo, uuid, channels, usedChannels))
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

	public boolean canMonitorInfo(T info, InfoUUID uuid, Map<NodeConnection, MonitoredList<T>> channels, List<NodeConnection> usedChannels) {
		return true;
	}

	@Override
	public List<INetworkListHandler> getValidHandlers() {
		if (validHandlers == null) {
			validHandlers = addValidHandlers(Lists.newArrayList());
		}
		return validHandlers;
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
	public ChannelList getChannels() {
		return list;
	}

	public void sendCoordsToServer(IInfo info, int channelID) {
		if (info instanceof MonitoredBlockCoords) {
			lastSelected.setCoords(((MonitoredBlockCoords) info).getCoords());
			sendByteBufPacket(-3);
		}
		if (info instanceof MonitoredEntity) {
			lastSelectedUUID.setObject(((MonitoredEntity) info).getUUID());
			sendByteBufPacket(-4);
		}
	}

	//// ILogicViewable \\\\

	public ListenableList<PlayerListener> getListenerList() {
		return listeners;
	}

	@Override
	public void onListenerAdded(ListenerTally<PlayerListener> tally) {
		super.onListenerAdded(tally);
		SonarMultipartHelper.sendMultipartSyncToPlayer(this, tally.listener.player);
	}

	//// IOperatorProvider \\\\
	public void addInfo(List<String> info) {
		super.addInfo(info);
		info.add("Channels Configured: " + !list.hasChannels());
		info.add("Max Info: " + getMaxInfo());
		// info.add("UUID: " + getIdentity());
	}

	//// EVENTS \\\\
	public void validate() {
		super.validate();
		if (isServer()) {
			hasMonitor.setObject(PL2API.getCableHelper().getDisplayScreen(getCoords(), getCableFace()) != null);
		}
	}

	@Override
	public boolean rotatePart(EnumFacing axis) {
		if (super.rotatePart(axis)) {
			hasMonitor.setObject(PL2API.getCableHelper().getDisplayScreen(getCoords(), getCableFace()) != null);
			sendUpdatePacket(true);
			return true;
		}
		return false;
	}

	//// STATE \\\\

	@Override
	public IBlockState getActualState(IBlockState state) {
		return state.withProperty(PL2Properties.ORIENTATION, getCableFace()).withProperty(hasDisplay, this.hasMonitor.getObject());
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(MCMultiPartMod.multipart, new IProperty[] { PL2Properties.ORIENTATION, hasDisplay });
	}

	//// PACKETS \\\\

	@Override
	public void onPartChanged(IMultipart changedPart) {
		if (!this.getWorld().isRemote) {
			if (changedPart instanceof AbstractDisplayPart) {
				AbstractDisplayPart screen = (AbstractDisplayPart) changedPart;
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
			list.give(lastSelectedUUID.getUUID());
			sendByteBufPacket(1);
			break;
		case -3:
			lastSelected.readFromBuf(buf);
			list.give(lastSelected.getCoords());
			sendByteBufPacket(1);
			break;
		}
	}

	//// GUI \\\\

	public void onGuiOpened(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 1:
			getNetwork().sendConnectionsPacket(player);
			break;
		}
	}
}
