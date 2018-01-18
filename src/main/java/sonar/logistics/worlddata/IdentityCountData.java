package sonar.logistics.worlddata;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import sonar.logistics.PL2;

public class IdentityCountData extends WorldSavedData {

	public static final String tag = "sonar.logistics.networks.identitycount";
	public int IDENTITY_COUNT = -1;

	public IdentityCountData(String string) {
		super(string);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		IDENTITY_COUNT = nbt.getInteger("count");
		PL2.getServerManager().setIdentityCount(IDENTITY_COUNT);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		IDENTITY_COUNT = PL2.getServerManager().getIdentityCount();
		compound.setInteger("count", IDENTITY_COUNT);
		return compound;
	}

	public boolean isDirty() {
		return true;
	}

	public static IdentityCountData get(World world) {
		MapStorage storage = world.getMapStorage();
		IdentityCountData instance = (IdentityCountData) storage.getOrLoadData(IdentityCountData.class, tag);

		if (instance == null) {
			instance = new IdentityCountData(tag);
			storage.setData(tag, instance);
		}
		return instance;
	}
}
