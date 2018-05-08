package sonar.logistics.core.tiles.wireless.emitters;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.PL2;
import sonar.logistics.PL2Properties;
import sonar.logistics.api.core.tiles.connections.EnumCableConnection;
import sonar.logistics.api.core.tiles.connections.EnumCableConnectionType;
import sonar.logistics.api.core.tiles.connections.redstone.network.IRedstoneNetwork;
import sonar.logistics.api.core.tiles.wireless.IWirelessManager;
import sonar.logistics.api.core.tiles.wireless.emitters.IRedstoneEmitter;
import sonar.logistics.core.tiles.connections.redstone.handling.RedstoneConnectionHelper;
import sonar.logistics.core.tiles.connections.redstone.network.EmptyRedstoneNetwork;
import sonar.logistics.core.tiles.wireless.handling.WirelessRedstoneManager;

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
	public EnumCableConnection canConnect(int registryID, EnumCableConnectionType type, EnumFacing dir, boolean internal) {
		if (!type.isRedstone()) {
			return EnumCableConnection.NONE;
		}
		EnumFacing toCheck = internal ? dir : dir.getOpposite();
		return toCheck == getCableFace() ? EnumCableConnection.NETWORK : EnumCableConnection.NONE;
	}

	@Override
	public void onCableChanged(int power) {}

	@Override
	public int getRedstonePower() {
		currentPower.setObject(RedstoneConnectionHelper.getCableState(world, pos).getValue(PL2Properties.ACTIVE) ? 15 : 0);
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
			PL2.logger.info("%s : attempted to disconnect from the wrong handling with ID: %s expected %s", this, network.getNetworkID(), networkID.getObject());
		}		
	}

}
