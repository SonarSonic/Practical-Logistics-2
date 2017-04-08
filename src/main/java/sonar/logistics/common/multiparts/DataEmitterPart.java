package sonar.logistics.common.multiparts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.helpers.NBTHelper;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.listener.ListenerList;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.STRING;
import sonar.core.network.sync.SyncUUID;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2;
import sonar.logistics.PL2Items;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.api.wireless.DataEmitterSecurity;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.client.gui.GuiDataEmitter;
import sonar.logistics.common.multiparts.generic.WirelessPart;
import sonar.logistics.connections.CacheHandler;
import sonar.logistics.connections.managers.EmitterManager;
import sonar.logistics.connections.monitoring.FluidMonitorHandler;
import sonar.logistics.connections.monitoring.ItemMonitorHandler;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredFluidStack;
import sonar.logistics.connections.monitoring.MonitoredItemStack;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.LogisticsHelper;

public class DataEmitterPart extends WirelessPart implements IDataEmitter, IFlexibleGui, IByteBufTile {

	public static int STATIC_ITEM_ID = -16;
	public static int STATIC_FLUID_ID = -17;
	public static final String UNNAMED = "Unnamed Emitter";
	public ListenerList<PlayerListener> listeners = new ListenerList(this, ListenerType.ALL.size());
	public ArrayList<IDataReceiver> receivers = new ArrayList();
	public LogicMonitorHandler[] validHandlers;
	public SyncTagType.STRING emitterName = (STRING) new SyncTagType.STRING(2).setDefault(UNNAMED);
	public SyncEnum<DataEmitterSecurity> security = new SyncEnum(DataEmitterSecurity.values(), 5);

	{
		syncList.addParts(emitterName, security);
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack heldItem, PartMOP hit) {
		if (!LogisticsHelper.isPlayerUsingOperator(player)) {
			if (isServer()) {
				openFlexibleGui(player, 0);
			}
			return true;
		}
		return false;
	}

	@Override
	public ArrayList<Integer> getWatchingNetworks() {
		ArrayList<Integer> networks = new ArrayList();
		Iterator<IDataReceiver> iterator = receivers.iterator();
		while (iterator.hasNext()) {
			IDataReceiver receiver = iterator.next();
			if (!receiver.isValid()) {
				int id = receiver.getNetworkID();
				if (!networks.contains(id)) {
					networks.add(id);
				}
			} else {
				iterator.remove();
			}
		}
		return networks;
	}

	//// IDataEmitter \\\\

	@Override
	public boolean canPlayerConnect(UUID uuid) {
		return playerUUID.getUUID().equals(uuid);
	}

	@Override
	public String getEmitterName() {
		return emitterName.getObject();
	}

	@Override
	public DataEmitterSecurity getSecurity() {
		return security.getObject();
	}

	@Override
	public void connect(IDataReceiver receiver) {
		if (!receivers.contains(receiver)) {
			receivers.add(receiver);
			network.markCacheDirty(CacheHandler.EMITTERS);
		}
	}

	@Override
	public void disconnect(IDataReceiver receiver) {
		if (receivers.remove(receiver)) {
			network.markCacheDirty(CacheHandler.EMITTERS);
		}
	}

	@Override
	public MonitoredList<MonitoredItemStack> getServerItems() {
		return PL2.getServerManager().getMonitoredList(this.getNetworkID(), new InfoUUID(this.getIdentity(), STATIC_ITEM_ID));
	}

	@Override
	public MonitoredList<MonitoredFluidStack> getServerFluids() {
		return PL2.getServerManager().getMonitoredList(this.getNetworkID(), new InfoUUID(this.getIdentity(), STATIC_FLUID_ID));
	}

	//// PACKETS \\\\
	@Override
	public void writePacket(ByteBuf buf, int id) {
		ISyncPart part = NBTHelper.getSyncPartByID(syncList.getStandardSyncParts(), id);
		if (part != null)
			part.writeToBuf(buf);
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		ISyncPart part = NBTHelper.getSyncPartByID(syncList.getStandardSyncParts(), id);
		if (part != null)
			part.readFromBuf(buf);

		if (id == 5) {
			EmitterManager.emitterChanged(this);
		}
	}

	//// GUI \\\\

	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new ContainerMultipartSync(this) : null;
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new GuiDataEmitter(this) : null;
	}

	@Override
	public void onGuiOpened(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			break;
		}
	}

	public ListenerList<PlayerListener> getListenerList() {
		return listeners;
	}

	@Override
	public void onListenerAdded(ListenerTally<PlayerListener> tally) {}

	public void onListenerRemoved(ListenerTally<PlayerListener> tally) {}

	@Override
	public MonitoredList sortMonitoredList(MonitoredList updateInfo, int channelID) {
		return updateInfo;
	}

	@Override
	public MonitoredList<IMonitorInfo> getUpdatedList(int infoID, Map<NodeConnection, MonitoredList<?>> channels, ArrayList<NodeConnection> usedChannels) {
		MonitoredList updateList = MonitoredList.newMonitoredList(getNetworkID());
		for (Entry<NodeConnection, MonitoredList<?>> entry : channels.entrySet()) {
			if ((entry.getValue() != null && !entry.getValue().isEmpty())) {
				for (IMonitorInfo coordInfo : (MonitoredList<IMonitorInfo>) entry.getValue()) {
					updateList.addInfoToList(coordInfo, (MonitoredList<IMonitorInfo>) entry.getValue());
				}
				updateList.sizing.add(entry.getValue().sizing);
				usedChannels.add(entry.getKey());
			}
		}
		return updateList;
	}

	@Override
	public LogicMonitorHandler[] getValidHandlers() {
		if (validHandlers == null) {
			validHandlers = new LogicMonitorHandler[] { ItemMonitorHandler.instance(), FluidMonitorHandler.instance() };
		}
		return validHandlers;
	}

	@Override
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.DATA_EMITTER;
	}
}
