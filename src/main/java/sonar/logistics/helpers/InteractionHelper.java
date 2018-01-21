package sonar.logistics.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import sonar.core.api.utils.BlockInteractionType;
import sonar.logistics.api.info.render.DisplayInfo;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayLayout;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.api.tiles.displays.IScaleableDisplay;

public class InteractionHelper {

	public static DisplayScreenClick getClickPosition(IDisplay display, BlockPos clickPos, BlockInteractionType type, EnumFacing face, float hitX, float hitY, float hitZ) {
		DisplayScreenClick position = new DisplayScreenClick();
		double[] clickPosition = getClickPosition(face, hitX, hitY, hitZ);
		if (display instanceof ILargeDisplay) {
			ConnectedDisplay connected = ((ILargeDisplay) display).getDisplayScreen();
			ILargeDisplay topLeft = connected.getTopLeftScreen();
			if (topLeft != null && topLeft.getCoords() != null) {
				BlockPos leftPos = topLeft.getCoords().getBlockPos();
				BlockPos displayPos = clickPos;
				int x = Math.abs(leftPos.getX() - displayPos.getX());
				int y = Math.abs(leftPos.getY() - displayPos.getY());
				int z = Math.abs(leftPos.getZ() - displayPos.getZ());
				if (topLeft.getCableFace().getAxis() != Axis.Y) {
					clickPosition[0] += x + z;
					clickPosition[1] += y;
				} else if (topLeft.getCableFace() == EnumFacing.UP) {
					clickPosition[0] += x;
					clickPosition[1] += z;
				} else if (topLeft.getCableFace() == EnumFacing.DOWN) {
					clickPosition[0] += x;
					clickPosition[1] += z;
				}
			}
		} else {
			//clickPosition[0] = clickPosition[0] - ((display.getDisplayType().width / 2));
			clickPosition[1] = clickPosition[1] - ((display.getDisplayType().height / 2));
		}
		position.setClickPosition(clickPosition);
		position.type = type;
		position.clickPos = clickPos;

		return position;
	}

	/* public static double[] getPos(IDisplay display, RenderInfoProperties renderInfo) { if (display instanceof ConnectedDisplay) { ConnectedDisplay connected = (ConnectedDisplay) display; if (connected.getTopLeftScreen() != null && connected.getTopLeftScreen().getCoords() != null) { BlockPos leftPos = connected.getTopLeftScreen().getCoords().getBlockPos(); double[] translation = renderInfo.getTranslation(); switch (display.getCableFace()) { case DOWN: break; case EAST: break; case NORTH: return new double[] { leftPos.getX() - translation[0], leftPos.getY() - translation[1], leftPos.getZ() }; case SOUTH: break; case UP: break; case WEST: break; default: break; } } } return new double[] { display.getCoords().getX(), display.getCoords().getY(), display.getCoords().getZ() }; } */

	public static int getSlot(DisplayScreenClick click, DisplayInfo renderInfo, int xSize, int ySize) {
		double[] sect = getIntersect(renderInfo.container.getDisplay(), renderInfo.container.getDisplay().getLayout(), renderInfo.getInfoPosition());
		double maxX = sect[2] - sect[0];
		double maxY = sect[3] - sect[1];
		double clickX = click.clickX - sect[0];
		double clickY = click.clickY - sect[1];
		int xPos = (int) (clickX * xSize);
		int yPos = (int) (clickY * ySize);
		int slot = (int) (xPos + (yPos * (Math.ceil(maxX * xSize))));
		return slot;
	}

	public static int getListSlot(DisplayScreenClick click, DisplayInfo renderInfo, double elementSize, double spacing, int maxPageSize) {
		double[] sect = getIntersect(renderInfo.container.getDisplay(), renderInfo.container.getDisplay().getLayout(), renderInfo.getInfoPosition());
		for (int i = 0; i < maxPageSize; i++) {
			double yStart = (i * elementSize) + (Math.max(0, (i - 1) * spacing)) + 0.0625 + sect[1];
			double yEnd = yStart + elementSize;
			if (click.clickY > yStart && click.clickY < yEnd) {
				return i;
			}
		}
		return -1;
	}

	public static double[] getTranslation(IDisplay display, DisplayLayout layout, int pos) {
		double[] displaySize = getDisplaySize(display);
		double width = displaySize[0], height = displaySize[1];
		switch (layout) {
		case DUAL:
			return new double[] { 0, pos == 1 ? height / 2 : 0, 0 };
		case GRID:
			return new double[] { pos == 1 || pos == 3 ? (double) width / 2 : 0, (double) pos > 1 ? height / 2 : 0, 0 };
		case LIST:
			return new double[] { 0, pos * (height / 4), 0 };
		default:
			return new double[] { 0, 0, 0 };
		}
	}

	public static double[] getScaling(IDisplay display, DisplayLayout layout, int pos) {
		double[] displaySize = getDisplaySize(display);
		double width = displaySize[0], height = displaySize[1], scale = displaySize[2];
		switch (layout) {
		case DUAL:
			return new double[] { width, height / 2, scale };
		case GRID:
			return new double[] { width / 2, height / 2, scale / 1.5 };
		case LIST:
			return new double[] { width, height / 4, scale / 1.5 };
		default:
			return new double[] { width, height, scale * 1.2 };
		}
	}

	/** in the form of start x, start y, end x, end y */
	public static double[] getIntersect(IDisplay display, DisplayLayout layout, int pos) {
		double[] displaySize = getDisplaySize(display);
		double width = displaySize[0], height = displaySize[1];
		switch (layout) {
		case DUAL:
			return new double[] { 0, pos == 1 ? height / 2 : 0, pos == 1 ? width : width / 2, pos == 1 ? height : height / 2 };
		case GRID:
			return new double[] { (pos == 1 || pos == 3 ? width / 2 : 0), (pos == 2 || pos == 3 ? height / 2 : 0), (pos == 1 || pos == 3 ? width : width / 2), (pos == 2 || pos == 3 ? height : height / 2) };
		case LIST:
			return new double[] { 0, pos * (height / 4), width, (pos + 1) * (height / 4) };
		default:
			return new double[] { 0, 0, width, height };
		}
	}

	public static boolean canBeClickedStandard(DisplayInfo renderInfo, DisplayScreenClick click) {
		IDisplay display = renderInfo.container.getDisplay();
		double[] intersect = getIntersect(display, display.getLayout(), renderInfo.getRenderProperties().infoPos);
		double x = click.clickX;
		double y = click.clickY;
		if (x >= intersect[0] && x <= intersect[2] && y >= intersect[1] && y <= intersect[3]) {
			return true;
		}
		return false;

	}

	public static double[] getClickPosition(EnumFacing face, float hitX, float hitY, float hitZ) {
		double trueX = face != EnumFacing.SOUTH ? 1 - hitX : hitX;
		double trueY = 1 - hitY;
		double trueZ = face != EnumFacing.WEST ? 1 - hitZ : hitZ;
		switch (face) {
		case DOWN:
			return new double[] { trueX, 1 - trueZ };// this is only really for the way displays are shown upside down
		case EAST:
			return new double[] { trueZ, trueY };
		case UP:
			return new double[] { trueX, trueZ };
		case WEST:
			return new double[] { trueZ, trueY };
		default:// south and north
			return new double[] { trueX, trueY };
		}
	}

	public static double[] getDisplaySize(IDisplay display) {
		DisplayType type = display.getDisplayType();
		double width = type.width, height = type.height, scale = type.scale;
		if (display instanceof IScaleableDisplay) {
			double[] scaling = ((IScaleableDisplay) display).getScaling();
			width = scaling[0];
			height = scaling[1];
			scale = scaling[2];
		}
		return new double[] { width, height, scale };
	}
}