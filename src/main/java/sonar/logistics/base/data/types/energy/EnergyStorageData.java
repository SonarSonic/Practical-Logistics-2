package sonar.logistics.base.data.types.energy;

import sonar.core.api.energy.EnergyType;
import sonar.logistics.base.data.api.IData;

public class EnergyStorageData implements IData {

    public EnergyType type;
    public long energy, capacity;
    public boolean hasUpdated;

    public void preUpdate(){
        hasUpdated = false;
    }

    public boolean hasUpdated(){
        return hasUpdated;
    }
}
