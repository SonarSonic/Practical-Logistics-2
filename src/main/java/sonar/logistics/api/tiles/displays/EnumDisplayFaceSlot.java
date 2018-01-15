package sonar.logistics.api.tiles.displays;

import mcmultipart.api.slot.EnumEdgeSlot;
import mcmultipart.api.slot.EnumSlotAccess;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import sonar.logistics.PL2Constants;

public enum EnumDisplayFaceSlot implements IPartSlot {// , IPartSlot.IFaceSlot {

	DOWN_DISPLAY(EnumFacing.DOWN), UP_DISPLAY(EnumFacing.UP), NORTH_DISPLAY(EnumFacing.NORTH), SOUTH_DISPLAY(EnumFacing.SOUTH), WEST_DISPLAY(EnumFacing.WEST), EAST_DISPLAY(EnumFacing.EAST);

	public static final EnumDisplayFaceSlot[] VALUES = values();

	private final ResourceLocation name;
	private final EnumFacing facing;

	private EnumDisplayFaceSlot(EnumFacing facing) {
		this.name = new ResourceLocation(PL2Constants.MODID, name().toLowerCase() +"display");
		this.facing = facing;
	}

	@Override
	public ResourceLocation getRegistryName() {
		return this.name;
	}

	public EnumFacing getFacing() {
		return facing;
	}

	public EnumDisplayFaceSlot getOpposite() {
		return EnumDisplayFaceSlot.VALUES[ordinal() ^ 1];
	}

	@Override
	public EnumSlotAccess getFaceAccess(EnumFacing face) {
		return face == this.getFacing() ? EnumSlotAccess.OVERRIDE : (face != getOpposite().getFacing() ? EnumSlotAccess.MERGE : EnumSlotAccess.NONE);
	}

	@Override
	public int getFaceAccessPriority(EnumFacing face) {
		return face == this.getFacing() ? 301 : (face != getOpposite().getFacing() ? 251 : 0);
	}

	@Override
	public EnumSlotAccess getEdgeAccess(EnumEdgeSlot edge, EnumFacing face) {
		return edge.getFace1() == face || edge.getFace2() == face ? EnumSlotAccess.OVERRIDE : EnumSlotAccess.NONE;
	}

	@Override
	public int getEdgeAccessPriority(EnumEdgeSlot edge, EnumFacing face) {
		return 201;
	}

	public static EnumDisplayFaceSlot fromFace(EnumFacing face) {
		return VALUES[face.ordinal()];
	}

}
