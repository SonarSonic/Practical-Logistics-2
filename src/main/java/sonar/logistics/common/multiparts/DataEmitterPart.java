package sonar.logistics.common.multiparts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.helpers.NBTHelper;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.STRING;
import sonar.core.network.sync.SyncUUID;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.Logistics;
import sonar.logistics.LogisticsItems;
import sonar.logistics.api.LogisticsAPI;
import sonar.logistics.api.cabling.ChannelType;
import sonar.logistics.api.connecting.ILogisticsNetwork;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.readers.IListReader;
import sonar.logistics.api.readers.IdentifiedChannelsList;
import sonar.logistics.api.utils.LogisticsHelper;
import sonar.logistics.api.viewers.IViewersList;
import sonar.logistics.api.viewers.ViewerTally;
import sonar.logistics.api.viewers.ViewerType;
import sonar.logistics.api.viewers.ViewersList;
import sonar.logistics.api.wireless.DataEmitterSecurity;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.client.gui.GuiDataEmitter;
import sonar.logistics.connections.managers.EmitterManager;
import sonar.logistics.connections.monitoring.FluidMonitorHandler;
import sonar.logistics.connections.monitoring.ItemMonitorHandler;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredFluidStack;
import sonar.logistics.connections.monitoring.MonitoredItemStack;
import sonar.logistics.connections.monitoring.MonitoredList;

public class DataEmitterPart extends SidedMultipart implements IDataEmitter, IFlexibleGui, IByteBufTile {

	public ViewersList viewers = new ViewersList(this, ViewerType.ALL);
	public ArrayList<IDataReceiver> receivers = new ArrayList();
	public LogicMonitorHandler[] validHandlers;
	public SyncTagType.STRING emitterName = (STRING) new SyncTagType.STRING(2).setDefault("Unnamed Emitter");
	public SyncUUID playerUUID = new SyncUUID(3);
	public SyncEnum<DataEmitterSecurity> security = new SyncEnum(DataEmitterSecurity.values(), 5);
	public static int STATIC_ITEM_ID = -16;
	public static int STATIC_FLUID_ID = -17;

	{
		syncList.addParts(emitterName, playerUUID, security);
	}

	public DataEmitterPart() {
		super(0.0625 * 5, 0.0625 / 2, 0.0625 * 4);
	}

	public DataEmitterPart(EntityPlayer player, EnumFacing dir) {
		super(dir, 0.0625 * 5, 0.0625 / 2, 0.0625 * 4);
		playerUUID.setObject(player.getGameProfile().getId());
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
	public ArrayList<Integer> getNetworks() {
		ArrayList<Integer> networks = new ArrayList();
		for (IDataReceiver receiver : receivers) {
			int id = receiver.getNetworkID();
			if (!networks.contains(id)) {
				networks.add(id);
			}
		}
		return networks;
	}

	@Override
	public UUID getIdentity() {
		return getUUID();// emitterUUID.getUUID();
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
		receivers.add(receiver);
	}

	@Override
	public void disconnect(IDataReceiver receiver) {
		receivers.remove(receiver);
	}

	@Override
	public MonitoredList<MonitoredItemStack> getServerItems() {
		return Logistics.getServerManager().getMonitoredList(this.getNetworkID(), new InfoUUID(this.getIdentity().hashCode(), STATIC_ITEM_ID));
	}

	@Override
	public MonitoredList<MonitoredFluidStack> getServerFluids() {
		return Logistics.getServerManager().getMonitoredList(this.getNetworkID(), new InfoUUID(this.getIdentity().hashCode(), STATIC_FLUID_ID));
	}
	//// EVENTS \\\\

	public void onLoaded() {
		super.onLoaded();
		Logistics.getInfoManager(this.getWorld().isRemote).addMonitor(this);
		if (isServer()) {
			EmitterManager.addEmitter(this);
		}
	}

	public void onRemoved() {
		super.onRemoved();
		Logistics.getInfoManager(this.getWorld().isRemote).removeMonitor(this);
		if (isServer()) {
			EmitterManager.removeEmitter(this);
		}
	}

	public void onUnloaded() {
		super.onUnloaded();
		Logistics.getInfoManager(this.getWorld().isRemote).removeMonitor(this);
		if (isServer()) {
			EmitterManager.removeEmitter(this);
		}
	}

	public void onFirstTick() {
		super.onFirstTick();
		Logistics.getInfoManager(this.getWorld().isRemote).addMonitor(this);
		if (isServer()) {
			sendByteBufPacket(playerUUID.id);
			EmitterManager.addEmitter(this);
		}
	}
	//// PACKETS \\\\

	public void setLocalNetworkCache(INetworkCache network) {
		if (!this.network.isFakeNetwork() && this.network.getNetworkID() != network.getNetworkID()) {
			((ILogisticsNetwork) this.network).removeMonitor(this);
		}
		super.setLocalNetworkCache(network);
		if (network instanceof ILogisticsNetwork) {
			ILogisticsNetwork storageCache = (ILogisticsNetwork) network;
			storageCache.addMonitor(this);
		}
		if (network.getNetworkID() != this.getNetworkID())
			EmitterManager.emitterChanged(this);
	}

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

	@Override
	public ItemStack getItemStack() {
		return new ItemStack(LogisticsItems.partEmitter);
	}

	@Override
	public IViewersList getViewersList() {
		return viewers;
	}

	@Override
	public void onViewerAdded(EntityPlayer player, List<ViewerTally> arrayList) {
	}

	@Override
	public void onViewerRemoved(EntityPlayer player, List<ViewerTally> arrayList) {
	}

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
}
