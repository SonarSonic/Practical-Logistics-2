package sonar.logistics.api.tiles.cable;

import net.minecraft.util.IStringSerializable;

/**the different types of Data Cable connection, used for rendering, used on client side*/
public enum CableRenderType implements IStringSerializable {
	CABLE, INTERNAL, HALF, /* BLOCK, */ NONE;

	/**if the Data Cable can connect in this connections direction*/
	public boolean canConnect() {
		return this == CABLE || this == INTERNAL || this == HALF;
	}

	/**how far to offset the bounding box from the side of the box*/
	public double offsetBounds() {
		return this == INTERNAL ? 0.0625 : this == HALF ? 0.0625 * 3 : 0;
	}

	public String getName() {
		return this.toString().toLowerCase();
	}
}