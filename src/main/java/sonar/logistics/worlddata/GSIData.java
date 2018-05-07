package sonar.logistics.worlddata;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import sonar.core.helpers.NBTHelper;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.networking.ServerInfoHandler;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class GSIData extends WorldSavedData {

    public static final String IDENTIFIER = "sonar.logistics.networking.gsi";
    public static final Map<Integer, NBTTagCompound> unloadedGSI = new HashMap<>();

    public GSIData(String name) {
        super(name);
    }

    public GSIData() {
        super(IDENTIFIER);
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        NBTTagList tag = nbt.getTagList("gsi", Constants.NBT.TAG_COMPOUND);

        for (int t = 0; t < tag.tagCount(); t++) {
            NBTTagCompound gsiTag = tag.getCompoundTagAt(t);
            int registryID = gsiTag.getInteger("registryID");
            unloadedGSI.put(registryID, gsiTag);
        }

    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<Integer, DisplayGSI> display : ServerInfoHandler.instance().displays.entrySet()) {
            if (display.getValue() != null) {
                NBTTagCompound screenTag = new NBTTagCompound();
                screenTag.setInteger("registryID", display.getKey());
                display.getValue().writeData(screenTag, NBTHelper.SyncType.SAVE);
                list.appendTag(screenTag);
            }
        }
        for (Map.Entry<Integer, NBTTagCompound> display : unloadedGSI.entrySet()) {
            NBTTagCompound screenTag = display.getValue();
            screenTag.setInteger("registryID", display.getKey());
            list.appendTag(screenTag);
        }

        compound.setTag("gsi", list);
        //unloadedGSI.clear();
        return compound;
    }

    public boolean isDirty() {
        return true;
    }
}
