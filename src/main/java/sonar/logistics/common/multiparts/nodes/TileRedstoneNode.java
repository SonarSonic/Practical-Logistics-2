package sonar.logistics.common.multiparts.nodes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.PL2;
import sonar.logistics.api.cabling.CableConnectionType;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.cabling.IRedstoneConnectable;
import sonar.logistics.api.cabling.IRedstonePowerProvider;
import sonar.logistics.networking.ServerInfoHandler;
import sonar.logistics.networking.cabling.EmptyRedstoneNetwork;
import sonar.logistics.networking.cabling.IRedstoneNetwork;
import sonar.logistics.networking.cabling.RedstoneCableHelper;

public class TileRedstoneNode extends TileSonarMultipart implements IRedstoneConnectable, IRedstonePowerProvider {

	public IRedstoneNetwork rNetwork = EmptyRedstoneNetwork.INSTANCE;
	private SyncTagType.INT identity = (INT) new SyncTagType.INT("identity").setDefault(-1);
	public SyncTagType.INT networkID = (INT) new SyncTagType.INT(0).setDefault(-1);
	private SyncTagType.INT power = (INT) new SyncTagType.INT("power");
	public boolean isSelfChecking = false;

	{
		syncList.addParts(identity, power);
	}

	@Override
	public void onFirstTick() {
		super.onFirstTick();
		// if (this.isServer())
		// PL2.getRedstoneManager().queueConnectorAddition(this);
	}

	public void invalidate() {
		super.invalidate();
		// if (this.isServer())
		// PL2.getRedstoneManager().queueConnectorRemoval(this);
	}

	public EnumFacing getCableFace() {
		return EnumFacing.VALUES[getBlockMetadata()];
	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.INTERNAL;
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
		int power = RedstoneCableHelper.getPower(state, world, adjPos, face.getOpposite());
		isSelfChecking =false;
		return power;
	}

	@Override
	public CableConnectionType canConnect(int registryID, ConnectableType type, EnumFacing dir, boolean internal) {
		EnumFacing toCheck = internal ? dir : dir.getOpposite();
		return toCheck == getCableFace() ? CableConnectionType.NETWORK : CableConnectionType.NONE;
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
