package sonar.logistics.common.multiparts;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import mcmultipart.MCMultiPartMod;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.PartSlot;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.network.sync.SyncEnum;
import sonar.core.utils.Pair;
import sonar.logistics.api.PL2Properties;
import sonar.logistics.api.tiles.cable.NetworkConnectionType;

public abstract class SidedPart extends LogisticsPart implements ISlottedPart {

	@Override
	public NetworkConnectionType canConnect(EnumFacing dir) {
		return dir != face.getObject() ? NetworkConnectionType.NETWORK : NetworkConnectionType.NONE;
	}

	//// MULTIPART \\\\
	@Override
	public EnumFacing[] getValidRotations() {
		return EnumFacing.VALUES;
	}

	@Override
	public boolean rotatePart(EnumFacing axis) {
		Pair<Boolean, EnumFacing> rotate = rotatePart(face.getObject(), axis);
		if (rotate.a && isServer()) {
			UUID uuid = getUUID();
			BlockPos pos = getPos();
			World world = getWorld();
			getContainer().removePart(this);
			face.setObject(rotate.b);
			isValid = false;
			MultipartHelper.addPart(world, pos, this, uuid);
			sendUpdatePacket(true);
		}
		return rotate.a;
	}

}
