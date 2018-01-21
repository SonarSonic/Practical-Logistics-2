package sonar.logistics.common.items;

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
import sonar.logistics.PL2;
import sonar.logistics.api.tiles.readers.IWirelessStorageReader;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.client.gui.GuiWirelessStorageEmitterList;
import sonar.logistics.client.gui.GuiWirelessStorageReader;
import sonar.logistics.common.containers.ContainerEmitterList;
import sonar.logistics.common.containers.ContainerStorageViewer;
import sonar.logistics.helpers.ItemHelper;

public class WirelessStorageReader extends SonarItem implements IWirelessStorageReader, IFlexibleGui<ItemStack> {

	public static final String EMITTER_UUID = "uuid";
	public static final String NETWORK_ID = "network_id";

	@Override
	public int getEmitterIdentity(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey(EMITTER_UUID)) {
			return stack.getTagCompound().getInteger(EMITTER_UUID);
		}
		return -1;
	}

	public void setEmitterIdentity(int identity, ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		nbt.setInteger(EMITTER_UUID, identity);
		stack.setTagCompound(nbt);
	}

	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (!world.isRemote && !stack.isEmpty()) {
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt == null) {
				nbt = new NBTTagCompound();
			}
			Integer identity = getEmitterIdentity(stack);
			IDataEmitter emitter = PL2.getWirelessDataManager().getEmitter(identity);
			if (emitter != null) {
				if (!emitter.getCoords().isChunkLoaded()) {
					FontHelper.sendMessage("The Emitter isn't chunk loaded", world, player);
					return new ActionResult(EnumActionResult.SUCCESS, stack);
				}
				NBTTagCompound tag = new NBTTagCompound();
				tag.setBoolean(FlexibleGuiHandler.ITEM, true);
				tag.setInteger(EMITTER_UUID, emitter.getIdentity());
				tag.setInteger(NETWORK_ID, emitter.getNetworkID());
				SonarCore.instance.guiHandler.openGui(false, player, world, player.getPosition(), 0, tag);
			} else {
				SonarCore.instance.guiHandler.openBasicItemStack(false, stack, player, world, player.getPosition(), 1);
			}
		}
		return new ActionResult(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void onGuiOpened(ItemStack obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			IDataEmitter emitter = PL2.getWirelessDataManager().getEmitter(getEmitterIdentity(obj));
			if (emitter != null) {
				emitter.sendRapidUpdate(player);
				emitter.getListenerList().addListener(player, ListenerType.LISTENER);
			}
			break;
		case 1:
			PL2.getWirelessDataManager().addViewer(player);
			break;
		}
	}

	@Override
	public Object getServerElement(ItemStack obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new ContainerStorageViewer(tag.getInteger(EMITTER_UUID), player);
		case 1:
			return new ContainerEmitterList(player);
		}
		return null;
	}

	@Override
	public Object getClientElement(ItemStack obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new GuiWirelessStorageReader(obj, tag.getInteger(EMITTER_UUID), tag.getInteger(NETWORK_ID), player);
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
			IDataEmitter emitter = PL2.getWirelessDataManager().getEmitter(getEmitterIdentity(stack));
			ItemHelper.onNetworkItemInteraction(emitter, emitter.getNetwork(), emitter.getServerItems(), player, selected, buf.readInt());

			break;
		case 1:
			int entityUUID = buf.readInt();
			setEmitterIdentity(entityUUID, stack);
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
