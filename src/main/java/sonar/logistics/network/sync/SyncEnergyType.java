package sonar.logistics.network.sync;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.SonarCore;
import sonar.core.api.energy.EnergyType;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.SyncPart;

public class SyncEnergyType extends SyncPart {

	public EnergyType type = EnergyType.RF;

	public SyncEnergyType(int id) {
		super(id);
	}
	
	public EnergyType getEnergyType(){
		return type;
	}

	public void incrementType() {
		int ordinal = SonarCore.energyTypes.getObjectID(type.getName()) + 1;
		EnergyType type = SonarCore.energyTypes.getRegisteredObject(ordinal);
		if (type == null) {
			this.type = SonarCore.energyTypes.getRegisteredObject(0);
		} else {
			this.type = type;
		}
		this.markChanged();
	}

	@Override
	public void writeToBuf(ByteBuf buf) {
		buf.writeInt(SonarCore.energyTypes.getObjectID(type.getName()));
	}

	@Override
	public void readFromBuf(ByteBuf buf) {
		type = SonarCore.energyTypes.getRegisteredObject(buf.readInt());
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		this.type = SonarCore.energyTypes.getRegisteredObject(nbt.getInteger("energyType"));
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		nbt.setInteger("energyType", SonarCore.energyTypes.getObjectID(this.type.getName()));
		return nbt;
	}

}
