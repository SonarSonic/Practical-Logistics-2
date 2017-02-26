package sonar.logistics.common.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import sonar.core.SonarCore;
import sonar.core.api.IFlexibleGui;
import sonar.core.common.item.SonarItem;
import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.common.containers.ContainerGuide;

public class ItemGuide extends SonarItem implements IFlexibleGui<ItemStack> {

	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		if (!world.isRemote) {
			SonarCore.instance.guiHandler.openBasicItemStack(false, stack, player, world, player.getPosition(), 0);
		}
		return new ActionResult(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void onGuiOpened(ItemStack obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {}

	@Override
	public Object getServerElement(ItemStack obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {

		return id == 0 ? new ContainerGuide(player) : null;
	}

	@Override
	public Object getClientElement(ItemStack obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {

		return id == 0 ? new GuiGuide(player) : null;
	}

}
