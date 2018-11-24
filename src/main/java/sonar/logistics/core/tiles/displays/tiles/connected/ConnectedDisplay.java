package sonar.logistics.core.tiles.displays.tiles.connected;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.*;
import sonar.core.network.sync.SyncTagType.BOOLEAN;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.core.tiles.connections.EnumCableConnection;
import sonar.logistics.api.core.tiles.connections.EnumCableConnectionType;
import sonar.logistics.api.core.tiles.connections.EnumCableRenderSize;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.displays.tiles.IDisplay;
import sonar.logistics.api.core.tiles.displays.tiles.ILargeDisplay;
import sonar.logistics.base.guidance.errors.ErrorMessage;
import sonar.logistics.base.listeners.ILogicListenable;
import sonar.logistics.base.utils.slots.EnumDisplayFaceSlot;
import sonar.logistics.base.utils.worlddata.ConnectedDisplayData;
import sonar.logistics.core.tiles.connections.data.handling.CableConnectionHelper;
import sonar.logistics.core.tiles.displays.DisplayHandler;
import sonar.logistics.core.tiles.displays.DisplayHelper;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.storage.EditContainer;
import sonar.logistics.core.tiles.displays.tiles.DisplayVectorHelper;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

import java.util.List;

import static sonar.logistics.core.tiles.displays.tiles.DisplayVectorHelper.*;

/** used with Large Display Screens so they all have one uniform InfoContainer, Viewer list etc. */
public class ConnectedDisplay implements IDisplay, INBTSyncable, ISyncPart, ISyncableListener {

	public int dim;
	public int registryID;
	public ILargeDisplay topLeftScreen = null;
	public SyncableList syncParts = new SyncableList(this);
	public SyncEnum<EnumFacing> face = new SyncEnum(EnumFacing.VALUES, 0);
	public SyncTagType.DOUBLE width = new SyncTagType.DOUBLE(2), height = new SyncTagType.DOUBLE(3);
	public SyncTagType.BOOLEAN canBeRendered = (BOOLEAN) new SyncTagType.BOOLEAN(4).setDefault(true);
	public DisplayGSI gsi = null;
	public SyncCoords topLeftCoords = new SyncCoords(5);
	public SyncTagType.BOOLEAN isLocked = new SyncTagType.BOOLEAN(6);
	public World world;

	public Vec3d screenScale = new Vec3d(0,0,0);
	public Vec3d screenRotation = new Vec3d(0,0,0);
	public Vec3d screenOrigin = new Vec3d(0,0,0);

	// server side
	{
		syncParts.addParts(face, width, height, canBeRendered, topLeftCoords, isLocked);
	}

	ConnectedDisplay() {}

	public static ConnectedDisplay loadDisplay(World world, int registryID) {
		NBTTagCompound tag = world.isRemote ? null : ConnectedDisplayData.unloadedDisplays.get(registryID);
		return loadDisplay(world, registryID, tag);
	}

	public static ConnectedDisplay loadDisplay(World world, int registryID, NBTTagCompound tag){
        ConnectedDisplay display = new ConnectedDisplay();
        display.world = world;
        display.gsi = new DisplayGSI(display, world, registryID);
        display.registryID = registryID;
        if (tag != null) {
            display.readData(tag, SyncType.SAVE);
            ConnectedDisplayData.unloadedDisplays.remove(registryID);
        }
        if (world.isRemote) {
            EditContainer.addEditContainer(display.gsi);
        }
        return display;
    }


	public void setDisplayScaling() {
		List<ILargeDisplay> displays = DisplayHandler.instance().getConnections(getRegistryID());
		if (displays.isEmpty()) {
			canBeRendered.setObject(false);
			displays.forEach(d -> d.setShouldRender(false));
			return;
		}
		displays.forEach(display -> display.setConnectedDisplay(this));
		topLeftScreen = displays.get(0);
		Vec3d p = convertVector(topLeftScreen.getCoords().getBlockPos()).addVector(0.5, 0.5, 0.5);
		double minX = p.x, maxX = p.x, minY = p.y, maxY = p.y, minZ = p.z, maxZ = p.z;

		for (ILargeDisplay display : displays) {
			Vec3d pos = convertVector(display.getCoords().getBlockPos()).addVector(0.5, 0.5, 0.5);
			minX = Math.min(pos.x, minX);
			maxX = Math.max(pos.x, maxX);

			minY = Math.min(pos.y, minY);
			maxY = Math.max(pos.y, maxY);

			minZ = Math.min(pos.z, minZ);
			maxZ = Math.max(pos.z, maxZ);
		}

		double width = 0, height = 0;
		switch (getCableFace().getAxis()) {
			case X:
				width = maxZ - minZ;
				height = maxY - minY;
				break;
			case Y:
				width = maxX - minX;
				height = maxZ - minZ;
				break;
			case Z:
				width = maxX - minX;
				height = maxY - minY;
				break;
			default:
				break;
		}

		///TODO if roll = 90 or 270, switch the width and height
		this.width.setObject(width += 1 - 0.0625*2);
		this.height.setObject(height += 1 - 0.0625*2);
		this.screenOrigin = new Vec3d((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2).add(DisplayVectorHelper.getFaceOffset(getCableFace(), 0.5));
		this.screenRotation = DisplayVectorHelper.getScreenRotation(this.getCableFace());
		this.screenScale =  new Vec3d (width, height, 0.001 );
		if(Math.ceil(width) * Math.ceil(height) != displays.size()){
			canBeRendered.setObject(false);
			displays.forEach(d -> d.setShouldRender(false));
			return;
		}
		Vec3d[] vectors = getScreenVectors(this, getLookVector(getPitch(), getYaw()));
		Vec3i topLeft = convertVector(getTopRight(new Vec3d((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2), vectors[0], vectors[1], width, height));
		displays.forEach(display -> setTopLeftScreen(display, display.getCoords().getBlockPos().equals(topLeft)));
		canBeRendered.setObject(true);
		gsi.updateScaling();
	}

	@Override
	public Vec3d getScreenScaling() {
		return screenScale;
	}

	@Override
	public Vec3d getScreenOrigin(){
		return screenOrigin;
	}

	@Override
	public Vec3d getScreenRotation(){
		return screenRotation;
	}

	public List<ILogicListenable> getLocalProviders(List<ILogicListenable> monitors) {
		List<ILargeDisplay> displays = DisplayHandler.instance().getConnections(registryID);
		for (ILargeDisplay display : displays) {
			if (display instanceof TileAbstractDisplay) {
				monitors = DisplayHelper.getLocalProvidersFromDisplay(monitors, ((TileAbstractDisplay) display).getWorld(), ((TileAbstractDisplay) display).getPos(), display);
			}
		}
		return monitors;
	}

	public ILargeDisplay getActualDisplay() {
		return getTopLeftScreen();
	}

	public void setTopLeftScreen(ILargeDisplay display, boolean isTopLeft) {
		if (isTopLeft) {
			topLeftScreen = display;
			topLeftCoords.setCoords(display.getCoords());
			display.setShouldRender(true);
			face.setObject(display.getCableFace());
		} else {
			display.setShouldRender(false);
		}
	}

	public ILargeDisplay getTopLeftScreen() {
		if (topLeftCoords.getCoords() != null) {
			IDisplay display = CableConnectionHelper.getDisplay(world, topLeftCoords.getCoords().getBlockPos(), EnumDisplayFaceSlot.fromFace(face.getObject()));
			if (display instanceof ILargeDisplay) {
				topLeftScreen = (ILargeDisplay) display;
			}
		}
		return topLeftScreen;
	}

	@Override
	public DisplayGSI getGSI() {
		return gsi;
	}

	@Override
	public void setGSI(DisplayGSI gsi) {
		this.gsi = gsi;
	}

	@Override
	public EnumFacing getCableFace() {
		return face.getObject();
	}

	@Override
	public BlockCoords getCoords() {
		return getTopLeftScreen() != null ? topLeftScreen.getCoords() : topLeftCoords.getCoords();
	}

	public int getDimension() {
		return getCoords().getDimension();
	}

	@Override
	public int getInfoContainerID() {
		return getRegistryID();
	}

	public void onInfoContainerPacket() {
		DisplayHandler.instance().markConnectedDisplayChanged(getRegistryID(), ConnectedDisplayChange.WATCHERS_CHANGED);
	}

	public int getRegistryID() {
		return registryID;
	}

	public void readData(NBTTagCompound nbt, SyncType type) {
		if (nbt.hasKey(getTagName())) {
			NBTTagCompound tag = nbt.getCompoundTag(this.getTagName());
			NBTHelper.readSyncParts(tag, type, this.syncParts);
			screenScale = readVec3d("scale", nbt, type);
			screenRotation = readVec3d("rotate", nbt, type);
			screenOrigin = readVec3d("origin", nbt, type);
			getGSI().readData(tag, type);
		}
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTTagCompound tag = new NBTTagCompound();
		NBTHelper.writeSyncParts(tag, type, this.syncParts, true);
		writeVec3d(screenScale,"scale", nbt, type);
		writeVec3d(screenRotation,"rotate", nbt, type);
		writeVec3d(screenOrigin,"origin", nbt, type);
		getGSI().writeData(tag, type);
		if (!tag.hasNoTags()) {
			nbt.setTag(this.getTagName(), tag);
		}
		return nbt;
	}

	@Override
	public void writeToBuf(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, writeData(new NBTTagCompound(), SyncType.SAVE));
	}

	@Override
	public void readFromBuf(ByteBuf buf) {
		readData(ByteBufUtils.readTag(buf), SyncType.SAVE);
	}

	@Override
	public boolean canSync(SyncType sync) {
		return SyncType.isGivenType(sync, SyncType.DEFAULT_SYNC, SyncType.SAVE);
	}

	@Override
	public String getTagName() {
		return "connected";
	}

	@Override
	public ISyncableListener getListener() {
		return this;
	}

	@Override
	public IDirtyPart setListener(ISyncableListener listener) {
		if (listener instanceof ILargeDisplay) {
			if (listener != topLeftScreen) {
				setTopLeftScreen((ILargeDisplay) listener, true);
			}
		}
		return this;
	}

	@Override
	public void markChanged(IDirtyPart part) {
		syncParts.markSyncPartChanged(part);
		if(!this.getActualWorld().isRemote)
			DisplayHandler.instance().markConnectedDisplayChanged(getRegistryID(), ConnectedDisplayChange.WATCHERS_CHANGED);
	}

	@Override
	public int getNetworkID() {
		return getTopLeftScreen().getNetworkID();
	}

	@Override
	public ILogisticsNetwork getNetwork() {
		return getTopLeftScreen().getNetwork();
	}

	@Override
	public int getIdentity() {
		return registryID;
	}

	@Override
	public ErrorMessage[] getValidMessages() {
		return new ErrorMessage[0];
	}

	@Override
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.LARGE_DISPLAY_SCREEN;
	}

	@Override
	public void onNetworkConnect(ILogisticsNetwork network) {}

	@Override
	public void onNetworkDisconnect(ILogisticsNetwork network) {}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public EnumCableConnection canConnect(int registryID, EnumCableConnectionType type, EnumFacing dir, boolean internal) {
		return EnumCableConnection.NETWORK;
	}

	@Override
	public EnumCableRenderSize getCableRenderSize(EnumFacing dir) {
		return EnumCableRenderSize.CABLE;
	}

	@Override
	public World getActualWorld() {
		return gsi.getWorld();
	}

	@Override
	public World getPartWorld() {
		return getTopLeftScreen().getPartWorld();
	}

}