package sonar.logistics.api.readers;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IWirelessStorageReader  {

	public int getEmitterIdentity(ItemStack stack);
	
	public void readPacket(ItemStack stack, EntityPlayer player, ByteBuf buf, int id);
	
	public void writePacket(ItemStack stack, EntityPlayer player, ByteBuf buf, int id);
}
