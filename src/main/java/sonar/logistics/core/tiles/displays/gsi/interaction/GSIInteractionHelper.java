package sonar.logistics.core.tiles.displays.gsi.interaction;

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
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.displays.tiles.IDisplay;
import sonar.logistics.core.tiles.connections.data.network.LogisticsNetworkHandler;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.info.InfoPacketHelper;
import sonar.logistics.core.tiles.displays.info.elements.DisplayElementHelper;
import sonar.logistics.core.tiles.displays.tiles.connected.ConnectedDisplay;
import sonar.logistics.core.tiles.displays.tiles.holographic.TileAbstractHolographicDisplay;
import sonar.logistics.core.tiles.readers.fluids.handling.DummyFluidHandler;
import sonar.logistics.core.tiles.readers.items.handling.ItemHelper;
import sonar.logistics.network.packets.PacketItemInteractionText;

public class GSIInteractionHelper {

    //// GRID SELECTION MODE \\\\\

    public static double getGridXScale(DisplayGSI gsi) {
        return Math.max(gsi.getDisplayScaling()[0] / 8, gsi.display.getDisplayType().width / 4);
    }

    public static double getGridYScale(DisplayGSI gsi) {
        return Math.max(gsi.getDisplayScaling()[1] / 8, gsi.display.getDisplayType().height / 4);
    }

    public static double getGridXPosition(DisplayGSI gsi, double x) {
        return DisplayElementHelper.toNearestMultiple(x, gsi.getDisplayScaling()[0], getGridXScale(gsi));
    }

    public static double getGridYPosition(DisplayGSI gsi, double y) {
        return DisplayElementHelper.toNearestMultiple(y, gsi.getDisplayScaling()[1], getGridYScale(gsi));
    }

    public static DisplayScreenClick createFakeClick(DisplayGSI gsi, double clickX, double clickY, boolean doubleClick, int key) {
        DisplayScreenClick fakeClick = new DisplayScreenClick();
        fakeClick.gsi = gsi;
        fakeClick.type = key == 0 ? BlockInteractionType.LEFT : BlockInteractionType.RIGHT;
        fakeClick.clickX = clickX;
        fakeClick.clickY = clickY;
        fakeClick.clickPos = gsi.getDisplay().getActualDisplay().getCoords().getBlockPos();
        fakeClick.identity = gsi.getDisplayGSIIdentity();
        fakeClick.doubleClick = false;
        fakeClick.fakeGuiClick = true;
        return fakeClick;
    }


	public static double[] getDisplayPositionFromXY(DisplayGSI container, BlockPos clickPos, EnumFacing face, float hitX, float hitY, float hitZ) {
		if(container.getDisplay() instanceof TileAbstractHolographicDisplay){
			return new double[]{hitX,hitY};
		}

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
		}
		clickPosition[0] = clickPosition[0] - container.getDisplay().getDisplayType().xPos;
		clickPosition[1] = clickPosition[1] - container.getDisplay().getDisplayType().yPos;
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
			return new double[] { trueX, 1 - trueZ };// this is only really for the way base are shown upside down
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

	/// ITEM & FLUID INTERACTION \\\\

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
							InfoPacketHelper.createRapidItemUpdate(Lists.newArrayList(stack), networkID);
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
							InfoPacketHelper.createRapidItemUpdate(Lists.newArrayList(storedItemStack.getItemStack()), networkID);
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
					InfoPacketHelper.createRapidFluidUpdate(Lists.newArrayList(toUpdate), networkID);
				}
			}
		}
	}
}