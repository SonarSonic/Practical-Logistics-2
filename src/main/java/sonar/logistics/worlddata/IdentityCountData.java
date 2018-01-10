package sonar.logistics.worlddata;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldSavedData;
import sonar.logistics.PL2;

public class IdentityCountData extends WorldSavedData {

	public static final String tag = "sonar.logistics.networks.identitycount";
	public int lastCount = -1;

	public IdentityCountData(String name) {
		super(name);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		PL2.getServerManager().setIdentityCount(nbt.getInteger("count"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("count", lastCount = PL2.getServerManager().getIdentityCount());
		return compound;
	}

	public boolean isDirty() {
		return PL2.getServerManager().getIdentityCount() != lastCount;
	}

}
