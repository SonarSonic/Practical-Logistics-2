package sonar.logistics.connections.managers;

import java.util.ArrayList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.logistics.Logistics;

public class LockedDisplayData extends WorldSavedData {

	public static final String tag = "sonar.logistics.networks.lockedID";

	public LockedDisplayData(String name) {
		super(name);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		ArrayList<Integer> lockedIDs = new ArrayList();
		NBTTagList tag = nbt.getTagList("lockedIDs", NBT.TAG_INT);
		for (int t = 0; t < tag.tagCount(); t++) {
			int lockedID = tag.getIntAt(t);
		}
		Logistics.getDisplayManager().lockedIDs.addAll(lockedIDs);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for (Integer i : Logistics.getDisplayManager().lockedIDs) {
			list.set(i, new NBTTagInt(i));
		}
		compound.setTag("lockedIDs", list);
		return compound;
	}
}