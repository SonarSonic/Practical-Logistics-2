package sonar.logistics.core.tiles.displays.tiles.small;

import net.minecraft.util.EnumFacing;
import sonar.logistics.api.core.tiles.connections.EnumCableRenderSize;
import sonar.logistics.api.core.tiles.displays.tiles.EnumDisplayType;
import sonar.logistics.api.core.tiles.displays.tiles.ISmallDisplay;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

public class TileDisplayScreen extends TileAbstractDisplay implements ISmallDisplay {

	public DisplayGSI container;

	@Override
	public DisplayGSI getGSI() {
		return container;
	}

	@Override
	public void setGSI(DisplayGSI gsi) {
		this.container = gsi;
	}

	@Override
	public int getInfoContainerID() {
		return getIdentity();
	}

	@Override
	public EnumDisplayType getDisplayType() {
		return EnumDisplayType.SMALL;
	}

	@Override
	public EnumCableRenderSize getCableRenderSize(EnumFacing dir) {
		return EnumCableRenderSize.INTERNAL;
	}
}