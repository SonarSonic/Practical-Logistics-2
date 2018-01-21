package sonar.logistics.common.multiparts.wireless;

import java.util.List;
import java.util.function.Function;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.PL2;
import sonar.logistics.api.tiles.signaller.EmitterStatement;
import sonar.logistics.api.tiles.signaller.SignallerModes;
import sonar.logistics.api.wireless.EnumConnected;
import sonar.logistics.api.wireless.IRedstoneEmitter;
import sonar.logistics.api.wireless.IRedstoneReceiver;
import sonar.logistics.api.wireless.IWirelessEmitter;
import sonar.logistics.api.wireless.IWirelessManager;
import sonar.logistics.client.gui.GuiDataReceiver;
import sonar.logistics.client.gui.GuiRedstoneReceiver;

public class TileRedstoneReceiver extends TileAbstractReceiver<IRedstoneEmitter, IRedstoneReceiver> implements IRedstoneReceiver {

	public static final Function<IRedstoneEmitter, Boolean> EMITTER_FUNC = e -> e.getRedstonePower() > 0;
	public SyncTagType.INT currentPower = new SyncTagType.INT(0);
	public SyncEnum<SignallerModes> mode = new SyncEnum(SignallerModes.values(), 1);

	@Override
	public IWirelessManager getWirelessHandler() {
		return PL2.getWirelessRedstoneManager();
	}

	public void onFirstTick() {
		super.onFirstTick();
		if (isServer()) {
			PL2.getWirelessRedstoneManager().connectReceiver(network, this);
			updatePower();
		}
	}

	public void invalidate() {
		super.invalidate();
		if (isServer()) {
			PL2.getWirelessRedstoneManager().disconnectReceiver(network, this);
			updatePower();
		}
	}

	public void updatePower() {
		if (isServer()) {
			boolean power = mode.getObject().checkList(getEmitters(), EMITTER_FUNC);
			int toSet = (power ? 15 : 0);// should we make it sensitive to total redstone power also?
			if (currentPower.getObject() != toSet) {
				currentPower.setObject(toSet);
				IBlockState state = world.getBlockState(pos);
				world.notifyNeighborsOfStateChange(pos, blockType, true);
				SonarMultipartHelper.sendMultipartUpdateSyncAround(this, 64);
			}
		}
	}

	public void markChanged(IDirtyPart part) {
		super.markChanged(part);
	}

	@Override
	public void onEmitterPowerChanged(IRedstoneEmitter emitter) {
		updatePower();
	}

	@Override
	public void onEmitterDisconnected(IRedstoneEmitter emitter) {
		updatePower();
	}

	@Override
	public void onEmitterConnected(IRedstoneEmitter emitter) {
		updatePower();
	}

	@Override
	public int getRedstonePower() {
		return currentPower.getObject();
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

}