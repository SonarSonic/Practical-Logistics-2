package sonar.logistics.common.multiparts.wireless;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.PL2;
import sonar.logistics.api.cabling.CableConnectionType;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.cabling.IRedstoneConnectable;
import sonar.logistics.api.networking.IRedstoneNetwork;
import sonar.logistics.api.tiles.signaller.SignallerModes;
import sonar.logistics.api.wireless.IRedstoneEmitter;
import sonar.logistics.api.wireless.IRedstoneReceiver;
import sonar.logistics.api.wireless.IWirelessManager;
import sonar.logistics.client.gui.GuiRedstoneReceiver;
import sonar.logistics.common.multiparts.cables.TileRedstoneCable;
import sonar.logistics.networking.cabling.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TileRedstoneReceiver extends TileAbstractReceiver<IRedstoneEmitter, IRedstoneReceiver> implements IRedstoneReceiver, IRedstoneConnectable {

	public IRedstoneNetwork rNetwork = EmptyRedstoneNetwork.INSTANCE;
	public static final Function<IRedstoneEmitter, Boolean> EMITTER_FUNC = e -> e.getRedstoneNetwork().getActualPower() > 0;
	public SyncTagType.INT currentPower = new SyncTagType.INT(0);
	public SyncEnum<SignallerModes> mode = new SyncEnum(SignallerModes.values(), 1);

	@Override
	public IWirelessManager getWirelessHandler() {
		return WirelessRedstoneManager.instance();
	}

	public void updatePower() {
		if (isServer()) {
			boolean power = mode.getObject().checkList(getAllNetworks(), n -> {
				return n.getLocalPower() > 0; //no need to check global as this IS ALL CONNECTED NETWORKS
			});
			int toSet = (power ? 15 : 0);// should we make it sensitive to total redstone power also?
			if (currentPower.getObject() != toSet) {
				currentPower.setObject(toSet);
				TileRedstoneCable cable = RedstoneCableHelper.getCable(world, pos);
				if (cable != null) {
					RedstoneConnectionHandler.instance().markPowerForUpdate(cable.getRegistryID());
				}
			}
		}
	}

	public List<IRedstoneNetwork> getAllNetworks() {
		List<IRedstoneNetwork> networks = new ArrayList<>();
		List<Integer> connected = getConnectedNetworks();
		connected.forEach(id -> RedstoneNetwork.addSubNetworks(networks, RedstoneConnectionHandler.instance().getNetwork(id), IRedstoneNetwork.CONNECTED_NETWORK));
		networks.remove(rNetwork);
		return networks;
	}

	/* @Override public void onEmitterPowerChanged(IRedstoneEmitter emitter) { updatePower(); } */
	@Override
	public void onEmitterDisconnected(IRedstoneEmitter emitter) {
		// updatePower();
		WirelessRedstoneManager.instance().disconnectNetworks(getRedstoneNetwork(), emitter.getRedstoneNetwork());
		RedstoneConnectionHandler.instance().markPowerForUpdate(rNetwork.getNetworkID());
	}

	@Override
	public void onEmitterConnected(IRedstoneEmitter emitter) {
		// updatePower();
		WirelessRedstoneManager.instance().connectNetworks(getRedstoneNetwork(), emitter.getRedstoneNetwork());
		RedstoneConnectionHandler.instance().markPowerForUpdate(rNetwork.getNetworkID());
	}

	@Override
	public void onNetworkConnect(IRedstoneNetwork network) {
		if (!this.rNetwork.isValid() || networkID.getObject() != network.getNetworkID()) {
			this.rNetwork = network;
			this.networkID.setObject(network.getNetworkID());
			// states.markTileMessage(TileMessage.NO_NETWORK, false);
		}
	}

	@Override
	public void onNetworkDisconnect(IRedstoneNetwork network) {
		if (networkID.getObject() == network.getNetworkID()) {
			this.rNetwork = EmptyRedstoneNetwork.INSTANCE;
			this.networkID.setObject(-1);
			// states.markTileMessage(TileMessage.NO_NETWORK, true);
		} else if (networkID.getObject() != -1) {
			PL2.logger.info("%s : attempted to disconnect from the wrong network with ID: %s expected %s", this, network.getNetworkID(), networkID.getObject());
		}
	}

	@Override
	public int getRedstonePower() {
		return currentPower.getObject();
	}

	@Override
	public CableConnectionType canConnect(int registryID, ConnectableType type, EnumFacing dir, boolean internal) {
		if (!type.isRedstone()) {
			return CableConnectionType.NONE;
		}
		EnumFacing toCheck = internal ? dir : dir.getOpposite();
		return toCheck == getCableFace() ? CableConnectionType.NETWORK : CableConnectionType.NONE;
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new GuiRedstoneReceiver(this) : null;
	}

	//// PACKETS \\\\

	@Override
	public void writePacket(ByteBuf buf, int id) {
		super.writePacket(buf, id);
		switch (id) {
		case 1:
			mode.writeToBuf(buf);
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		super.readPacket(buf, id);
		switch (id) {
		case 1:
			mode.readFromBuf(buf);
			updatePower();
			break;
		}
	}

	public int getCurrentPower() {
		return currentPower.getObject();
	}

	@Override
	public void refreshConnectedNetworks() {}

	@Override
	public List<Integer> getConnectedNetworks() {
		List<Integer> networks = new ArrayList<>();
		List<IRedstoneEmitter> emitters = getEmitters();
		for (IRedstoneEmitter emitter : emitters) {
			IRedstoneNetwork rNetwork = emitter.getRedstoneNetwork();
			if (rNetwork.getNetworkID() != -1 && rNetwork.isValid()) {
				networks.add(rNetwork.getNetworkID());
			}
		}
		return networks;

	}

	@Override
	public IRedstoneNetwork getRedstoneNetwork() {
		return rNetwork;
	}

}