package sonar.logistics.common.multiparts.wireless;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.PL2;
import sonar.logistics.api.wireless.IRedstoneEmitter;
import sonar.logistics.api.wireless.IWirelessManager;
import sonar.logistics.client.gui.GuiWirelessEmitter;

public class TileRedstoneEmitter extends TileAbstractEmitter implements IRedstoneEmitter {

	public SyncTagType.INT currentPower = new SyncTagType.INT(0);

	@Override
	public IWirelessManager getWirelessHandler() {
		return PL2.getWirelessRedstoneManager();
	}

	@Override
	public String getEmitterName() {
		if (currentPower.getObject() > 0)
			return TextFormatting.GREEN + super.getEmitterName();
		return super.getEmitterName();
	}

	public void onFirstTick() {
		super.onFirstTick();
		if (isServer()) {
			PL2.getWirelessRedstoneManager().connectEmitter(network, this);
		}
	}

	public void invalidate() {
		super.invalidate();
		if (isServer()) {
			PL2.getWirelessRedstoneManager().disconnectEmitter(network, this);
		}
	}

	public void onNeighbouringBlockChanged() {
		if (isServer()) {
			int power = world.getRedstonePower(pos.offset(getCableFace()), getCableFace().getOpposite());
			if (currentPower.getObject() != power) {
				currentPower.setObject(power);
				IBlockState state = world.getBlockState(pos);
				world.notifyBlockUpdate(pos, state, state, 1);
				SonarMultipartHelper.sendMultipartPacketAround(this, 0, 128);
				PL2.getWirelessRedstoneManager().onEmitterPowerChanged(this);
			}
		}
	}

	@Override
	public int getRedstonePower() {
		return currentPower.getObject();
	}

}
