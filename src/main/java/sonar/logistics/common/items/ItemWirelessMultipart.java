package sonar.logistics.common.items;

import mcmultipart.api.multipart.IMultipart;
import mcmultipart.item.ItemMultiPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import sonar.logistics.common.multiparts.wireless.AbstractWirelessPart;

/** for data emitter and data receiver */
public class ItemWirelessMultipart extends ItemMultiPart  {
	public final Class<? extends AbstractWirelessPart> type;

	public ItemWirelessMultipart(Class<? extends AbstractWirelessPart> type) {
		this.type = type;
	}

	@Override
	public IMultipart createPart(World world, BlockPos pos, EnumFacing side, Vec3d hit, ItemStack stack, EntityPlayer player) {
		try {
			return type.newInstance().setOwner(player).setCableFace(side);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
