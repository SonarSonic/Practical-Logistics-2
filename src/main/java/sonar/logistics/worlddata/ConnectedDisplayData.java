package sonar.logistics.worlddata;

import java.util.Map.Entry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;

public class ConnectedDisplayData extends WorldSavedData {

	public static final String tag = "sonar.logistics.networks.displays";

	public ConnectedDisplayData(String string) {
		super(tag);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		NBTTagList tag = nbt.getTagList("displays", NBT.TAG_COMPOUND);
		for (int t = 0; t < tag.tagCount(); t++) {
			NBTTagCompound screenTag = tag.getCompoundTagAt(t);
			int registryID = screenTag.getInteger("registryID");
			ConnectedDisplay display = new ConnectedDisplay(registryID);
			display.readData(screenTag, SyncType.SAVE);
			PL2.getServerManager().connectedDisplays.put(registryID, display);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for (Entry<Integer, ConnectedDisplay> display : PL2.getServerManager().connectedDisplays.entrySet()) {
			if (display.getValue() != null) {
				NBTTagCompound screenTag = new NBTTagCompound();
				screenTag.setInteger("registryID", display.getKey());
				display.getValue().writeData(screenTag, SyncType.SAVE);
				list.appendTag(screenTag);
			}
		}
		compound.setTag("displays", list);
		return compound;
	}

	public boolean isDirty() {
		return true;
	}

	public static ConnectedDisplayData get(World world) {
		MapStorage storage = world.getMapStorage();
		ConnectedDisplayData instance = (ConnectedDisplayData) storage.getOrLoadData(ConnectedDisplayData.class, tag);

		if (instance == null) {
			instance = new ConnectedDisplayData(tag);
			storage.setData(tag, instance);
		}
		return instance;
	}
}