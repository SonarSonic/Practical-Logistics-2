package sonar.logistics.common.multiparts.wireless;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

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
import sonar.logistics.api.networks.INetworkChannels;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.networks.INetworkListHandler;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.api.wireless.EnumConnected;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IWirelessEmitter;
import sonar.logistics.api.wireless.IWirelessManager;
import sonar.logistics.api.wireless.IWirelessReceiver;
import sonar.logistics.api.wireless.WirelessSecurity;
import sonar.logistics.client.gui.GuiWirelessEmitter;
import sonar.logistics.info.types.MonitoredFluidStack;
import sonar.logistics.info.types.MonitoredItemStack;
import sonar.logistics.networking.PL2ListenerList;
import sonar.logistics.networking.channels.ListNetworkChannels;
import sonar.logistics.networking.handlers.FluidNetworkHandler;
import sonar.logistics.networking.handlers.ItemNetworkHandler;

public abstract class TileAbstractEmitter<E extends IWirelessEmitter, R extends IWirelessReceiver> extends TileAbstractWireless implements IWirelessEmitter, IFlexibleGui, IByteBufTile {

	public static final String UNNAMED = "Unnamed Emitter";
	public PL2ListenerList listeners = new PL2ListenerList(this, ListenerType.ALL.size());
	public SyncTagType.STRING emitterName = (STRING) new SyncTagType.STRING(2).setDefault(UNNAMED);
	public SyncEnum<WirelessSecurity> security = new SyncEnum(WirelessSecurity.values(), 5);

	{
		syncList.addParts(emitterName, security);
	}

	public abstract IWirelessManager<E, R> getWirelessHandler();

	//// IWirelessEmitter \\\\
	@Override
	public EnumConnected canPlayerConnect(UUID uuid) {
		return EnumConnected.fromBoolean(playerUUID.getUUID().equals(uuid));
	}

	@Override
	public String getEmitterName() {
		return emitterName.getObject();
	}

	@Override
	public WirelessSecurity getSecurity() {
		return security.getObject();
	}

	//// GUI \\\\

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new GuiWirelessEmitter(this) : null;
	}
	
	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new ContainerMultipartSync(this) : null;
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
		WirelessSecurity oldSetting = getSecurity();
		ISyncPart part = NBTHelper.getSyncPartByID(syncList.getStandardSyncParts(), id);
		if (part != null)
			part.readFromBuf(buf);
		if (id == 5) {
			getWirelessHandler().onEmitterSecurityChanged((E) this, oldSetting);
		}
	}

	//// GUI \\\\
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

}
