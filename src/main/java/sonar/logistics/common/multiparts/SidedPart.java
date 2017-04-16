package sonar.logistics.common.multiparts;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.MultipartHelper;
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

	public SyncEnum<EnumFacing> face = new SyncEnum(EnumFacing.values(), -1);
	{
		syncList.addPart(face);
	}

	public SidedPart setCableFace(EnumFacing face) {
		this.face.setObject(face);
		return this;
	}

	public EnumFacing getCableFace() {
		return face.getObject();
	}

	@Override
	public NetworkConnectionType canConnect(EnumFacing dir) {
		return dir != face.getObject() ? NetworkConnectionType.NETWORK : NetworkConnectionType.NONE;
	}

	//// MULTIPART \\\\

	@Override
	public EnumSet<PartSlot> getSlotMask() {
		return EnumSet.of(PartSlot.getFaceSlot(face.getObject()));
	}

	@Override
	public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
		List<AxisAlignedBB> boxes = Lists.newArrayList();
		addSelectionBoxes(boxes);
		boxes.forEach(box -> {
			if (box.intersectsWith(mask)) {
				list.add(box);
			}
		});
	}

	public void addSelectionBoxes(List<AxisAlignedBB> list) {
		list.add(PL2Properties.getStandardBox(face.getObject(), getMultipart()));
	}

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

	//// STATE \\\\

	@Override
	public IBlockState getActualState(IBlockState state) {
		return state.withProperty(PL2Properties.ORIENTATION, face.getObject());
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(MCMultiPartMod.multipart, new IProperty[] { PL2Properties.ORIENTATION });
	}

	//// PACKETS \\\\

	@Override
	public void writeUpdatePacket(PacketBuffer buf) {
		super.writeUpdatePacket(buf);
		face.writeToBuf(buf);
	}

	@Override
	public void readUpdatePacket(PacketBuffer buf) {
		super.readUpdatePacket(buf);
		face.readFromBuf(buf);
	}
}
