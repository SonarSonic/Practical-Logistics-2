package sonar.logistics.api.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;
import sonar.logistics.api.tiles.INetworkConnection;

public class CapabilityNetworkConnection {
	/*
	public static void register() {

		CapabilityManager.INSTANCE.register(INetworkConnection.class, new IStorage<INetworkConnection>() {

			@Override
			public NBTBase writeNBT(Capability<INetworkConnection> capability, INetworkConnection instance, EnumFacing side) {
				return null;
			}

			@Override
			public void readNBT(Capability<INetworkConnection> capability, INetworkConnection instance, EnumFacing side, NBTBase nbt) {}
		}, () -> null);
	}
	*/
}