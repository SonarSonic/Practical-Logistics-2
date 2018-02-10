package sonar.logistics.common.multiparts.wireless;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.PL2;
import sonar.logistics.api.PL2Properties;
import sonar.logistics.api.cabling.CableConnectionType;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.cabling.IRedstoneConnectable;
import sonar.logistics.api.wireless.IRedstoneEmitter;
import sonar.logistics.api.wireless.IWirelessManager;
import sonar.logistics.networking.cabling.EmptyRedstoneNetwork;
import sonar.logistics.networking.cabling.IRedstoneNetwork;
import sonar.logistics.networking.cabling.RedstoneCableHelper;
import sonar.logistics.networking.cabling.WirelessRedstoneManager;

public class TileRedstoneEmitter extends TileAbstractEmitter implements IRedstoneEmitter {

	public IRedstoneNetwork rNetwork = EmptyRedstoneNetwork.INSTANCE;
	public SyncTagType.INT currentPower = new SyncTagType.INT(0);

	@Override
	public IWirelessManager getWirelessHandler() {
		return WirelessRedstoneManager.instance();
	}

	@Override
	public String getEmitterName() {
		if (currentPower.getObject() > 0)
			return TextFormatting.GREEN + super.getEmitterName();
		return super.getEmitterName();
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
	public void onCableChanged(int power) {}

	@Override
	public int getRedstonePower() {
		currentPower.setObject(RedstoneCableHelper.getCableState(world, pos).getValue(PL2Properties.ACTIVE) ? 15 : 0);
		return currentPower.getObject();
	}

	@Override
	public IRedstoneNetwork getRedstoneNetwork() {
		return rNetwork;
	}

	@Override
	public void onNetworkConnect(IRedstoneNetwork network) {
		if (!this.rNetwork.isValid() || networkID.getObject() != network.getNetworkID()) {
			this.rNetwork = network;
			this.networkID.setObject(network.getNetworkID());
			//states.markTileMessage(TileMessage.NO_NETWORK, false);
		}
	}

	@Override
	public void onNetworkDisconnect(IRedstoneNetwork network) {
		if (networkID.getObject() == network.getNetworkID()) {
			this.rNetwork = EmptyRedstoneNetwork.INSTANCE;
			this.networkID.setObject(-1);
			//states.markTileMessage(TileMessage.NO_NETWORK, true);
		} else if (networkID.getObject() != -1) {
			PL2.logger.info("%s : attempted to disconnect from the wrong network with ID: %s expected %s", this, network.getNetworkID(), networkID.getObject());
		}		
	}

}
