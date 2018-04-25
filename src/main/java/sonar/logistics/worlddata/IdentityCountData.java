package sonar.logistics.worlddata;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldSavedData;
import sonar.logistics.networking.LogisticsNetworkHandler;
import sonar.logistics.networking.ServerInfoHandler;
import sonar.logistics.networking.cabling.RedstoneConnectionHandler;

import javax.annotation.Nonnull;

public class IdentityCountData extends WorldSavedData {

	public static final String IDENTIFIER = "sonar.logistics.networks.identitycount";
	public int IDENTITY_COUNT = -1;
	public int NETWORK_COUNT = -1;
    public int REDSTONE_NETWORK_COUNT = -1;

	public IdentityCountData(String name) {
		super(name);
	}

	public IdentityCountData() {
		super(IDENTIFIER);
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbt) {
		IDENTITY_COUNT = nbt.getInteger("count");
		NETWORK_COUNT = nbt.getInteger("network_count");
        REDSTONE_NETWORK_COUNT = nbt.getInteger("r_network_count");
		ServerInfoHandler.instance().setIdentityCount(IDENTITY_COUNT);
		LogisticsNetworkHandler.instance().setIdentityCount(NETWORK_COUNT);
        RedstoneConnectionHandler.instance().setIdentityCount(REDSTONE_NETWORK_COUNT);
	}

	@Nonnull
    @Override
	public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
		IDENTITY_COUNT = ServerInfoHandler.instance().getIdentityCount();
		NETWORK_COUNT = LogisticsNetworkHandler.instance().getCurrentIdentity();
        REDSTONE_NETWORK_COUNT = RedstoneConnectionHandler.instance().getCurrentIdentity();
		compound.setInteger("count", IDENTITY_COUNT);
		compound.setInteger("network_count", NETWORK_COUNT);
        compound.setInteger("r_network_count", REDSTONE_NETWORK_COUNT);
		return compound;
	}

	public boolean isDirty() {
		return true;
	}
}
