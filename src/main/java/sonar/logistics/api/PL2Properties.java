package sonar.logistics.api;

import java.util.Collection;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import sonar.core.helpers.SonarHelper;
import sonar.core.utils.LabelledAxisAlignedBB;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.tiles.cable.CableRenderType;

public class PL2Properties {
	public static final PropertyBool CLOCK_HAND = PropertyBool.create("hand");
	public static final PropertyBool HASDISPLAY = PropertyBool.create("display");
	public static final PropertyBool ACTIVE = PropertyBool.create("active");
	
	public static final PropertyCableFace NORTH = PropertyCableFace.create(EnumFacing.NORTH);
	public static final PropertyCableFace EAST = PropertyCableFace.create(EnumFacing.EAST);
	public static final PropertyCableFace SOUTH = PropertyCableFace.create(EnumFacing.SOUTH);
	public static final PropertyCableFace WEST = PropertyCableFace.create(EnumFacing.WEST);
	public static final PropertyCableFace DOWN = PropertyCableFace.create(EnumFacing.DOWN);
	public static final PropertyCableFace UP = PropertyCableFace.create(EnumFacing.UP);
	public static final PropertyCableFace[] PROPS = new PropertyCableFace[] { NORTH, EAST, SOUTH, WEST, DOWN, UP };

	public static class PropertyCableFace extends PropertyEnum<CableRenderType> {
		public EnumFacing face;

		protected PropertyCableFace(EnumFacing face, Collection<CableRenderType> allowedValues) {
			super(face.name().toLowerCase(), CableRenderType.class, allowedValues);
			this.face = face;
		}

		public static PropertyCableFace create(EnumFacing face) {
			return new PropertyCableFace(face, SonarHelper.convertArray(CableRenderType.values()));
		}
	}

	public static AxisAlignedBB getCableBox(CableRenderType connect, EnumFacing face) {
		double p = 0.0625;
		double w = (1 - 2 * p) / 2;
		double heightMin = connect.offsetBounds();
		double heightMax = 6 * p;
		switch (face) {
		case DOWN:
			return new LabelledAxisAlignedBB(w, heightMin, w, 1 - w, heightMax, 1 - w).labelAxis(face.toString());
		case EAST:
			return new LabelledAxisAlignedBB(1 - heightMax, w, w, 1 - heightMin, 1 - w, 1 - w).labelAxis(face.toString());
		case NORTH:
			return new LabelledAxisAlignedBB(w, w, heightMin, 1 - w, 1 - w, heightMax).labelAxis(face.toString());
		case SOUTH:
			return new LabelledAxisAlignedBB(w, w, 1 - heightMax, 1 - w, 1 - w, 1 - heightMin).labelAxis(face.toString());
		case UP:
			return new LabelledAxisAlignedBB(w, 1 - heightMax, w, 1 - w, 1 - heightMin, 1 - w).labelAxis(face.toString());
		case WEST:
			return new LabelledAxisAlignedBB(heightMin, w, w, heightMax, 1 - w, 1 - w).labelAxis(face.toString());
		default:
			return new AxisAlignedBB(w, heightMin, w, 1 - w, heightMax, 1 - w);
		}

	}

	public static AxisAlignedBB getStandardBox(EnumFacing face, PL2Multiparts multipart) {
		return getStandardBox(face, multipart.width, multipart.heightMin, multipart.heightMax);
	}

	public static AxisAlignedBB getStandardBox(EnumFacing face, double width, double heightMin, double heightMax) {
		double p = 0.0625;
		double w = (1 - width) / 2;
		switch (face) {
		case DOWN:
			return new AxisAlignedBB(w, heightMin, w, 1 - w, heightMax, 1 - w);
		case EAST:
			return new AxisAlignedBB(1 - heightMax, w, w, 1 - heightMin, 1 - w, 1 - w);
		case NORTH:
			return new AxisAlignedBB(w, w, heightMin, 1 - w, 1 - w, heightMax);
		case SOUTH:
			return new AxisAlignedBB(w, w, 1 - heightMax, 1 - w, 1 - w, 1 - heightMin);
		case UP:
			return new AxisAlignedBB(w, 1 - heightMax, w, 1 - w, 1 - heightMin, 1 - w);
		case WEST:
			return new AxisAlignedBB(heightMin, w, w, heightMax, 1 - w, 1 - w);
		default:
			return new AxisAlignedBB(w, heightMin, w, 1 - w, heightMax, 1 - w);
		}
	}
}
