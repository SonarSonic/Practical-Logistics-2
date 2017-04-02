package sonar.logistics.api.readers;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IWirelessStorageReader  {

	public UUID getEmitterUUID(ItemStack stack);
	
	public void readPacket(ItemStack stack, EntityPlayer player, ByteBuf buf, int id);
	
	public void writePacket(ItemStack stack, EntityPlayer player, ByteBuf buf, int id);
}
