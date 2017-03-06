package sonar.logistics.common.multiparts;

import java.util.List;

import com.google.common.collect.Lists;

import mcmultipart.raytrace.RayTraceUtils.AdvancedRayTraceResultPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.SyncEnum;
import sonar.logistics.LogisticsItems;
import sonar.logistics.api.displays.DisplayType;
import sonar.logistics.api.displays.IInfoContainer;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.displays.ScreenLayout;
import sonar.logistics.api.operator.OperatorMode;
import sonar.logistics.api.viewers.ViewerType;
import sonar.logistics.api.viewers.ViewersList;

public class HolographicDisplayPart extends DisplayScreenPart {

	public HolographicDisplayPart() {
		super();
	}

	public HolographicDisplayPart(EnumFacing dir, EnumFacing rotation) {
		super(dir, rotation);
	}

	@Override
	public DisplayType getDisplayType() {
		return DisplayType.HOLOGRAPHIC;
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
	@Override
	public ItemStack getItemStack() {
		return new ItemStack(LogisticsItems.holographicDisplay);
	}

}