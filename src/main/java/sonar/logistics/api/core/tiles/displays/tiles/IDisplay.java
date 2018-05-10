package sonar.logistics.api.core.tiles.displays.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.listener.ISonarListener;
import sonar.core.network.sync.ISyncableListener;
import sonar.logistics.base.tiles.INetworkTile;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.tiles.DisplayVectorHelper;

import javax.annotation.Nullable;

public interface IDisplay extends INetworkTile, ISyncableListener, ISonarListener, IFlexibleGui<IDisplay> {

	/**the identity which should be used to identity the {@link DisplayGSI}
	 * by default this is the identity of the display*/
	default int getInfoContainerID(){
		return this.getIdentity();
	}

	/**gets the gsiMap {@link DisplayGSI}, or Guided Screen Interface*/
	@Nullable
	DisplayGSI getGSI();

	/**sets the gsiMap {@link DisplayGSI}, this occurs once the GSI has been created & validated*/
	void setGSI(DisplayGSI gsi);

	/**this represents the side of the cable this screen is connected to
	 * and hence the direction it is facing*/
	EnumFacing getCableFace();

	/**called when this gsiMap {@link DisplayGSI} sends a container packet to all watchers
	 * in effect when the GSI has changed
	 * called on server only*/
	default void onInfoContainerPacket(){}

	/**called when this gsiMap {@link DisplayGSI} is validated
	 * called on server and client*/
	default void onGSIValidate(){}

	/**called when this gsiMap {@link DisplayGSI} is invalidated
	 * called on server and client*/
	default void onGSIInvalidate(){}

	/**override this in rare cases when the display used by the {@link DisplayGSI} is not present in the world */
	default IDisplay getActualDisplay(){
		return this;
	}

	/**returns the screens scaling vector in the form WIDTH / HEIGHT / DEPTH
	 * this must be implemented on every screen*/
	Vec3d getScreenScaling();

	/**returns the screen's width, obtained from the x value of {@link #getScreenScaling()}*/
	default double getWidth(){
		return getScreenScaling().x;
	}

	/**returns the screen's height, obtained from the y value of {@link #getScreenScaling()}*/
	default double getHeight(){
		return getScreenScaling().y;
	}

	/**returns the screen's depth, obtained from the z value of {@link #getScreenScaling()}*/
	default double getDepth(){
		return getScreenScaling().z;
	}

	/**returns the screens rotational vector in the form PITCH / YAW / ROLL
	 * by default this is calculated using the screens {@link #getCableFace()}*/
	default Vec3d getScreenRotation(){
		return DisplayVectorHelper.getScreenRotation(getCableFace());
	}

	/**returns the screen's pitch, obtained from the x value of {@link #getScreenRotation()}*/
	default double getPitch(){
		return getScreenRotation().x;
	}

	/**returns the screen's yaw, obtained from the y value of {@link #getScreenRotation()}*/
	default double getYaw(){
		return getScreenRotation().y;
	}

	/**returns the screen's roll, obtained from the z value of {@link #getScreenRotation()}*/
	default double getRoll(){
		return getScreenRotation().z;
	}

	/**the screens origin ( the center of the screen / it's point of rotation )
	 * by default this uses {@link #getCableFace()} to offset the display*/
	default Vec3d getScreenOrigin(){
		Vec3d pos = DisplayVectorHelper.convertVector(getCoords().getBlockPos());
		pos = pos.addVector(0.5, 0.5, 0.5); // place vector in the centre of the block pos
		pos = pos.add(DisplayVectorHelper.getFaceOffset(getCableFace(), 0.5)); // offset by the direction of the screen
		return pos;
	}

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
