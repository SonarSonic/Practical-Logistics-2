package sonar.logistics.common.multiparts.wireless;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.helpers.NBTHelper;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.STRING;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.UniversalChangeableList;
import sonar.logistics.api.networks.INetworkChannels;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.networks.INetworkListHandler;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.api.wireless.DataEmitterSecurity;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.client.gui.GuiDataEmitter;
import sonar.logistics.info.types.MonitoredFluidStack;
import sonar.logistics.info.types.MonitoredItemStack;
import sonar.logistics.networking.PL2ListenerList;
import sonar.logistics.networking.channels.ListNetworkChannels;
import sonar.logistics.networking.connections.WirelessDataHandler;
import sonar.logistics.networking.handlers.FluidNetworkHandler;
import sonar.logistics.networking.handlers.ItemNetworkHandler;

public class TileDataEmitter extends TileAbstractWireless implements IDataEmitter, IFlexibleGui, IByteBufTile {

	public static int STATIC_ITEM_ID = -16;
	public static int STATIC_FLUID_ID = -17;
	public static final String UNNAMED = "Unnamed Emitter";
	public PL2ListenerList listeners = new PL2ListenerList(this, ListenerType.ALL.size());
	public List<INetworkListHandler> validHandlers;
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
	public AbstractChangeableList<MonitoredItemStack> getServerItems() {
		return PL2.getInfoManager(world.isRemote).getMonitoredList(new InfoUUID(this.getIdentity(), STATIC_ITEM_ID));
	}

	@Override
	public AbstractChangeableList<MonitoredFluidStack> getServerFluids() {
		return PL2.getInfoManager(world.isRemote).getMonitoredList(new InfoUUID(this.getIdentity(), STATIC_FLUID_ID));
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
		DataEmitterSecurity oldSetting = getSecurity();
		ISyncPart part = NBTHelper.getSyncPartByID(syncList.getStandardSyncParts(), id);
		if (part != null)
			part.readFromBuf(buf);

		if (id == 5) {
			WirelessDataHandler.onEmitterSecurityChanged(this, oldSetting);
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

	public PL2ListenerList getListenerList() {
		return listeners;
	}

	@Override
	public AbstractChangeableList sortMonitoredList(AbstractChangeableList updateInfo, int channelID) {
		return updateInfo;
	}

	public void sendRapidUpdate(EntityPlayer player) {
		SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
		for (INetworkHandler handler : getValidHandlers()) {
			INetworkChannels list = network.getNetworkChannels(handler.getChannelsType());
			if (list != null && list instanceof ListNetworkChannels) {
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
	public List<INetworkListHandler> getValidHandlers() {
		if (validHandlers == null) {
			validHandlers = Lists.newArrayList(ItemNetworkHandler.INSTANCE, FluidNetworkHandler.INSTANCE);
		}
		return validHandlers;
	}

}
