package sonar.logistics.worlddata;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.logistics.PL2;

public class LockedDisplayData extends WorldSavedData {

	public static final String tag = "sonar.logistics.networks.lockedID";

	public LockedDisplayData(String name) {
		super(name);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		List<Integer> lockedIDs = Lists.newArrayList();
		NBTTagList tag = nbt.getTagList("lockedIDs", NBT.TAG_INT);
		for (int t = 0; t < tag.tagCount(); t++) {
			int lockedID = tag.getIntAt(t);
		}
		PL2.getDisplayManager().lockedIDs.addAll(lockedIDs);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for (Integer i : PL2.getDisplayManager().lockedIDs) {
			list.set(i, new NBTTagInt(i));
		}
		compound.setTag("lockedIDs", list);
		return compound;
	}
}