package sonar.logistics.common.multiparts;

import java.util.List;

import com.google.common.collect.Lists;

import mcmultipart.raytrace.PartMOP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipart;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.PL2;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.PL2API;
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
import sonar.logistics.common.multiparts.readers.InfoReaderPart;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.network.PacketChannels;
import sonar.logistics.network.sync.SyncTileMessages;

public abstract class LogisticsPart extends SonarMultipart implements INetworkTile, INetworkListener, IOperatorProvider {

	public static final TileMessage[] defaultValidStates = new TileMessage[] { TileMessage.NO_NETWORK };
	public ILogisticsNetwork network = EmptyLogisticsNetwork.INSTANCE;
	public SyncTagType.INT identity = (INT) new SyncTagType.INT(100).setDefault(-1);
	public SyncTileMessages states = new SyncTileMessages(this, 101);
	public SyncTagType.INT networkID = (INT) new SyncTagType.INT(0).setDefault(-1);
	{
		syncList.addParts(networkID, identity, states);
		states.markAllMessages(true);
	}

	public LogisticsPart() {
		super();
	}

	public LogisticsPart(AxisAlignedBB collisionBox) {
		super(collisionBox);
	}

	public abstract EnumFacing getCableFace();
	public abstract PL2Multiparts getMultipart();

	@Override
	public ItemStack getItemStack() {
		return getMultipart().getItem();
	}

	public String getDisplayName() {
		return getMultipart().getDisplayName();
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

	public boolean hasStandardGui() {
		return false;
	}
	
	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack heldItem, PartMOP hit) {
		if (hasStandardGui() && canOpenGui(player)) {
			if (isServer()) {
				openFlexibleGui(player, 0);
			}
			return true;
		}
		return false;
	}

	public boolean canOpenGui(EntityPlayer player) {
		if (LogisticsHelper.isPlayerUsingOperator(player)) {
			return false;
		}
		/*
		TileMessage message = states.canOpenGui();
		if (message != null) {
			FontHelper.sendMessage(message.message.o(), player.getEntityWorld(), player);
			return false;
		}
		*/
		return true;
	}

	//// ILogicTile \\\\

	public int getIdentity() {
		if (identity.getObject() == -1 || identity.getObject() == 0) {
			identity.setObject(getUUID().hashCode());
		}
		return identity.getObject();
	}

	@Override
	public void validate() {
		super.validate();
		if (!this.getWorld().isRemote) {
			IDataCable cable = PL2API.getCableHelper().getCableFromCoords(this.getCoords());
			if (cable != null)
				cable.onConnectionAdded(this, getCableFace());
		}
		if (this instanceof ILogicListenable)
			PL2.getInfoManager(this.getWorld().isRemote).addIdentityTile((ILogicListenable) this);
	}

	public void invalidate() {
		super.invalidate();
		if (this instanceof ILogicListenable)
			PL2.getInfoManager(this.getWorld().isRemote).removeIdentityTile((ILogicListenable) this);
	}

	@Override
	public void onRemoved() {
		super.onRemoved();
		this.onUnloaded();
	}

	@Override
	public void onUnloaded() {
		super.onUnloaded();
		if (!this.getWorld().isRemote) {
			IDataCable cable = PL2API.getCableHelper().getCableFromCoords(this.getCoords());
			if (cable != null)
				cable.onConnectionRemoved(this, getCableFace());
		}
	}

	public boolean isValid() {
		return !wasRemoved;
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
		} else {
			PL2.logger.info("%s : attempted to disconnect from the wrong network with ID: %s expected %s", this, network.getNetworkID(), networkID.getObject());
		}
	}

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
		info.add(TextFormatting.UNDERLINE + getMultipart().getDisplayName());
		info.add("Network ID: " + networkID.getObject());
		info.add("Has channels: " + (this instanceof InfoReaderPart));
		info.add("IDENTITY: " + this.getIdentity());
	}

	@Override
	public List<ItemStack> getDrops() {
		return Lists.newArrayList(this.getItemStack());
	}

	@Override
	public TileMessage[] getValidMessages() {
		return defaultValidStates;
	}
}
