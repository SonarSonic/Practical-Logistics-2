package sonar.logistics.common.multiparts2;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.core.network.sync.SyncTagType.LONG;
import sonar.logistics.PL2;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.PL2API;
import sonar.logistics.api.capability.PL2Capabilities;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.networks.EmptyLogisticsNetwork;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkListener;
import sonar.logistics.api.operator.IOperatorProvider;
import sonar.logistics.api.states.TileMessage;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.tiles.cable.IDataCable;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.network.PacketChannels;
import sonar.logistics.network.sync.SyncTileMessages;

public abstract class TileLogistics extends TileSonarMultipart implements INetworkTile, INetworkListener, IOperatorProvider {

	public static final TileMessage[] defaultValidStates = new TileMessage[] { TileMessage.NO_NETWORK };
	public ILogisticsNetwork network = EmptyLogisticsNetwork.INSTANCE;
	private SyncTagType.INT identity = (INT) new SyncTagType.INT("identity").setDefault((int) -1);
	public SyncTagType.INT networkID = (INT) new SyncTagType.INT(0).setDefault(-1);
	public SyncTileMessages states = new SyncTileMessages(this, 101);

	{
		syncList.addParts(networkID, identity, states);
		states.markAllMessages(true);
	}

	public TileLogistics() {
		super();
	}

	public abstract EnumFacing getCableFace();

	public PL2Multiparts getMultipart() {
		if (this.getBlockType() instanceof BlockLogisticsMultipart) {
			return ((BlockLogisticsMultipart) getBlockType()).getMultipart();
		}
		return null;
	}

	public void sendNetworkCoordMap(EntityPlayer player) {
		if (isClient() || !network.isValid() || getNetworkID() == -1) {
			return;
		}
		MonitoredList<IInfo> coords = network.createConnectionsList(CacheType.ALL);
		NBTTagCompound coordTag = InfoHelper.writeMonitoredList(new NBTTagCompound(), coords.isEmpty(), coords.copyInfo(), SyncType.DEFAULT_SYNC);
		if (!coordTag.hasNoTags()) {
			PL2.network.sendTo(new PacketChannels(getNetworkID(), coordTag), (EntityPlayerMP) player);
		}
	}

	//// ILogicTile \\\\

	public int getIdentity() {
		if (identity.getObject() == -1) {
			identity.setObject(PL2.getServerManager().getNextIdentity());
		}
		return identity.getObject();
	}

	@Override
	public boolean isValid() {
		return !tileEntityInvalid;
	}

	@Override
	public void onFirstTick() {
		super.onFirstTick();
		if (this instanceof ILogicListenable)
			PL2.getInfoManager(getWorld().isRemote).addIdentityTile((ILogicListenable) this);
	}

	public void invalidate() {
		super.invalidate();
		if (this instanceof ILogicListenable)
			PL2.getInfoManager(getWorld().isRemote).removeIdentityTile((ILogicListenable) this);
	}

	@Override
	public void onNetworkConnect(ILogisticsNetwork network) {
		if (!this.network.isValid() || networkID.getObject() != network.getNetworkID()) {
			this.network = network;
			this.networkID.setObject(network.getNetworkID());
			states.markTileMessage(TileMessage.NO_NETWORK, false);
		}
	}

	@Override
	public void onNetworkDisconnect(ILogisticsNetwork network) {
		if (networkID.getObject() == network.getNetworkID()) {
			this.network = EmptyLogisticsNetwork.INSTANCE;
			this.networkID.setObject(-1);
			states.markTileMessage(TileMessage.NO_NETWORK, true);
		} else if (networkID.getObject() != -1) {
			PL2.logger.info("%s : attempted to disconnect from the wrong network with ID: %s expected %s", this, network.getNetworkID(), networkID.getObject());
		}
	}

	//// LISTENERS \\\\

	public void onListenerAdded(ListenerTally<PlayerListener> tally) {}

	public void onListenerRemoved(ListenerTally<PlayerListener> tally) {}

	public void onSubListenableAdded(ISonarListenable<PlayerListener> listen) {}

	public void onSubListenableRemoved(ISonarListenable<PlayerListener> listen) {}

	public ILogisticsNetwork getNetwork() {
		return network;
	}

	public int getNetworkID() {
		return networkID.getObject();
	}

	//// IOperatorProvider \\\\

	public void updateOperatorInfo() {
		this.requestSyncPacket();
	}

	public void addInfo(List<String> info) {
		// info.add(TextFormatting.UNDERLINE + getMultipart().getDisplayName());
		// FIXME
		info.add("Network ID: " + networkID.getObject());
		info.add("Has channels: " + (this instanceof TileInfoReader));
		info.add("IDENTITY: " + this.getIdentity());
	}

	@Override
	public TileMessage[] getValidMessages() {
		return defaultValidStates;
	}
}
