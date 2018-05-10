package sonar.logistics.core.tiles.misc.clock;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.connections.EnumCableRenderSize;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.readers.IInfoProvider;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.guidance.errors.ErrorMessage;
import sonar.logistics.base.listeners.ListenerType;
import sonar.logistics.base.listeners.PL2ListenerList;
import sonar.logistics.core.tiles.base.TileSidedLogistics;
import sonar.logistics.core.tiles.displays.info.types.ClockInfo;

import java.text.SimpleDateFormat;

public class TileClock extends TileSidedLogistics implements IInfoProvider, IByteBufTile, IFlexibleGui {

	public static final ErrorMessage[] validStates = new ErrorMessage[] {};

	public static final long[] tickAdjustments = new long[] { 100, -100, 1000, -1000, 60000, -60000, 60000 * 60, -(60000 * 60) };
	public final PL2ListenerList listeners = new PL2ListenerList(this, ListenerType.ALL.size());
	public SyncTagType.LONG tickTime = new SyncTagType.LONG(1);

	public long lastMillis;// when the movement was started
	public long currentMillis;// the current millis

	public float rotation;// 0-360 indicating rotation of the clock hand.
	public boolean isSet;

	public boolean lastSignal;
	public boolean wasStarted;
	public boolean powering;

	public long finalStopTime;
	{
		this.syncList.addParts(tickTime);
	}

	public void update() {
		super.update();
		if (isClient()) {
			return;
		}
		if (!(tickTime.getObject() < 10)) {
			currentMillis = (getWorld().getTotalWorldTime() * 50);
			long start = currentMillis - lastMillis;
			rotation = (start) * 360 / (tickTime.getObject());
			if (start > tickTime.getObject()) {
				this.lastMillis = currentMillis;
				powering = true;
				world.notifyBlockUpdate(getPos(), world.getBlockState(pos), world.getBlockState(pos), 1);
			} else {
				if (powering) {
					powering = false;
					world.notifyBlockUpdate(getPos(), world.getBlockState(pos), world.getBlockState(pos), 1);
				}
			}
			markDirty();
			sendByteBufPacket(0);
			setClockInfo();
		}
	}

	public void setClockInfo() {
		IInfo info = null;
		if (!(tickTime.getObject() < 10)) {
			long start = currentMillis - lastMillis;
			String timeString = new SimpleDateFormat("HH:mm:ss:SSS").format((start) - (60 * 60 * 1000)).substring(0, 11);
			info = new ClockInfo(start, tickTime.getObject(), timeString);
		}

		if (info != null) {
			InfoUUID id = new InfoUUID(getIdentity(), 0);
			IInfo oldInfo = ServerInfoHandler.instance().getInfoMap().get(id);
			if (oldInfo == null || !oldInfo.isMatchingType(info) || !oldInfo.isMatchingInfo(info) || !oldInfo.isIdenticalInfo(info)) {
				ServerInfoHandler.instance().changeInfo(this, id, info);
			}
		}
	}

	//// IInfoProvider \\\\

	@Override
	public IInfo getMonitorInfo(int pos) {
		return PL2.proxy.getInfoManager(isClient()).getInfoMap().get(new InfoUUID(getIdentity(), 0));
	}

	@Override
	public int getMaxInfo() {
		return 1;
	}

	//// ILogicViewable \\\\

	public PL2ListenerList getListenerList() {
		return listeners;
	}

	@Override
	public void onListenerAdded(ListenerTally<PlayerListener> tally) {
		SonarMultipartHelper.sendMultipartSyncToPlayer(this, tally.listener.player);
	}

	//// EVENTS \\\\

	public void onFirstTick() {
		super.onFirstTick();
		if (isServer()) {
			setClockInfo();
		}
	}

	//// SAVING \\\\

	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		tickTime.readData(nbt, type);
		if (type == SyncType.SAVE) {
			nbt.setBoolean("isSet", isSet);
			nbt.setBoolean("lastSignal", lastSignal);
			nbt.setBoolean("wasStarted", wasStarted);
			nbt.setLong("finalStopTime", finalStopTime);
		}

	}

	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		tickTime.writeData(nbt, type);
		if (type == SyncType.SAVE) {
			this.isSet = nbt.getBoolean("isSet");
			this.lastSignal = nbt.getBoolean("lastSignal");
			this.wasStarted = nbt.getBoolean("wasStarted");
			this.finalStopTime = nbt.getLong("finalStopTime");
		}
		return nbt;
	}

	//// PACKETS \\\\

	@Override
	public void writePacket(ByteBuf buf, int id) {
		switch (id) {
		case 0:
			buf.writeFloat(rotation);
			break;
		case 1:
			tickTime.writeToBuf(buf);
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		switch (id) {
		case -4:
			sendByteBufPacket(-2);
			break;
		case 0:
			rotation = buf.readFloat();
			break;
		case 1:
			tickTime.readFromBuf(buf);
			break;
		}
		if (id >= 2 && id <= 9) {
			tickTime.increaseBy(tickAdjustments[id - 2]);
		}
		if (tickTime.getObject() < 0) {
			tickTime.setObject((long) 0);
		} else if (tickTime.getObject() > (1000 * 60 * 60 * 24)) {
			tickTime.setObject((long) ((1000 * 60 * 60 * 24) - 1));
		}
	}

	//// GUI \\\\

	public boolean hasStandardGui() {
		return true;
	}

	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new ContainerMultipartSync(this) : null;
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new GuiClock(this) : null;
	}

	@Override
	public void onGuiOpened(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			break;
		}
	}

	@Override
	public ErrorMessage[] getValidMessages() {
		return validStates;
	}

	@Override
	public EnumCableRenderSize getCableRenderSize(EnumFacing dir) {
		return EnumCableRenderSize.HALF;
	}

}