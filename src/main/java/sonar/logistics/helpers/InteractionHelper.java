package sonar.logistics.helpers;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import sonar.core.api.SonarAPI;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.BlockCoords;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayLayout;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.api.tiles.displays.DisplayScreenLook;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.networking.LogisticsNetworkHandler;
import sonar.logistics.networking.fluids.DummyFluidHandler;
import sonar.logistics.networking.items.ItemHelper;
import sonar.logistics.packets.PacketItemInteractionText;

public class InteractionHelper {

	public enum ItemInteractionType {
		ADD, REMOVE
    }

	public static Pair<Integer, ItemInteractionType> getItemsToRemove(BlockInteractionType type) {
		switch (type) {
		case LEFT:
			return new Pair(1, ItemInteractionType.REMOVE);
		case RIGHT:
			return new Pair(64, ItemInteractionType.ADD);
		case SHIFT_LEFT:
			return new Pair(64, ItemInteractionType.REMOVE);
		default:
			return new Pair(0, ItemInteractionType.ADD);
		}
	}

	public static void screenItemStackClicked(int networkID, StoredItemStack storedItemStack, DisplayScreenClick click, EntityPlayer player, NBTTagCompound clickTag) {
		Pair<Integer, ItemInteractionType> toRemove = getItemsToRemove(click.type);
		EnumFacing facing = click.gsi.getFacing();
		ILogisticsNetwork network = LogisticsNetworkHandler.instance().getNetwork(networkID);
		if (toRemove.a != 0 && network.isValid()) {
			switch (toRemove.b) {
			case ADD:
				ItemStack stack = player.getHeldItem(player.getActiveHand());
				if (!stack.isEmpty()) {
					long changed = 0;
					if (!click.doubleClick) {
						changed = ItemHelper.insertItemFromPlayer(player, network, player.inventory.currentItem);
					} else {
						changed = ItemHelper.insertInventoryFromPlayer(player, network, player.inventory.currentItem);
					}
					if (changed > 0) {
						long itemCount = ItemHelper.getItemCount(stack, network);
						PL2.network.sendTo(new PacketItemInteractionText(stack, itemCount, changed), (EntityPlayerMP) player);
						PacketHelper.createRapidItemUpdate(Lists.newArrayList(stack), networkID);
					}
				}
				break;
			case REMOVE:
				if (storedItemStack != null) {
					StoredItemStack extract = ItemHelper.extractItem(network, storedItemStack.copy().setStackSize(toRemove.a));
					if (extract != null) {
						BlockPos pos = click.clickPos.offset(facing);
						long r = extract.stored;
						double[] coords = click.getCoordinates();
						SonarAPI.getItemHelper().spawnStoredItemStackDouble(extract, player.getEntityWorld(), coords[0], coords[1], coords[2], facing);

						long itemCount = ItemHelper.getItemCount(storedItemStack.getItemStack(), network);
						PL2.network.sendTo(new PacketItemInteractionText(storedItemStack.getItemStack(), itemCount, -r), (EntityPlayerMP) player);
						PacketHelper.createRapidItemUpdate(Lists.newArrayList(storedItemStack.getItemStack()), networkID);
					}
				}
				break;
			default:
				break;
			}

		}
	}

	public static void onScreenFluidStackClicked(int networkID, StoredFluidStack fluidStack, DisplayScreenClick click, EntityPlayer player, NBTTagCompound clickTag) {
		ILogisticsNetwork network = LogisticsNetworkHandler.instance().getNetwork(networkID);
		if (network.isValid()) {
			IFluidHandler handler = new DummyFluidHandler(network, fluidStack);
			EnumHand hand = player.getActiveHand();
			ItemStack heldItem = player.getHeldItem(hand);
			FluidActionResult result = FluidActionResult.FAILURE;
			FluidStack toUpdate = fluidStack == null ? FluidUtil.getFluidContained(heldItem) : fluidStack.getFullStack();
			if (click.type == BlockInteractionType.RIGHT) {
				result = FluidUtil.tryEmptyContainer(heldItem, handler, Integer.MAX_VALUE, player, true);
			} else if (fluidStack != null && click.type == BlockInteractionType.LEFT) {
				result = FluidUtil.tryFillContainer(heldItem, handler, (int) Math.min(1000, fluidStack.stored), player, true);
			} else if (fluidStack != null && click.type == BlockInteractionType.SHIFT_LEFT) {
				result = FluidUtil.tryFillContainer(heldItem, handler, (int) Math.min(Integer.MAX_VALUE, fluidStack.stored), player, true);
			}
			if (result.isSuccess()) {
				player.setHeldItem(hand, result.getResult());
				if (toUpdate != null) {
					PacketHelper.createRapidFluidUpdate(Lists.newArrayList(toUpdate), networkID);
				}
			}
		}
	}

	public static double[] getDisplayPositionFromXY(DisplayGSI container, BlockPos clickPos, EnumFacing face, float hitX, float hitY, float hitZ) {
		double[] clickPosition = getClickPosition(face, hitX, hitY, hitZ);
		if (container.getDisplay() instanceof ConnectedDisplay) {
			ConnectedDisplay connected = (ConnectedDisplay) container.getDisplay();
			BlockCoords coords = connected.getCoords();
			if (coords != null) {
				BlockPos leftPos = coords.getBlockPos();
				int x = Math.abs(leftPos.getX() - clickPos.getX());
				int y = Math.abs(leftPos.getY() - clickPos.getY());
				int z = Math.abs(leftPos.getZ() - clickPos.getZ());
				if (container.getFacing().getAxis() != Axis.Y) {
					clickPosition[0] += x + z;
					clickPosition[1] += y;
				} else if (container.getFacing() == EnumFacing.UP) {
					clickPosition[0] += x;
					clickPosition[1] += z;
				} else if (container.getFacing() == EnumFacing.DOWN) {
					clickPosition[0] += x;
					clickPosition[1] += z;
				}
			}
		} else {
			clickPosition[1] = clickPosition[1] - ((container.getDisplayScaling()[1] / 2D)) -0.0625;
		}
		return clickPosition;
	}

	public static DisplayScreenLook getLookPosition(DisplayGSI container, BlockPos clickPos, EnumFacing face, float hitX, float hitY, float hitZ) {
		DisplayScreenLook position = new DisplayScreenLook();
		double[] clickPosition = getDisplayPositionFromXY(container, clickPos, face, hitX, hitY, hitZ);
		position.setContainerIdentity(container.getDisplayGSIIdentity());
		position.setLookPosition(clickPosition);
		return position;
	}

	public static DisplayScreenClick getClickPosition(DisplayGSI displayGSI, BlockPos clickPos, BlockInteractionType type, EnumFacing face, float hitX, float hitY, float hitZ) {
		DisplayScreenClick position = new DisplayScreenClick();
		double[] clickPosition = getDisplayPositionFromXY(displayGSI, clickPos, face, hitX, hitY, hitZ);
		position.setContainerIdentity(displayGSI.getDisplayGSIIdentity());
		position.setClickPosition(clickPosition);
		position.gsi = displayGSI;
		position.type = type;
		position.clickPos = clickPos;
		return position;
	}

	/* public static double[] getPos(IDisplay display, RenderInfoProperties renderInfo) { if (display instanceof ConnectedDisplay) { ConnectedDisplay connected = (ConnectedDisplay) display; if (connected.getTopLeftScreen() != null && connected.getTopLeftScreen().getCoords() != null) { BlockPos leftPos = connected.getTopLeftScreen().getCoords().getBlockPos(); double[] translation = renderInfo.getTranslation(); switch (display.getCableFace()) { case DOWN: break; case EAST: break; case NORTH: return new double[] { leftPos.getX() - translation[0], leftPos.getY() - translation[1], leftPos.getZ() }; case SOUTH: break; case UP: break; case WEST: break; default: break; } } } return new double[] { display.getCoords().getX(), display.getCoords().getY(), display.getCoords().getZ() }; } */

	/** in the form of double[] {start x, start y, width, height, actual x click, actual y click} */
	/* public static double[] getActualBox(double mouseX, double mouseY, DisplayInfo renderInfo) { double[] actualIntersect = new double[8]; double[] sect = getPositionedClickBox(renderInfo.container, renderInfo.getInfoPosition()); actualIntersect[0] = sect[0]; //actual start x actualIntersect[1] = sect[1]; //actual start y actualIntersect[2] = sect[2]; //actual finish x actualIntersect[3] = sect[3]; //actual finish y actualIntersect[4] = sect[2] - sect[0]; // actual width actualIntersect[5] = sect[3] - sect[1]; // actual height actualIntersect[6] = mouseX - sect[0]; // actual x click actualIntersect[7] = mouseY - sect[1]; // actual y click return actualIntersect; } public static int getSlot(DisplayScreenClick click, DisplayInfo renderInfo, int xSize, int ySize) { double[] actualIntersect = getActualBox(click.clickX, click.clickY, renderInfo); int xPos = (int) (actualIntersect[6] * xSize); int yPos = (int) (actualIntersect[7] * ySize); int slot = (int) (xPos + (yPos * (Math.ceil(actualIntersect[4] * xSize)))); return slot; } public static int getListSlot(DisplayScreenClick click, DisplayInfo renderInfo, double elementSize, double spacing, int maxPageSize) { double[] sect = getPositionedClickBox(renderInfo.container, renderInfo.getInfoPosition()); for (int i = 0; i < maxPageSize; i++) { double yStart = (i * elementSize) + (Math.max(0, (i - 1) * spacing)) + 0.0625 + sect[1]; double yEnd = yStart + elementSize; if (click.clickY > yStart && click.clickY < yEnd) { return i; } } return -1; } */

	public static double[] getTranslation(double[] scaling, DisplayLayout layout, int pos) {
		double width = scaling[0], height = scaling[1];
		switch (layout) {
		case DUAL:
			return new double[] { 0, pos == 1 ? height / 2 : 0, 0 };
		case GRID:
			return new double[] { pos == 1 || pos == 3 ? width / 2 : 0, (double) pos > 1 ? height / 2 : 0, 0 };
		case LIST:
			return new double[] { 0, pos * (height / 4), 0 };
		default:
			return new double[] { 0, 0, 0 };
		}
	}

	public static double[] getScaling(double[] scaling, DisplayLayout layout, int pos) {
		double width = scaling[0], height = scaling[1], scale = scaling[2];
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
	/* @Deprecated public static double[] getPositionedClickBox(IInfoContainer container, int pos) { double[] displaySize = container.getDisplayScaling(); double width = displaySize[0], height = displaySize[1]; switch (container.getLayout()) { case DUAL: return new double[] { 0, pos == 1 ? height / 2 : 0, pos == 1 ? width : width / 2, pos == 1 ? height : height / 2 }; case GRID: return new double[] { (pos == 1 || pos == 3 ? width / 2 : 0), (pos == 2 || pos == 3 ? height / 2 : 0), (pos == 1 || pos == 3 ? width : width / 2), (pos == 2 || pos == 3 ? height : height / 2) }; case LIST: return new double[] { 0, pos * (height / 4), width, (pos + 1) * (height / 4) }; default: return new double[] { 0, 0, width, height }; } }
	 * @Deprecated public static boolean canBeClickedStandard(DisplayInfo renderInfo, DisplayScreenClick click) { double[] intersect = getPositionedClickBox(renderInfo.container, renderInfo.getInfoPosition()); double x = click.clickX; double y = click.clickY; if (x >= intersect[0] + 0.0625 && x <= intersect[2] + 0.0625 && y >= intersect[1] + 0.0625 && y <= intersect[3] + 0.0625) { // add one pixel for the border of the screen return true; } return false; } */

	public static boolean withinX(double x, double[] clickBox) {
		return x >= Math.min(clickBox[0], clickBox[2]) && x <= Math.max(clickBox[0], clickBox[2]);
	}

	public static boolean withinY(double y, double[] clickBox) {
		return y >= Math.min(clickBox[1], clickBox[3]) && y <= Math.max(clickBox[1], clickBox[3]);
	}

	public static boolean overlapX(double x, double[] clickBox) {
		return x >= Math.min(clickBox[0], clickBox[2]) && x <= Math.max(clickBox[0], clickBox[2]);
	}

	public static boolean overlapY(double y, double[] clickBox) {
		return y >= Math.min(clickBox[1], clickBox[3]) && y <= Math.max(clickBox[1], clickBox[3]);
	}
	
	public static boolean checkClick(double x, double y, double[] clickBox) {
		return withinX(x, clickBox) && withinY(y, clickBox);
	}

	public static boolean checkOverlap(double[] elementBox, double[] clickBox) {
		if (elementBox.length != 4 || clickBox.length != 4) {
			return false;
		}
		boolean xOverlap = overlapX(elementBox[0], clickBox) || overlapX(elementBox[2], clickBox) || overlapX(clickBox[0], elementBox)|| overlapX(clickBox[1], elementBox);
		boolean yOverlap = overlapY(elementBox[1], clickBox) || overlapY(elementBox[3], clickBox) || overlapY(clickBox[1], elementBox) || overlapY(clickBox[3], elementBox);
		return xOverlap && yOverlap;
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

	public static double[] getClickCoordinates(DisplayScreenClick click) {
		IDisplay d = click.gsi.getDisplay().getActualDisplay();
		BlockPos dPos = d.getCoords().getBlockPos();
		double x = dPos.getX() + 0.5;
		double y = dPos.getY() + 0.5;
		double z = dPos.getZ() + 0.5;
		switch (d.getCableFace()) {
		case DOWN:
			y -= 1;
			x -= click.clickX;
			z += click.clickY;
			break;
		case EAST:
			x += 1;
			z -= click.clickX;
			y -= click.clickY;
			break;
		case NORTH:
			z -= 1;
			x -= click.clickX;
			y -= click.clickY;
			break;
		case SOUTH:
			z += 1;
			x += click.clickX;
			y -= click.clickY;
			break;
		case UP:
			y += 1;
			x -= click.clickX;
			z -= click.clickY;
			break;
		case WEST:
			x -= 1;
			z += click.clickX;
			y -= click.clickY;
			break;
		default:
			break;
		}
		return new double[] { x, y, z };
	}
	/* public static double[] getDisplaySize(IInfoContainer container) { DisplayType type = display.getDisplayType(); double width = type.width, height = type.height, scale = type.scale; if (display instanceof IScaleableDisplay) { double[] scaling = ((IScaleableDisplay) display).getScaling(); width = scaling[0]; height = scaling[1]; scale = scaling[2]; } return new double[] { width, height, scale }; } */
}