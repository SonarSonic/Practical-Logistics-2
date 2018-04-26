package sonar.logistics.api.displays.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import sonar.core.helpers.NBTHelper;
import sonar.logistics.PL2ASMLoader;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.errors.IInfoError;
import sonar.logistics.api.info.InfoUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DisplayGSISaveHandler {

    public static void readGSIData(DisplayGSI gsi, NBTTagCompound nbt, NBTHelper.SyncType type, DisplayGSISavedData data){
        data.readHandler.readData(gsi, nbt, type);
    }

    public static NBTTagCompound writeGSIData(DisplayGSI gsi, NBTTagCompound nbt, NBTHelper.SyncType type, DisplayGSISavedData data){
        return data.writeHandler.writeData(gsi, nbt, type);
    }

    public static void readGSIData(DisplayGSI gsi, NBTTagCompound nbt, NBTHelper.SyncType type, Collection<DisplayGSISavedData> data){
        for(DisplayGSISavedData save : DisplayGSISavedData.values()){
            if(data.contains(save)){
                save.readHandler.readData(gsi, nbt, type);
            }
        }
    }

    public static NBTTagCompound writeGSIData(DisplayGSI gsi, NBTTagCompound nbt, NBTHelper.SyncType type, Collection<DisplayGSISavedData> data){
        for(DisplayGSISavedData save : DisplayGSISavedData.values()){
            if(data.contains(save)){
                save.writeHandler.writeData(gsi, nbt, type);
            }
        }
        return nbt;
    }

    private static void readAllGSIData(DisplayGSI gsi, NBTTagCompound nbt, NBTHelper.SyncType type){
        for(DisplayGSISavedData save : DisplayGSISavedData.values()){
            if(save != DisplayGSISavedData.ALL_DATA) {
                save.readHandler.readData(gsi, nbt, type);
            }
        }
    }

    private static NBTTagCompound writeAllGSIData(DisplayGSI gsi, NBTTagCompound nbt, NBTHelper.SyncType type){
        for(DisplayGSISavedData save : DisplayGSISavedData.values()){
            if(save != DisplayGSISavedData.ALL_DATA) {
                save.writeHandler.writeData(gsi, nbt, type);
            }
        }
        return nbt;
    }

    public static enum DisplayGSISavedData{
        ALL_DATA(DisplayGSISaveHandler::readAllGSIData, DisplayGSISaveHandler::writeAllGSIData),
        SYNC_PARTS(DisplayGSISaveHandler::readSyncParts, DisplayGSISaveHandler::writeSyncParts),
        INFO_REFERENCES(DisplayGSISaveHandler::readInfoReferences, DisplayGSISaveHandler::writeInfoReferences),
        ERRORS(DisplayGSISaveHandler::readErrors, DisplayGSISaveHandler::writeErrors),
        CONTAINERS(DisplayGSISaveHandler::readContainers, DisplayGSISaveHandler::writeContainers);
        IGSIDataReadHandler readHandler;
        IGSIDataWriteHandler writeHandler;

        DisplayGSISavedData(IGSIDataReadHandler readHandler, IGSIDataWriteHandler writeHandler){
            this.readHandler = readHandler;
            this.writeHandler = writeHandler;
        }
    }

    public interface IGSIDataReadHandler{
        void readData(DisplayGSI gsi, NBTTagCompound nbt, NBTHelper.SyncType type);
    }
    public interface IGSIDataWriteHandler{
        NBTTagCompound writeData(DisplayGSI gsi, NBTTagCompound nbt, NBTHelper.SyncType type);
    }

    private static void readInfoReferences(DisplayGSI gsi, NBTTagCompound nbt, NBTHelper.SyncType type) {
        gsi.references = InfoUUID.readInfoList(nbt, "refs");
        gsi.cleanSavedErrors();
    }

    private static NBTTagCompound writeInfoReferences(DisplayGSI gsi, NBTTagCompound nbt, NBTHelper.SyncType type){
        nbt = InfoUUID.writeInfoList(nbt, gsi.references, "refs");
        return nbt;
    }

    private static void readErrors(DisplayGSI gsi, NBTTagCompound nbt, NBTHelper.SyncType type) {
        gsi.errors = PL2ASMLoader.readListFromNBT(IInfoError.class, nbt);
        if (gsi.getWorld().isRemote) {
            gsi.updateErroredElements();
        }
    }

    private static NBTTagCompound writeErrors(DisplayGSI gsi, NBTTagCompound nbt, NBTHelper.SyncType type) {
        PL2ASMLoader.writeListToNBT(IInfoError.class, gsi.errors, nbt);
        return nbt;
    }

    private static void readContainers(DisplayGSI gsi, NBTTagCompound nbt, NBTHelper.SyncType type) {
        if (!type.isType(NBTHelper.SyncType.SAVE)) {
            return;
        }
        List<Integer> loaded = new ArrayList<>();
        NBTTagList tagList = nbt.getTagList("containers", Constants.NBT.TAG_COMPOUND);
        tagList.forEach(tag ->{
            DisplayElementContainer c = loadContainer(gsi, (NBTTagCompound) tag, type);
            if(c.getElements().getElementCount() != 0) {
                loaded.add(c.getContainerIdentity());
            }
        });
        loaded.add(DisplayGSI.EDIT_CONTAINER_ID);
        List<Integer> toDelete = new ArrayList<>();
        gsi.forEachContainer(c -> {
            if (!loaded.contains(c.getContainerIdentity())) {
                toDelete.add(c.getContainerIdentity());
            }
        });
        toDelete.forEach(del -> {
            gsi.invalidateContainer(gsi.containers.get(del));
            gsi.containers.remove(del);
        });
    }

    private static NBTTagCompound writeContainers(DisplayGSI gsi, NBTTagCompound nbt, NBTHelper.SyncType type) {
        if (!type.isType(NBTHelper.SyncType.SAVE)) {
            return nbt;
        }
        NBTTagList tagList = new NBTTagList();
        gsi.forEachContainer(c -> {
            NBTTagCompound tag = saveContainer(gsi, c, type);
            if (!tag.hasNoTags()) {
                tagList.appendTag(tag);
            }
        });
        nbt.setTag("containers", tagList);
        return nbt;
    }

    public static NBTTagCompound saveContainer(DisplayGSI gsi, DisplayElementContainer c, NBTHelper.SyncType type) {
        if (gsi.isEditContainer(c) && !gsi.edit_mode.getObject()) {
            return new NBTTagCompound(); // don't send the edit container if it isn't being viewed
        }
        if(c.getElements().getElementCount() == 0){
            gsi.invalidateContainer(c);
            return new NBTTagCompound();
        }
        return c.writeData(new NBTTagCompound(), type);
    }

    public static DisplayElementContainer loadContainer(DisplayGSI gsi, NBTTagCompound nbt, NBTHelper.SyncType type) {
        int identity = nbt.getInteger("iden");
        DisplayElementContainer container = gsi.containers.get(identity);
        if (container == null) {
            container = new DisplayElementContainer();
            container.gsi = gsi;
            container.readData(nbt, type);
            if(container.getElements().getElementCount() != 0) {
                gsi.containers.put(identity, container);
                gsi.validateContainer(container);
            }
        } else {
            List<Integer> elements = new ArrayList<>();
            container.getElements().forEach(e -> elements.add(e.getElementIdentity()));
            container.readData(nbt, type);

            List<Integer> loaded = new ArrayList<>();
            container.getElements().forEach(e -> loaded.add(e.getElementIdentity()));
            elements.removeAll(loaded);

            for (Integer del : elements) {
                gsi.invalidateElement(container.getElements().getElementFromIdentity(del));
            }
        }
        return container;
    }


    private static void readSyncParts(DisplayGSI gsi, NBTTagCompound nbt, NBTHelper.SyncType type) {
        NBTTagCompound tag = nbt.getCompoundTag(gsi.getTagName());
        if (!tag.hasNoTags()) {
            NBTHelper.readSyncParts(tag, type, gsi.syncParts);
        }
    }


    private static NBTTagCompound writeSyncParts(DisplayGSI gsi, NBTTagCompound nbt, NBTHelper.SyncType type) {
        NBTTagCompound tag = NBTHelper.writeSyncParts(new NBTTagCompound(), type, gsi.syncParts, type.mustSync());
        if (!tag.hasNoTags()) {
            nbt.setTag(gsi.getTagName(), tag);
        }
        return nbt;
    }

}