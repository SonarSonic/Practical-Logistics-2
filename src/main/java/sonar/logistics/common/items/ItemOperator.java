package sonar.logistics.common.items;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.common.item.SonarItem;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.SonarHelper;
import sonar.logistics.PL2;
import sonar.logistics.api.operator.IOperatorTool;
import sonar.logistics.api.operator.OperatorMode;

public class ItemOperator extends SonarItem implements IOperatorTool, IFlexibleGui<ItemStack> {

	//// IOperatorTool \\\\

	@Override
	public OperatorMode getOperatorMode(ItemStack stack) {
		if (stack.hasTagCompound()) {
			return OperatorMode.values()[stack.getTagCompound().getInteger("mode")];
		}
		return OperatorMode.DEFAULT;
	}

	//// INTERACTIONS \\\\

	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		/*
		IMultipartContainer container = (IMultipartContainer) MultipartHelper.getPartContainer(world, pos);
		if (container != null) {
			
			Vec3d start = RayTraceUtils.getStart(player);
			Vec3d end = RayTraceUtils.getEnd(player);
			AdvancedRayTraceResultPart result = SonarMultipartHelper.collisionRayTrace(container, start, end);
			if (result == null) {
				return EnumActionResult.PASS;
			}
			IMultipart part = result.hit.partHit;
			OperatorMode mode = getOperatorMode(stack);
			switch (mode) {
			case ANALYSE:

				// DO ERROR CHECKING.
				break;
			case DEFAULT:
				if (!player.isSneaking()) {
					if (part != null && part instanceof IOperatorTile) {
						boolean operation = ((IOperatorTile) part).performOperation(result, mode, player, hand, facing, hitX, hitY, hitZ);
						return operation ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
					}
				} else {
				}

				break;
			case CHANNELS:
				if (part != null && part instanceof IChannelledTile) {
					if (!world.isRemote) {
						IChannelledTile tile = (IChannelledTile) part;
						tile.getNetwork().sendConnectionsPacket(player);
						NBTTagCompound tag = new NBTTagCompound();
						tag.setBoolean(FlexibleGuiHandler.ITEM, true);
						tag.setInteger(FlexibleGuiHandler.ID, 0);
						tag.setInteger("hash", tile.getIdentity());
						tag.setInteger("x", tile.getCoords().getX());
						tag.setInteger("y", tile.getCoords().getY());
						tag.setInteger("z", tile.getCoords().getZ());
						SonarCore.instance.guiHandler.openGui(false, player, world, pos, 0, tag);
					}
					return EnumActionResult.SUCCESS;
				}
				// FIXME - Need a version for Logic Monitors
				break;
			case INFO:
				break;
			case ROTATE:
				//return (part != null && part.rotatePart(facing)) ? EnumActionResult.SUCCESS : EnumActionResult.PASS; //FIXME
				return EnumActionResult.PASS;
			default:
				break;

			}
		}
		*/
		//FIXME
		return EnumActionResult.PASS;
	}

	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (player.isSneaking()) {
			stack = changeOperatorMode(stack);
			FontHelper.sendMessage("Mode: " + getOperatorMode(stack), world, player);
			return new ActionResult(EnumActionResult.PASS, stack);
		} else if (world.isRemote) {
			OperatorMode mode = getOperatorMode(stack);
			if (mode == OperatorMode.INFO) {
				boolean isUsing = !PL2.proxy.isUsingOperator();
				PL2.proxy.setUsingOperator(isUsing);
				player.sendMessage(new TextComponentTranslation("Display Info: " + TextFormatting.AQUA + isUsing));
			}
		}
		return new ActionResult(EnumActionResult.PASS, stack);
	}

	public ItemStack changeOperatorMode(ItemStack stack) {
		OperatorMode mode = SonarHelper.incrementEnum(getOperatorMode(stack), OperatorMode.values());
		NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
		tag.setInteger("mode", mode.ordinal());
		stack.setTagCompound(tag);
		boolean isUsing = mode == OperatorMode.INFO;
		return stack;
	}

	public void addInformation(ItemStack stack, World world, List list, ITooltipFlag flag) {
		super.addInformation(stack, world, list, flag);
		list.add("Mode: " + getOperatorMode(stack));
	}

	//// GUI \\\\ FIXME

	@Override
	public Object getServerElement(ItemStack obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		/* FIXME
		switch (id) {
		case 0:
			int hash = tag.getInteger("hash");
			BlockPos pos = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
			IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
			for (IMultipart part : container.getParts()) {
				if (part != null && part instanceof IChannelledTile) {
					IChannelledTile tile = (IChannelledTile) part;
					if (tile.getIdentity() == hash) {
						return new ContainerChannelSelection(tile);
					}
				}
			}
		}
		*/
		return null;
	}

	@Override
	public Object getClientElement(ItemStack obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
	/*
		switch (id) {
		case 0:
			int hash = tag.getInteger("hash");
			BlockPos pos = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
			IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
			for (IMultipart part : container.getParts()) {
				if (part != null && part instanceof IChannelledTile) {
					IChannelledTile tile = (IChannelledTile) part;
					if (tile.getIdentity() == hash) {
						return new GuiChannelSelection(player, tile, 0);
					}
				}
			}
		}
		*/
		return null;
	}

	@Override
	public void onGuiOpened(ItemStack obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {}

}
