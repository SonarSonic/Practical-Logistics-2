package sonar.logistics.core.tiles.nodes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.connections.EnumCableConnection;
import sonar.logistics.api.core.tiles.connections.EnumCableConnectionType;
import sonar.logistics.api.core.tiles.connections.EnumCableRenderSize;
import sonar.logistics.api.core.tiles.connections.redstone.IRedstoneConnectable;
import sonar.logistics.api.core.tiles.connections.redstone.IRedstonePowerProvider;
import sonar.logistics.api.core.tiles.connections.redstone.network.IRedstoneNetwork;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.core.tiles.connections.redstone.handling.RedstoneConnectionHelper;
import sonar.logistics.core.tiles.connections.redstone.network.EmptyRedstoneNetwork;

public class TileRedstoneNode extends TileSonarMultipart implements IRedstoneConnectable, IRedstonePowerProvider {

	public IRedstoneNetwork rNetwork = EmptyRedstoneNetwork.INSTANCE;
	private SyncTagType.INT identity = (INT) new SyncTagType.INT("identity").setDefault(-1);
	public SyncTagType.INT networkID = (INT) new SyncTagType.INT(0).setDefault(-1);
	private SyncTagType.INT power = (INT) new SyncTagType.INT("power");
	public boolean isSelfChecking = false;

	{
		syncList.addParts(identity, power);
	}

	public EnumFacing getCableFace() {
		return EnumFacing.VALUES[getBlockMetadata()];
	}

	@Override
	public EnumCableRenderSize getCableRenderSize(EnumFacing dir) {
		return EnumCableRenderSize.INTERNAL;
	}

	@Override
	public int getIdentity() {
		if (identity.getObject() == -1 && this.isServer()) {
			identity.setObject(ServerInfoHandler.instance().getNextIdentity());
		}
		return identity.getObject();
	}

	@Override
	public int getCurrentPower() {
		EnumFacing face = getCableFace();
		BlockPos adjPos = pos.offset(face);
		IBlockState state = world.getBlockState(adjPos);
		isSelfChecking=true;
		int power = RedstoneConnectionHelper.getPower(state, world, adjPos, face.getOpposite());
		isSelfChecking =false;
		return power;
	}

	@Override
	public EnumCableConnection canConnect(int registryID, EnumCableConnectionType type, EnumFacing dir, boolean internal) {
		EnumFacing toCheck = internal ? dir : dir.getOpposite();
		return toCheck == getCableFace() ? EnumCableConnection.NETWORK : EnumCableConnection.NONE;
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

	@Override
	public int getNetworkID() {
		return networkID.getObject();
	}

	@Override
	public boolean isValid() {
		return !isInvalid();
	}

	@Override
	public IRedstoneNetwork getRedstoneNetwork() {
		return rNetwork;
	}

}
