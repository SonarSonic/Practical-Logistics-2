package sonar.logistics.api.tiles.readers;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IWirelessStorageReader  {

	int getEmitterIdentity(ItemStack stack);
	
	void readPacket(ItemStack stack, EntityPlayer player, ByteBuf buf, int id);
	
	void writePacket(ItemStack stack, EntityPlayer player, ByteBuf buf, int id);
}
