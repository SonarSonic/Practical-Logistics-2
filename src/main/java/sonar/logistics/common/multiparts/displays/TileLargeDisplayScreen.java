package sonar.logistics.common.multiparts.displays;

import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.listener.ListenableList;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.BOOLEAN;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.PL2;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.operator.OperatorMode;
import sonar.logistics.api.tiles.cable.CableRenderType;
import sonar.logistics.api.tiles.cable.ConnectableType;
import sonar.logistics.api.tiles.cable.NetworkConnectionType;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayLayout;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.packets.PacketConnectedDisplayUpdate;

public class TileLargeDisplayScreen extends TileAbstractDisplay implements ILargeDisplay {

	//public int registryID = -1;
	public boolean wasAdded = false;
	public ConnectedDisplay overrideDisplay = null;
	public NBTTagCompound savedTag = null;
	public SyncTagType.BOOLEAN shouldRender = (BOOLEAN) new SyncTagType.BOOLEAN(3); // set default info
	public SyncTagType.INT registryID = (INT) new SyncTagType.INT(4).setDefault(-1); 
	public boolean onRenderChange = true;

	{
		syncList.addParts(shouldRender, registryID);
	}

	@Override
	public boolean performOperation(RayTraceResult rayTrace, OperatorMode mode, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (getDisplayScreen() != null && !getWorld().isRemote) {
			incrementLayout();
			FontHelper.sendMessage("Screen Layout: " + getDisplayScreen().layout.getObject(), getWorld(), player);
		}
		return false;
	}

	public void update() {
		super.update();
		if (isServer() && onRenderChange) {
			if (this.shouldRender()) {
				getDisplayScreen().updateListeners = true;
			}
			SonarMultipartHelper.sendMultipartUpdateSyncAround(this, 128);
			onRenderChange = false;
		}
	}

	public void updateDefaultInfo() {
		if (this.getDisplayScreen() != null && this.shouldRender()) {
			super.updateDefaultInfo();
		}
	}

	//// IInfoDisplay \\\\

	@Override
	public InfoContainer container() {
		return getDisplayScreen().container;
	}

	@Override
	public DisplayLayout getLayout() {
		ConnectedDisplay screen = getDisplayScreen();
		return screen == null ? DisplayLayout.ONE : screen.getLayout();
	}

	@Override
	public void incrementLayout() {
		getDisplayScreen().layout.incrementEnum();
		while (!(getDisplayScreen().layout.getObject().maxInfo <= this.maxInfo())) {
			getDisplayScreen().layout.incrementEnum();
		}
		SonarMultipartHelper.sendMultipartUpdateSyncAround(this, 128);
		getDisplayScreen().updateListeners = true;
	}

	@Override
	public DisplayType getDisplayType() {
		return DisplayType.LARGE;
	}

	@Override
	public int maxInfo() {
		return 4;
	}

	@Override
	public ConnectableType getConnectableType() {
		return ConnectableType.SCREEN;
	}

	//// ILargeDisplay \\\\

	@Override
	public int getRegistryID() {
		return registryID.getObject();
	}

	@Override
	public void setRegistryID(int id) {
		registryID.setObject(id);
	}

	@Override
	public ConnectedDisplay getDisplayScreen() {
		return overrideDisplay != null ? overrideDisplay : PL2.getInfoManager(world.isRemote).getOrCreateDisplayScreen(getWorld(), this, getRegistryID());
	}

	@Override
	public void setConnectedDisplay(ConnectedDisplay connectedDisplay) {
		if (isServer() && shouldRender()) {
			if (this.savedTag != null && !savedTag.hasNoTags()) {
				connectedDisplay.readData(savedTag, SyncType.SAVE);
				savedTag = null;
				connectedDisplay.updateAllListeners();
				PL2.getServerManager().updateViewingMonitors = true;
			}
		}
	}

	@Override
	public boolean shouldRender() {
		return shouldRender.getObject() && getRegistryID() != -1 && getDisplayScreen() != null;
	}

	@Override
	public void setShouldRender(boolean shouldRender) {
		if (shouldRender != this.shouldRender.getObject()) {
			this.shouldRender.setObject(shouldRender);
		}
		onRenderChange = true;
		if (isServer()) {
			PL2.getServerManager().updateViewingMonitors = true;
		}
		this.markDirty();
	}

	//// ILogicViewable \\\\

	//// NETWORK \\\\
	public void onNetworkConnect(ILogisticsNetwork network) {
		super.onNetworkConnect(network);
		getDisplayScreen().setHasChanged();
	}

	public void onNetworkDisconnect(ILogisticsNetwork network) {
		super.onNetworkDisconnect(network);
		getDisplayScreen().setHasChanged();
	}

	@Override
	public NetworkConnectionType canConnect(int registryID, ConnectableType type, EnumFacing dir, boolean internal) {
		boolean cableFace = (dir == getCableFace() || dir == getCableFace().getOpposite());
		boolean cableConnection = (internal && cableFace && type == ConnectableType.CONNECTABLE);

		if (cableConnection || (!cableFace && type == this.getConnectableType() && (getRegistryID() == registryID || !(getDisplayScreen().isLocked.getObject())))) {
			return NetworkConnectionType.NETWORK;
		}
		return NetworkConnectionType.NONE;
	}

	@Override
	public void addInfo(List<String> info) {
		super.addInfo(info);
		info.add("Large Display ID: " + registryID);
		info.add("Should Render " + this.shouldRender.getObject());
	}

	public void addConnection() {
		if (isServer()) {
			PL2.getDisplayManager().queueDisplayAddition(this);
		}
	}

	public void removeConnection() {
		if (isServer()) {
			PL2.getDisplayManager().queueDisplayRemoval(this);
		}
	}

	//// EVENTS \\\\
	@Override
	public void onFirstTick() {
		super.onFirstTick();
		if (isServer() && !wasAdded) {
			addConnection();
			wasAdded = true;
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		wasAdded = false;
		removeConnection();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	//// SAVE \\\\
	/*
	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		if (nbt.hasKey("3")) {
			registryID = nbt.getInteger("id");
			shouldRender.readData(nbt, type);
			wasLocked.readData(nbt, type);
		}
		if (this.isServer() && type.isType(SyncType.SAVE) && nbt.hasKey("connected")) {
			this.savedTag = nbt;
		}
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		if (type.isType(SyncType.DEFAULT_SYNC) || (type.isType(SyncType.SAVE) && getDisplayScreen() != null && getDisplayScreen().isLocked.getObject())) {
			nbt.setInteger("id", registryID);
			shouldRender.writeData(nbt, type);
			wasLocked.setObject(this.getDisplayScreen().isLocked.getObject());
			wasLocked.writeData(nbt, type);
		}
		if (this.isServer() && type.isType(SyncType.SAVE) && this.shouldRender()) {
			if (this.getDisplayScreen() != null) {
				this.getDisplayScreen().writeData(nbt, type);
			}
		}
		return super.writeData(nbt, type);
	}
	*/

	//// PACKETS \\\\

	@Override
	public void writePacket(ByteBuf buf, int id) {
		super.writePacket(buf, id);
		switch (id) {
		case 5:
			shouldRender.writeToBuf(buf);
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		super.readPacket(buf, id);
		switch (id) {
		case 5:
			this.shouldRender.readFromBuf(buf);
			break;
		case 6:
			if (getDisplayScreen().isLocked.getObject()) {
				getDisplayScreen().unlock();
			} else {
				getDisplayScreen().lock();
			}
			break;
		}
	}

	@Override
	public void onGuiOpened(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			TileAbstractDisplay part = (TileAbstractDisplay) getDisplayScreen().getTopLeftScreen();
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			PL2.network.sendTo(new PacketConnectedDisplayUpdate(getDisplayScreen(), getRegistryID()), (EntityPlayerMP) player);
			PacketHelper.sendLocalProvidersFromScreen(part, world, pos, player);
			break;
		}
	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.CABLE;
	}

	@Override
	public boolean isBlocked(EnumFacing dir) {
		return false;
	}

	@Override
	public void updateCableRenders() {

	}
}
