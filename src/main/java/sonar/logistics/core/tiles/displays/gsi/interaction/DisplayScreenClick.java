package sonar.logistics.core.tiles.displays.gsi.interaction;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.helpers.NBTHelper;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayElementContainer;
import sonar.logistics.core.tiles.displays.info.elements.base.IDisplayElement;
import sonar.logistics.core.tiles.displays.tiles.DisplayVectorHelper;

public class DisplayScreenClick {

	public DisplayGSI gsi;
	public BlockInteractionType type;
	public double clickX, clickY;
	public Vec3d intersect;
	public int identity;
	public boolean doubleClick = false;
	public boolean fakeGuiClick = false;

	@SideOnly(Side.CLIENT)
	public IDisplayElement clickedElement = null;
	@SideOnly(Side.CLIENT)
	public double subClickX = 0, subClickY = 0;
	@SideOnly(Side.CLIENT)
	public DisplayElementContainer clickedContainer = null;
	
	public DisplayScreenClick setClickPosition(double[] clickPosition){
		this.clickX = clickPosition[0];
		this.clickY = clickPosition[1];
		return this;
	}
	
	public static NBTTagCompound writeClick(DisplayScreenClick click, NBTTagCompound tag){
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("identity", click.identity);
		nbt.setInteger("type", click.type.ordinal());
		nbt.setDouble("clickX", click.clickX);
		nbt.setDouble("clickY", click.clickY);
		DisplayVectorHelper.writeVec3d(click.intersect, "intersect", tag, NBTHelper.SyncType.SAVE);
		nbt.setBoolean("doubleClick", click.doubleClick);	
		tag.setTag("displayclick", nbt);
		return tag;
		
	}
	
	public static DisplayScreenClick readClick(NBTTagCompound tag){
		DisplayScreenClick click = new DisplayScreenClick();
		NBTTagCompound nbt = tag.getCompoundTag("displayclick");
		click.identity = nbt.getInteger("identity");		
		click.type = BlockInteractionType.values()[nbt.getInteger("type")];
		click.clickX = nbt.getDouble("clickX");
		click.clickY = nbt.getDouble("clickY");
		click.intersect = DisplayVectorHelper.readVec3d("intersect", tag, NBTHelper.SyncType.SAVE);
		click.doubleClick = nbt.getBoolean("doubleClick");		
		return click;
		
	}
	
}
