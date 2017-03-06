package sonar.logistics.common.items;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sonar.core.common.item.SonarItem;
import sonar.core.helpers.FontHelper;
import sonar.logistics.api.wireless.IEntityTransceiver;

public class WirelessEntityTransceiver extends SonarItem implements IEntityTransceiver {

	//// IEntityTransceiver \\\\\
	
	@Override
	public UUID getEntityUUID(ItemStack stack) {
		if (stack.hasTagCompound()) {
			return stack.getTagCompound().getUniqueId("uuid");
		}
		return null;
	}
	
	//// INTERACTIONS \\\\
	
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		if (player.isSneaking()) {
			if (!world.isRemote) {
				onRightClickEntity(player, stack, player);
			}
			return new ActionResult(EnumActionResult.SUCCESS, stack);
		}
		return new ActionResult(EnumActionResult.PASS, stack);
	}

	@Override
	public void onRightClickEntity(EntityPlayer player, ItemStack stack, Entity entity) {
		if (entity != null) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag == null)
				tag = new NBTTagCompound();
			tag.setUniqueId("uuid", entity.getPersistentID());
			tag.setString("targetName", entity.getDisplayName().getUnformattedText());
			FontHelper.sendMessage(stack.hasTagCompound() ? "Overwritten Entity" : "Saved Entity", player.getEntityWorld(), player);
			stack.setTagCompound(tag);
		}
	}

	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
		if (!player.isSneaking() &&!player.getEntityWorld().isRemote){
			onRightClickEntity(player, stack, target);
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par) {
		super.addInformation(stack, player, list, par);
		if (stack.hasTagCompound()) {
			list.add("Entity: " + TextFormatting.ITALIC + FontHelper.translate(stack.getTagCompound().getString("targetName")));
			list.add("UUID: " + TextFormatting.ITALIC + getEntityUUID(stack).toString());
		}
	}
}
