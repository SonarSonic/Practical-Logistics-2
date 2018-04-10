package sonar.logistics.worlddata;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldSavedData;
import sonar.logistics.networking.ServerInfoHandler;

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
		ServerInfoHandler.instance().setIdentityCount(IDENTITY_COUNT);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		IDENTITY_COUNT = ServerInfoHandler.instance().getIdentityCount();
		compound.setInteger("count", IDENTITY_COUNT);
		return compound;
	}

	public boolean isDirty() {
		return true;
	}
}
