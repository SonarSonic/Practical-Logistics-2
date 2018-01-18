package sonar.logistics.api.tiles.displays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import sonar.core.api.utils.BlockInteractionType;

public class DisplayScreenClick {

	public BlockInteractionType type;
	public double clickX, clickY;
	public BlockPos clickPos;
	public boolean doubleClick = false;
	
	public DisplayScreenClick setClickPosition(double[] clickPosition){
		this.clickX = clickPosition[0];
		this.clickY = clickPosition[1];
		return this;
	}
	
	public void setDoubleClick(boolean bool){
		doubleClick=bool;
	}
	
	public static NBTTagCompound writeClick(DisplayScreenClick click, NBTTagCompound tag){
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("type", click.type.ordinal());
		nbt.setDouble("clickX", click.clickX);
		nbt.setDouble("clickY", click.clickY);
		nbt.setInteger("x", click.clickPos.getX());
		nbt.setInteger("y", click.clickPos.getY());
		nbt.setInteger("z", click.clickPos.getZ());
		nbt.setBoolean("doubleClick", click.doubleClick);	
		tag.setTag("displayclick", nbt);
		return tag;
		
	}
	
	public static DisplayScreenClick readClick(NBTTagCompound tag){
		DisplayScreenClick click = new DisplayScreenClick();
		NBTTagCompound nbt = tag.getCompoundTag("displayclick");
		click.type = BlockInteractionType.values()[nbt.getInteger("type")];
		click.clickX = nbt.getDouble("clickX");
		click.clickY = nbt.getDouble("clickY");
		click.clickPos = new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
		click.doubleClick = nbt.getBoolean("doubleClick");		
		return click;
		
	}
	/*
	public DisplayClickPosition setDisplaySize(double[] displaySize){
		this.width = displaySize[0]; 
		this.height = displaySize[1];
		this.size = displaySize[2];
		return this;
	}
	*/
	
}
