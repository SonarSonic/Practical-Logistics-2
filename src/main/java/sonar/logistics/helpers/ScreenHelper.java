package sonar.logistics.helpers;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;

public class ScreenHelper {

	/** in the form of [facing, rotation] */
	public static EnumFacing[] getScreenOrientation(EntityLivingBase placer, EnumFacing side) {
		EnumFacing facing = side;
		EnumFacing rotation = EnumFacing.NORTH;
		if (placer.rotationPitch > 75 || placer.rotationPitch < -75) {
			rotation = placer.getHorizontalFacing().getOpposite();
		} else {
			facing = placer.getHorizontalFacing().getOpposite();
		}
		return new EnumFacing[] { facing, rotation };
	}

}
