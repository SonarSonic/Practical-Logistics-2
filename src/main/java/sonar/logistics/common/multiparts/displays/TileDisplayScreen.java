package sonar.logistics.common.multiparts.displays;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.sync.SyncEnum;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.IInfoContainer;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.operator.OperatorMode;
import sonar.logistics.api.tiles.displays.DisplayLayout;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.networking.displays.ChunkViewerHandler;

public class TileDisplayScreen extends TileAbstractDisplay {

	private DisplayGSI container;

	//// IInfoDisplay \\\\

	@Override
	public DisplayGSI getGSI() {
		if (container == null) {
			container = new DisplayGSI(this, getInfoContainerID());
		}
		return container;
	}

	@Override
	public int getInfoContainerID() {
		return getIdentity();
	}

	@Override
	public DisplayType getDisplayType() {
		return DisplayType.SMALL;
	}

	//// SAVING \\\\

	@Override
	public NBTTagCompound writeData(NBTTagCompound tag, SyncType type) {
		super.writeData(tag, type);
		getGSI().writeData(tag, type.isType(SyncType.SPECIAL) ? SyncType.SAVE : type);
		return tag;
	}

	@Override
	public void readData(NBTTagCompound tag, SyncType type) {
		super.readData(tag, type);
		getGSI().readData(tag, type.isType(SyncType.SPECIAL) ? SyncType.SAVE : type);
	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.INTERNAL;
	}
}