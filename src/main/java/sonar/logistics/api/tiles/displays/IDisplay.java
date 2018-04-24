package sonar.logistics.api.tiles.displays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.listener.ISonarListener;
import sonar.core.network.sync.ISyncableListener;
import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.api.displays.DisplayGSI;

public interface IDisplay extends INetworkTile, ISyncableListener, ISonarListener, IFlexibleGui<IDisplay> {

	int getInfoContainerID();

	DisplayGSI getGSI();

	EnumFacing getCableFace();

	DisplayType getDisplayType();

	default void onInfoContainerPacket(){}

	default void onGSIValidate(){}

	default void onGSIInvalidate(){}
	
	IDisplay getActualDisplay();

	default void onGuiOpened(IDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		getGSI().onGuiOpened(obj, id, world, player, tag);
	}

	default Object getServerElement(IDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return getGSI().getServerElement(obj, id, world, player, tag);
	}

	default Object getClientElement(IDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag){
		return getGSI().getClientElement(obj, id, world, player, tag);
	}
}
