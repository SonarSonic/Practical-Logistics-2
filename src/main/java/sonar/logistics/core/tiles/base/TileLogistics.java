package sonar.logistics.core.tiles.base;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import sonar.core.handlers.inventories.ItemStackHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.PL2;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.core.items.operator.IOperatorProvider;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.channels.PacketChannels;
import sonar.logistics.base.events.LogisticsEventHandler;
import sonar.logistics.base.events.types.NetworkPartEvent;
import sonar.logistics.base.guidance.errors.ErrorMessage;
import sonar.logistics.base.listeners.ILogicListenable;
import sonar.logistics.base.tiles.INetworkTile;
import sonar.logistics.base.utils.CacheType;
import sonar.logistics.base.utils.PL2AdditionType;
import sonar.logistics.base.utils.PL2RemovalType;
import sonar.logistics.core.tiles.connections.data.network.EmptyLogisticsNetwork;
import sonar.logistics.core.tiles.displays.info.types.channels.MonitoredBlockCoords;
import sonar.logistics.core.tiles.displays.info.types.general.InfoChangeableList;
import sonar.logistics.core.tiles.readers.info.TileInfoReader;
import sonar.logistics.core.tiles.readers.info.handling.InfoHelper;
import sonar.logistics.network.sync.SyncTileMessages;

import java.util.List;

public abstract class TileLogistics extends TileSonarMultipart implements INetworkTile, IOperatorProvider {

	//very important, used for identifying this tile within the handling, don't alter unless you know what you're doing
	public int identity = -1;
	public static final ErrorMessage[] defaultValidStates = new ErrorMessage[] { ErrorMessage.NO_NETWORK };
	public ILogisticsNetwork network = EmptyLogisticsNetwork.INSTANCE;
	public SyncTagType.INT networkID = (INT) new SyncTagType.INT(0).setDefault(-1);
	public SyncTileMessages states = new SyncTileMessages(this, 101);

	{
		syncList.addParts(networkID, states);
		states.markAllMessages(true);
	}

	public abstract EnumFacing getCableFace();

	public PL2Multiparts getMultipart() {
		if (this.getBlockType() instanceof BlockLogistics) {
			return ((BlockLogistics) getBlockType()).getMultipart();
		}
		return null;
	}

	public void sendNetworkCoordMap(EntityPlayer player) {
		if (isClient() || !network.isValid() || getNetworkID() == -1) {
			return;
		}
		InfoChangeableList<MonitoredBlockCoords> coords = network.createConnectionsList(CacheType.ALL);
		NBTTagCompound coordTag = InfoHelper.writeMonitoredList(new NBTTagCompound(), coords, SyncType.DEFAULT_SYNC);
		if (!coordTag.hasNoTags()) {
			PL2.network.sendTo(new PacketChannels(getNetworkID(), coordTag), (EntityPlayerMP) player);
		}
	}

	//// ILogicTile \\\\

	public int getIdentity() {
		if (identity == -1 && this.isServer()) {
			identity = ServerInfoHandler.instance().getNextIdentity();
			markDirty();
		}
		return identity;
	}

	public ItemStack getDisplayStack() {
		return ItemStackHelper.getBlockItem(getWorld(), getPos());
	}

	@Override
	public boolean isValid() {
		return !tileEntityInvalid;
	}

	public void doAdditionEvent(PL2AdditionType type) {
		if (!getWorld().isRemote){
			LogisticsEventHandler.instance().queueNetworkAddition(this, type);
		}else{
			MinecraftForge.EVENT_BUS.post(new NetworkPartEvent.AddedPart(this, this.getActualWorld(), type));
		}
	}

	public void doRemovalEvent(PL2RemovalType type) {
		if (!getWorld().isRemote){
			LogisticsEventHandler.instance().queueNetworkRemoval(this, type);
		}else{
			MinecraftForge.EVENT_BUS.post(new NetworkPartEvent.RemovedPart(this, this.getActualWorld(), type));
		}
	}

	public boolean PART_ADDED = false;

	public void doChunkLoadEvent() {
		if (!PART_ADDED) {
			doAdditionEvent(PL2AdditionType.CHUNK_LOADED);
			PART_ADDED = true;
		}
	}

	public void doChunkUnloadEvent() {
		if (PART_ADDED) {
			doRemovalEvent(PL2RemovalType.CHUNK_UNLOADED);
			PART_ADDED = false;
		}
	}

	@Override
	public void onFirstTick() {
		super.onFirstTick();
		if (isServer()) { // only on server, the client is only added when identity is known
			doChunkLoadEvent();
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (getIdentity() != -1) {
			doChunkUnloadEvent();
		}
		if(this instanceof ILogicListenable){
			((ILogicListenable) this).getListenerList().invalidateList();
		}
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag){
		super.handleUpdateTag(tag);
		if (isClient()) {
			doChunkLoadEvent();
		}
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		super.onDataPacket(net, packet);
		if (isClient()) {
			doChunkLoadEvent();
		}
	}

	@Override
	public final void onNetworkConnect(ILogisticsNetwork network) {
		if (!this.network.isValid() || networkID.getObject() != network.getNetworkID()) {
			this.network = network;
			this.networkID.setObject(network.getNetworkID());
			states.markTileMessage(ErrorMessage.NO_NETWORK, false);
			// doAdditionEvent(PL2AdditionType.NETWORK_CONNECTED);
		}
	}

	@Override
	public final void onNetworkDisconnect(ILogisticsNetwork network) {
		if (networkID.getObject() == network.getNetworkID()) {
			this.network = EmptyLogisticsNetwork.INSTANCE;
			this.networkID.setObject(-1);
			states.markTileMessage(ErrorMessage.NO_NETWORK, true);
			// doRemovalEvent(PL2RemovalType.NETWORK_DISCONNECTED);
		} else if (networkID.getObject() != -1) {
			PL2.logger.info("%s : attempted to disconnect from the wrong handling with ID: %s expected %s", this, network.getNetworkID(), networkID.getObject());
		}
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		identity = nbt.getInteger("identity");
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		nbt.setInteger("identity", getIdentity());
		return super.writeData(nbt, type);
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
		PL2Multiparts multipart = getMultipart();
		if (multipart != null)
			info.add(TextFormatting.UNDERLINE + multipart.getDisplayName());
		info.add("Network ID: " + networkID.getObject());
		info.add("Has channels: " + (this instanceof TileInfoReader));
		info.add("IDENTITY: " + getIdentity());
	}

	@Override
	public ErrorMessage[] getValidMessages() {
		return defaultValidStates;
	}
}
