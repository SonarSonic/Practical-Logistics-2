package sonar.logistics.worlddata;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import sonar.logistics.PL2;

public class IdentityCountData extends WorldSavedData {

	public static final String IDENTIFIER = "sonar.logistics.networks.identitycount";
	public int IDENTITY_COUNT = -1;

	public IdentityCountData(String name) {
		super(name);
	}

	public IdentityCountData() {
		super(IDENTIFIER);
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
}
