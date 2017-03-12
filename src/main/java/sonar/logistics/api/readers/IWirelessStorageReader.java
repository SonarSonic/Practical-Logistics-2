package sonar.logistics.api.readers;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.common.items.WirelessStorageReader;
import sonar.logistics.connections.monitoring.MonitoredFluidStack;
import sonar.logistics.connections.monitoring.MonitoredItemStack;
import sonar.logistics.connections.monitoring.MonitoredList;

public interface IWirelessStorageReader  {

	public UUID getEmitterUUID(ItemStack stack);
	
	public void readPacket(ItemStack stack, EntityPlayer player, ByteBuf buf, int id);
	
	public void writePacket(ItemStack stack, EntityPlayer player, ByteBuf buf, int id);
}
