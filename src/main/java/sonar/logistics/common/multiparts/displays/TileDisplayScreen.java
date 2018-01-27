package sonar.logistics.common.multiparts.displays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.sync.SyncEnum;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.info.render.IInfoContainer;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.operator.OperatorMode;
import sonar.logistics.api.tiles.displays.DisplayLayout;
import sonar.logistics.api.tiles.displays.DisplayType;

public class TileDisplayScreen extends TileAbstractDisplay {

	public SyncEnum<DisplayLayout> layout = new SyncEnum(DisplayLayout.values(), 1);
	public InfoContainer container = new InfoContainer(this);

	@Override
	public boolean performOperation(RayTraceResult rayTrace, OperatorMode mode, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!getWorld().isRemote) {
			incrementLayout();
			FontHelper.sendMessage("Screen Layout: " + layout.getObject(), getWorld(), player);
		}
		return true;
	}
	
	//// IInfoDisplay \\\\

	@Override
	public IInfoContainer container() {
		return container;
	}

	@Override
	public DisplayLayout getLayout() {
		return layout.getObject();
	}

	@Override
	public DisplayType getDisplayType() {
		return DisplayType.SMALL;
	}

	@Override
	public int maxInfo() {
		return 2;
	}

	@Override
	public void incrementLayout() {
		layout.incrementEnum();
		while (!(layout.getObject().maxInfo <= this.maxInfo())) {
			layout.incrementEnum();
		}
		SonarMultipartHelper.sendMultipartUpdateSyncAround(this, 128);
	}

	//// SAVING \\\\

	@Override
	public NBTTagCompound writeData(NBTTagCompound tag, SyncType type) {
		super.writeData(tag, type);
		container().writeData(tag, type);
		layout.writeData(tag, type);
		return tag;
	}

	@Override
	public void readData(NBTTagCompound tag, SyncType type) {
		super.readData(tag, type);
		container().readData(tag, type);
		layout.readData(tag, type);
	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.INTERNAL;
	}
	//// PACKETS \\\\
	/*
	@Override
	public void writeUpdatePacket(PacketBuffer buf) {
		super.writeUpdatePacket(buf);
		ByteBufUtils.writeTag(buf, container().writeData(new NBTTagCompound(), SyncType.SAVE));
		layout.writeToBuf(buf);
	}

	@Override
	public void readUpdatePacket(PacketBuffer buf) {
		super.readUpdatePacket(buf);
		container().readData(ByteBufUtils.readTag(buf), SyncType.SAVE);
		layout.readFromBuf(buf);
	}
	*/
}