package sonar.logistics.common.multiparts.wireless;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.helpers.NBTHelper;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ListenableList;
import sonar.core.listener.ListenerList;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.STRING;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.networks.INetworkChannels;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.states.TileMessage;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.api.wireless.DataEmitterSecurity;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.client.gui.GuiDataEmitter;
import sonar.logistics.connections.CacheHandler;
import sonar.logistics.connections.channels.ListNetworkChannels;
import sonar.logistics.connections.handlers.DefaultNetworkHandler;
import sonar.logistics.connections.handlers.FluidNetworkHandler;
import sonar.logistics.connections.handlers.ItemNetworkHandler;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.info.types.MonitoredFluidStack;
import sonar.logistics.info.types.MonitoredItemStack;
import sonar.logistics.managers.WirelessManager;

public class DataEmitterPart extends AbstractWirelessPart implements IDataEmitter, IFlexibleGui, IByteBufTile {

	public static int STATIC_ITEM_ID = -16;
	public static int STATIC_FLUID_ID = -17;
	public static final String UNNAMED = "Unnamed Emitter";
	public ListenableList<PlayerListener> listeners = new ListenableList(this, ListenerType.ALL.size());
	public List<INetworkHandler> validHandlers;
	public SyncTagType.STRING emitterName = (STRING) new SyncTagType.STRING(2).setDefault(UNNAMED);
	public SyncEnum<DataEmitterSecurity> security = new SyncEnum(DataEmitterSecurity.values(), 5);

	{
		syncList.addParts(emitterName, security);
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
			WirelessManager.emitterChanged(this);
		}
	}

	//// GUI \\\\

	public boolean hasStandardGui() {
		return true;
	}

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

	public ListenableList<PlayerListener> getListenerList() {
		return listeners;
	}

	@Override
	public MonitoredList sortMonitoredList(MonitoredList updateInfo, int channelID) {
		return updateInfo;
	}

	public void sendRapidUpdate(EntityPlayer player) {
		SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
		for (INetworkHandler handler : getValidHandlers()) {
			INetworkChannels list = network.getNetworkChannels(handler);
			if (list != null && list instanceof ListNetworkChannels) {
				((ListNetworkChannels) list).sendLocalRapidUpdate(this, player);
			}
		}
	}

	@Override
	public MonitoredList<IInfo> getUpdatedList(InfoUUID uuid, Map<NodeConnection, MonitoredList<IInfo>> channels, List<NodeConnection> usedChannels) {
		MonitoredList updateList = MonitoredList.newMonitoredList(getNetworkID());
		for (Entry<NodeConnection, MonitoredList<IInfo>> entry : channels.entrySet()) {
			if ((entry.getValue() != null && !entry.getValue().isEmpty())) {
				for (IInfo coordInfo : (MonitoredList<IInfo>) entry.getValue()) {
					updateList.addInfoToList(coordInfo, (MonitoredList<IInfo>) entry.getValue());
				}
				updateList.sizing.add(entry.getValue().sizing);
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
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.DATA_EMITTER;
	}
}
