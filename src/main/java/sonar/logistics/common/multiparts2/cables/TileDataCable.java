package sonar.logistics.common.multiparts2.cables;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import mcmultipart.api.slot.EnumFaceSlot;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.core.utils.LabelledAxisAlignedBB;
import sonar.logistics.PL2;
import sonar.logistics.PL2Items;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.PL2API;
import sonar.logistics.api.PL2Properties;
import sonar.logistics.api.PL2Properties.PropertyCableFace;
import sonar.logistics.api.capability.PL2Capabilities;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.operator.IOperatorProvider;
import sonar.logistics.api.operator.IOperatorTile;
import sonar.logistics.api.operator.OperatorMode;
import sonar.logistics.api.tiles.INetworkConnection;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.tiles.cable.CableRenderType;
import sonar.logistics.api.tiles.cable.ConnectableType;
import sonar.logistics.api.tiles.cable.IDataCable;
import sonar.logistics.api.tiles.cable.NetworkConnectionType;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.common.multiparts.displays.DataCablePart;
import sonar.logistics.helpers.CableHelper;

public class TileDataCable extends TileSonarMultipart implements IDataCable, IOperatorTile, IOperatorProvider {

	public boolean[] isBlocked = new boolean[6];
	public int registryID = -1;

	public ILogisticsNetwork getNetwork() {
		return PL2API.getCableHelper().getNetwork(registryID);
	}

	@Override
	public void onFirstTick() {
		super.onFirstTick();
		if (isServer()) {
			CableConnectionHandler.addCableToNetwork(this);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (isServer()) {
			CableConnectionHandler.removeCableFromNetwork(this);
		}
	}

	@Override
	public ConnectableType canRenderConnection(EnumFacing dir) {
		return CableHelper.getConnectableType(this, dir);
	}

	public boolean isBlocked(EnumFacing dir) {
		return isBlocked[dir.ordinal()];
	}

	public boolean isInternallyBlocked(EnumFacing dir) {
		return info == null || dir == null ? false : info.getContainer().getPart(EnumFaceSlot.fromFace(dir)).isPresent();
	}

	@Override
	public NetworkConnectionType canConnect(int networkID, EnumFacing dir, boolean internal) {
		boolean internallyBlocked = isInternallyBlocked(dir);
		if (isBlocked(dir) || (!internal && internallyBlocked)) {
			return NetworkConnectionType.NONE;
		}
		return NetworkConnectionType.NETWORK;
	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.CABLE;
	}

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
		return ConnectableType.CONNECTABLE;
	}

	//// OPERATOR \\\\

	@Override
	public void updateOperatorInfo() {
		requestSyncPacket();
	}

	@Override
	public void addInfo(List<String> info) {
		// info.add(TextFormatting.UNDERLINE +
		// PL2Multiparts.DATA_CABLE.getDisplayName()); //FIXME
		// info.add("Network ID: " + registryID);
	}

	@Override
	public boolean performOperation(RayTraceResult rayTrace, OperatorMode mode, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		/* if (mode == OperatorMode.DEFAULT) { List<AxisAlignedBB> bounds = Lists.newArrayList(); addSelectionBoxes(bounds); for (AxisAlignedBB bound : bounds) { if (bound instanceof LabelledAxisAlignedBB && bound.equals(rayTrace.bounds)) { if (isClient()) { return true; } String label = ((LabelledAxisAlignedBB) bound).label; EnumFacing face = null; face = !label.equals("c") ? EnumFacing.valueOf(label.toUpperCase()) : facing; isBlocked[face.ordinal()] = !isBlocked[face.ordinal()]; IDataCable cable = PL2API.getCableHelper().getCableFromCoords(BlockCoords. translateCoords(getCoords(), face)); removeConnection(); addConnection(); if (cable != null && cable instanceof DataCablePart) { DataCablePart part = (DataCablePart) cable; part.isBlocked[face.getOpposite().ordinal()] = isBlocked[face.ordinal()]; part.sendUpdatePacket(true); part.markDirty(); // it's messy, but there is no easier way to check if the cables are connected properly. part.removeConnection(); part.addConnection(); } sendUpdatePacket(true); markDirty(); return true; } } } */
		return false;
	}

	//// MULTIPART \\\\

	//// STATE \\\\

	//// SAVING \\\\

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		isBlocked = new boolean[6];
		NBTTagCompound tag = nbt.getCompoundTag("isBlocked");
		for (int i = 0; i < isBlocked.length; i++) {
			isBlocked[i] = tag.getBoolean("" + i);
		}
		/* if (type.isType(SyncType.DEFAULT_SYNC)) { registryID = nbt.getInteger("id"); } */
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTTagCompound tag = new NBTTagCompound();
		for (int i = 0; i < isBlocked.length; i++) {
			tag.setBoolean("" + i, isBlocked[i]);
		}
		nbt.setTag("isBlocked", tag);
		/* if (type.isType(SyncType.DEFAULT_SYNC)) { nbt.setInteger("id", registryID); } */
		return super.writeData(nbt, type);
	}

	//// PACKETS \\\\
	/* @Override public void writeUpdatePacket(PacketBuffer buf) { super.writeUpdatePacket(buf); for (int i = 0; i < isBlocked.length; i++) { buf.writeBoolean(isBlocked[i]); } }
	 * @Override public void readUpdatePacket(PacketBuffer buf) { super.readUpdatePacket(buf); for (int i = 0; i < isBlocked.length; i++) { isBlocked[i] = buf.readBoolean(); } } */

}