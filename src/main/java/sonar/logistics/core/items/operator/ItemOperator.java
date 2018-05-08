package sonar.logistics.core.items.operator;

import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import sonar.core.SonarCore;
import sonar.core.api.IFlexibleGui;
import sonar.core.common.item.SonarItem;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RayTraceHelper;
import sonar.core.helpers.SonarHelper;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.FlexibleGuiHandler;
import sonar.logistics.PL2;
import sonar.logistics.api.core.items.operator.IOperatorTile;
import sonar.logistics.api.core.items.operator.IOperatorTool;
import sonar.logistics.api.core.items.operator.OperatorMode;
import sonar.logistics.base.ClientInfoHandler;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.channels.ContainerChannelSelection;
import sonar.logistics.base.channels.GuiChannelSelection;
import sonar.logistics.base.tiles.IChannelledTile;
import sonar.logistics.base.listeners.ILogicListenable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

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

	@Nonnull
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		Optional<IMultipartContainer> c = MultipartHelper.getContainer(world, pos);
		if (c.isPresent()) {
			IBlockState state = world.getBlockState(pos);
			RayTraceResult result = RayTraceHelper.getRayTraceEyes(player);

			IMultipartContainer container = c.get();
			if (result == null) {
				return EnumActionResult.PASS;
			}
			Optional<IMultipartTile> multipartTile = SonarMultipartHelper.getMultipartTileFromSlotID(world, pos, result.subHit);
			Object part = multipartTile.isPresent() ? multipartTile.get() : world.getTileEntity(pos);

			if (part == null) {
				return EnumActionResult.PASS;
			}
			OperatorMode mode = getOperatorMode(player.getHeldItem(hand));
			switch (mode) {
			case ANALYSE:
				// FIXME
				// DO ERROR CHECKING.
				break;
			case DEFAULT:
				if (!player.isSneaking()) {
					if (part instanceof IOperatorTile) {
						boolean operation = ((IOperatorTile) part).performOperation(result, mode, player, hand, facing, hitX, hitY, hitZ);
						return operation ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
					}
				}
				break;
			case CHANNELS:
				if (part instanceof IChannelledTile) {
					if (!world.isRemote) {
						IChannelledTile tile = (IChannelledTile) part;
						tile.getNetwork().sendConnectionsPacket(player);
						NBTTagCompound tag = new NBTTagCompound();
						tag.setBoolean(FlexibleGuiHandler.ITEM, true);
						tag.setInteger(FlexibleGuiHandler.ID, 0);
						tag.setInteger("hash", tile.getIdentity());
						// tag.setInteger("x", tile.getCoords().getX());
						// tag.setInteger("y", tile.getCoords().getY());
						// tag.setInteger("z", tile.getCoords().getZ());
						SonarCore.instance.guiHandler.openGui(false, player, world, pos, 0, tag);
					}
					return EnumActionResult.SUCCESS;
				}else{
					FontHelper.sendMessage("This block has no channels", world, player);
				}
				// FIXME - Need a version for Logic Monitors
				break;
			case INFO:
				break;
			case ROTATE:
				// return (part != null && part.rotate(facing)) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
			default:
				break;

			}
		}
		return EnumActionResult.PASS;
	}

	@Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
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
		switch (id) {
		case 0:
			ILogicListenable listen = ServerInfoHandler.instance().getIdentityTile(tag.getInteger("hash"));
			if (listen instanceof IChannelledTile) {
				IChannelledTile tile = (IChannelledTile) listen;
				return new ContainerChannelSelection(tile);
			}
		}
		return null;
	}

	@Override
	public Object getClientElement(ItemStack obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			ILogicListenable listen = ClientInfoHandler.instance().getIdentityTile(tag.getInteger("hash"));
			if (listen instanceof IChannelledTile) {
				IChannelledTile tile = (IChannelledTile) listen;
				return new GuiChannelSelection(player, tile, 0);
			}
		}
		return null;
	}

	@Override
	public void onGuiOpened(ItemStack obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {}

}
