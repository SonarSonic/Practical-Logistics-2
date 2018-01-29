package sonar.logistics.common.multiparts.cables;

import java.util.List;

import com.google.common.collect.Lists;

import mcmultipart.api.slot.EnumFaceSlot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RayTraceHelper;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.core.utils.LabelledAxisAlignedBB;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.PL2Properties;
import sonar.logistics.api.cabling.CableConnectionType;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.cabling.ICableConnectable;
import sonar.logistics.api.cabling.IRedstoneCable;
import sonar.logistics.api.cabling.IRedstonePowerProvider;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.operator.IOperatorProvider;
import sonar.logistics.api.operator.IOperatorTile;
import sonar.logistics.api.operator.OperatorMode;
import sonar.logistics.networking.cabling.CableHelper;
import sonar.logistics.networking.cabling.RedstoneCableHelper;
import sonar.logistics.networking.cabling.RedstoneConnectionHandler;

public class TileRedstoneCable extends TileSonarMultipart implements IRedstoneCable, IOperatorTile, IOperatorProvider {

	public int[] isBlocked = new int[6];
	public int[] isConnected = new int[6];

	public int registryID = -1;

	@Override
	public void onFirstTick() {
		super.onFirstTick();
		if (isServer()) {
			RedstoneConnectionHandler.instance().queueCableAddition(this);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (isServer()) {
			RedstoneConnectionHandler.instance().queueCableRemoval(this);
		}
	}

	@Override
	public void updateCableRenders() {
		if (isServer()) {
			for (EnumFacing face : EnumFacing.values()) {
				isConnected[face.ordinal()] = CableHelper.getConnectionRenderType(this, face).ordinal();
			}
			SonarMultipartHelper.sendMultipartUpdateSyncAround(this, 128);
		}
	}

	public CableRenderType getRenderType(EnumFacing face) {
		return CableRenderType.values()[isConnected[face.ordinal()]];
	}

	@Override
	public ConnectableType canRenderConnection(EnumFacing dir) {
		return CableHelper.getConnectableType(this, dir);
	}

	public boolean isBlocked(EnumFacing dir) {
		return isBlocked[dir.ordinal()] == 1;
	}

	public void invertBlock(EnumFacing dir) {
		isBlocked[dir.ordinal()] = isBlocked[dir.ordinal()] == 1 ? 0 : 1;
	}

	public boolean isInternallyBlocked(EnumFacing dir) {
		return info == null || dir == null ? false : info.getContainer().getPart(EnumFaceSlot.fromFace(dir)).isPresent();
	}

	@Override
	public CableConnectionType canConnect(int registryID, ConnectableType type, EnumFacing dir, boolean internal) {
		if (type != this.getConnectableType()) {
			return CableConnectionType.NONE;
		}
		boolean internallyBlocked = isInternallyBlocked(dir);
		if (isBlocked(dir) || (!internal && internallyBlocked)) {
			return CableConnectionType.NONE;
		}
		return CableConnectionType.NETWORK;
	}

	// @Override
	// public CableRenderType getCableRenderSize(EnumFacing dir) {
	// return CableRenderType.CABLE;
	// }

	@Override
	public int getRegistryID() {
		return registryID;
	}

	@Override
	public void setRegistryID(int id) {
		registryID = id;
	}

	@Override
	public ConnectableType getConnectableType() {
		return ConnectableType.REDSTONE;
	}

	//// OPERATOR \\\\

	@Override
	public void updateOperatorInfo() {
		requestSyncPacket();
	}

	@Override
	public void addInfo(List<String> info) {
		info.add(TextFormatting.UNDERLINE + PL2Multiparts.DATA_CABLE.getDisplayName());
		info.add("Network ID: " + registryID);
	}

	@Override
	public boolean performOperation(RayTraceResult rayTrace, OperatorMode mode, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (mode == OperatorMode.DEFAULT) {
			Pair<Vec3d, Vec3d> look = RayTraceHelper.getPlayerLookVec(player, world);
			Pair<RayTraceResult, AxisAlignedBB> trace = RayTraceHelper.rayTraceBoxes(pos, look.getLeft(), look.getRight(), BlockDataCable.getSelectionBoxes(world, pos, Lists.newArrayList()));

			if (trace.b instanceof LabelledAxisAlignedBB) {
				if (isClient()) {
					return true;
				}
				String label = ((LabelledAxisAlignedBB) trace.b).label;
				EnumFacing face = null;
				face = !label.equals("c") ? EnumFacing.valueOf(label.toUpperCase()) : facing;
				TileRedstoneCable adjCable = RedstoneCableHelper.getCable(world, pos.offset(face));
				if (adjCable != null) {
					// remove both cables
					RedstoneConnectionHandler.instance().removeConnection(this);
					RedstoneConnectionHandler.instance().removeConnection(adjCable);

					// change blocked settings
					invertBlock(face);
					adjCable.isBlocked[face.getOpposite().ordinal()] = isBlocked[face.ordinal()];

					// add both cables back again
					RedstoneConnectionHandler.instance().addConnection(adjCable);
					RedstoneConnectionHandler.instance().addConnection(this);

					// update networks
					ILogisticsNetwork thisNet = PL2.getNetworkManager().getOrCreateNetwork(getRegistryID());
					ILogisticsNetwork adjNetNet = PL2.getNetworkManager().getOrCreateNetwork(adjCable.getRegistryID());
					thisNet.onCablesChanged();
					adjNetNet.onCablesChanged();

					// update cable render
					SonarMultipartHelper.sendMultipartUpdateSyncAround(this, 128);
					SonarMultipartHelper.sendMultipartUpdateSyncAround(adjCable, 128);
				}
				return true;

			}
		}
		return false;

	}

	//// SAVING \\\\

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		isBlocked = nbt.getIntArray("isBlocked");
		isConnected = nbt.getIntArray("isConnected");
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTTagCompound tag = new NBTTagCompound();
		nbt.setIntArray("isBlocked", isBlocked);
		nbt.setIntArray("isConnected", isConnected);
		return super.writeData(nbt, type);
	}

	@Override
	public int getMaxPower() {
		int maxPower = 0;
		for (EnumFacing face : EnumFacing.VALUES) {
			ICableConnectable connection = CableHelper.getConnection(this, face, CableConnectionType.NETWORK, false);
			if (connection != null && connection instanceof IRedstonePowerProvider) {
				IRedstonePowerProvider provider = (IRedstonePowerProvider) connection;
				if (provider.getCurrentPower() > 0) {
					return 15;
				}
			}
		}
		return 0;
	}

	@Override
	public void setNetworkPower(int power) {
		IBlockState oldState = this.getBlockType().getDefaultState();
		IBlockState newState = this.getBlockType().getDefaultState().withProperty(PL2Properties.ACTIVE, power > 0);
		world.setBlockState(pos, newState, 2);
		world.notifyNeighborsOfStateChange(pos, blockType, true);
	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.CABLE;
	}
}