package sonar.logistics.common.multiparts.displays;

import java.util.List;

import mcmultipart.raytrace.RayTraceUtils.AdvancedRayTraceResultPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.listener.ListenableList;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.SyncEnum;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.info.render.IInfoContainer;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.operator.OperatorMode;
import sonar.logistics.api.tiles.displays.DisplayLayout;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.common.multiparts.AbstractDisplayPart;

public class DisplayScreenPart extends AbstractDisplayPart {

	public SyncEnum<DisplayLayout> layout = new SyncEnum(DisplayLayout.values(), 1);
	public ListenableList<PlayerListener> listeners = new ListenableList(this, ListenerType.ALL.size());
	public InfoContainer container = new InfoContainer(this);

	public DisplayScreenPart() {
		super();
	}

	public DisplayScreenPart(EnumFacing dir, EnumFacing rotation) {
		super(dir, rotation);
	}

	@Override
	public boolean performOperation(AdvancedRayTraceResultPart rayTrace, OperatorMode mode, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
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
		sendSyncPacket();
		sendUpdatePacket(true);
	}

	public ListenableList<PlayerListener> getListenerList() {
		return listeners;
	}

	//// MULTIPART \\\\

	public void addSelectionBoxes(List<AxisAlignedBB> list) {
		double p = 0.0625;
		double height = p * 16, width = 0, length = p * 1;
		switch (face) {
		case EAST:
			list.add(new AxisAlignedBB(1, p * 4, (width) / 2, 1 - length, 1 - p * 4, 1 - width / 2));
			break;
		case NORTH:
			list.add(new AxisAlignedBB((width) / 2, p * 4, length, 1 - width / 2, 1 - p * 4, 0));
			break;
		case SOUTH:
			list.add(new AxisAlignedBB((width) / 2, p * 4, 1, 1 - width / 2, 1 - p * 4, 1 - length));
			break;
		case WEST:
			list.add(new AxisAlignedBB(length, p * 4, (width) / 2, 0, 1 - p * 4, 1 - width / 2));
			break;
		case DOWN:
			list.add(new AxisAlignedBB(0, 0, 0, 1, 0.0625, 1));
			break;
		case UP:
			list.add(new AxisAlignedBB(0, 1 - 0, 0, 1, 1 - 0.0625, 1));
			break;
		default:
			break;
		}
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

	//// PACKETS \\\\

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

	@Override
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.DISPLAY_SCREEN;
	}

}