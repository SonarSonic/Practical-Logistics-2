package sonar.logistics.common.items;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import sonar.core.SonarCore;
import sonar.core.api.IFlexibleGui;
import sonar.core.common.item.SonarItem;
import sonar.core.helpers.FontHelper;
import sonar.core.network.FlexibleGuiHandler;
import sonar.logistics.api.readers.IWirelessStorageReader;
import sonar.logistics.api.viewers.ViewerType;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.client.gui.GuiWirelessStorageEmitterList;
import sonar.logistics.client.gui.GuiWirelessStorageReader;
import sonar.logistics.common.containers.ContainerEmitterList;
import sonar.logistics.common.containers.ContainerStorageViewer;
import sonar.logistics.connections.managers.EmitterManager;
import sonar.logistics.helpers.ItemHelper;

public class WirelessStorageReader extends SonarItem implements IWirelessStorageReader, IFlexibleGui<ItemStack> {

	public static final String EMITTER_UUID = "uuid";
	public static final String NETWORK_ID = "id";

	@Override
	public UUID getEmitterUUID(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasUniqueId(EMITTER_UUID)) {
			return stack.getTagCompound().getUniqueId(EMITTER_UUID);
		}
		return null;
	}

	public void setEmitterUUID(UUID uuid, ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		nbt.setUniqueId(EMITTER_UUID, uuid);
		stack.setTagCompound(nbt);
	}

	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		if (!world.isRemote) {
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt == null) {
				nbt = new NBTTagCompound();
			}
			IDataEmitter emitter = EmitterManager.getEmitter(getEmitterUUID(stack));			
			if (emitter != null) {
				if(!emitter.getCoords().isChunkLoaded()){
					FontHelper.sendMessage("The Emitter isn't chunk loaded", world, player);					
					return new ActionResult(EnumActionResult.SUCCESS, stack);
				}
				NBTTagCompound tag = new NBTTagCompound();
				tag.setBoolean(FlexibleGuiHandler.ITEM, true);
				tag.setUniqueId(EMITTER_UUID, emitter.getIdentity());
				tag.setInteger(NETWORK_ID, emitter.getNetworkID());
				SonarCore.instance.guiHandler.openGui(false, player, world, player.getPosition(), 0, tag);
			}else{
				SonarCore.instance.guiHandler.openBasicItemStack(false, stack, player, world, player.getPosition(), 1);
			}
		}
		return new ActionResult(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void onGuiOpened(ItemStack obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			IDataEmitter emitter = EmitterManager.getEmitter(getEmitterUUID(obj));
			emitter.getViewersList().addViewer(player, ViewerType.FULL_INFO);
			break;
		case 1:
			EmitterManager.addViewer(player);
			break;
		}
	}

	@Override
	public Object getServerElement(ItemStack obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new ContainerStorageViewer(tag.getUniqueId(EMITTER_UUID), player);
		case 1:
			return new ContainerEmitterList(player);
		}
		return null;
	}

	@Override
	public Object getClientElement(ItemStack obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new GuiWirelessStorageReader(obj, tag.getUniqueId(EMITTER_UUID), tag.getInteger(NETWORK_ID), player);
		case 1:
			return new GuiWirelessStorageEmitterList(obj, player);
		}
		return null;
	}

	@Override
	public void readPacket(ItemStack stack, EntityPlayer player, ByteBuf buf, int id) {
		switch (id) {
		case 0:
			ItemStack selected = null;
			if (buf.readBoolean()) {
				selected = ByteBufUtils.readItemStack(buf);
			}
			IDataEmitter emitter = EmitterManager.getEmitter(getEmitterUUID(stack));
			ItemHelper.onNetworkItemInteraction(emitter.getNetwork(), emitter.getServerItems(), player, selected, buf.readInt());
			
			break;
		case 1:
			long msb = buf.readLong();
			long lsb = buf.readLong();
			UUID entityUUID = new UUID(msb, lsb);
			setEmitterUUID(entityUUID, stack);
			player.setHeldItem(EnumHand.MAIN_HAND, stack);
			break;
		}

	}

	@Override
	public void writePacket(ItemStack stack, EntityPlayer player, ByteBuf buf, int id) {
		switch (id) {
		case 0:
			break;
		}

	}

}
