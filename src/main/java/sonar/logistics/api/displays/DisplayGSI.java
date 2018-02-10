package sonar.logistics.api.displays;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.DirtyPart;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.ISyncableListener;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncableList;
import sonar.core.network.sync.SyncTagType.DOUBLE;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.api.displays.elements.DisplayElementContainer;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.helpers.DisplayElementHelper;

public class DisplayGSI extends DirtyPart implements ISyncPart, ISyncableListener {

	// the watched id, followed by how many references it has been attached to, it is loaded by the IDisplayElements
	public Map<InfoUUID, Integer> infoReferences = Maps.newHashMap();
	public Map<Integer, DisplayElementContainer> containers = Maps.newHashMap();
	public List<Integer> changedElements = Lists.newArrayList();
	public SyncableList syncParts = new SyncableList(this);

	private SyncTagType.INT container_identity = (INT) new SyncTagType.INT(0).setDefault(-1);
	private SyncTagType.INT display_container_identity_count = (INT) new SyncTagType.INT(1).setDefault(0);
	private SyncTagType<Double> width = new DOUBLE(2);
	private SyncTagType<Double> height = new DOUBLE(3);
	private SyncTagType<Double> scale = new DOUBLE(4);
	{
		syncParts.addParts(container_identity, display_container_identity_count, width, height, scale);
	}

	//// ELEMENTS \\\\

	public DisplayElementContainer addElementContainer(double width, double height, double scale) {
		int identity = createDisplayContainerIdentity();
		DisplayElementContainer container = new DisplayElementContainer(width,height,scale, identity);		
		containers.put(identity, container);
		return container;
	}

	public void removeElementContainer(int containerID) {
		containers.remove(containerID);
		//reset info references???
	}

	public void addElement(int containerID, IDisplayElement element) {
		containers.get(containerID).getElements().addElement(element);
		addInfoReferences(element.getInfoReferences());
	}

	public void removeElement(int containerID, IDisplayElement element) {
		containers.get(containerID).getElements().removeElement(element);
		removeInfoReferences(element.getInfoReferences());
	}

	/* public NBTTagCompound saveElement(IDisplayElement element, SyncType type) { NBTTagCompound elementTag = new NBTTagCompound(); if (type.isGivenType(SyncType.SAVE) || (type.isGivenType(SyncType.DEFAULT_SYNC) && hasElementChanged(element.getElementIdentity()))) { elementTag.setInteger("identity", element.getElementIdentity()); DisplayElementHelper.saveElement(elementTag, element, type); } return elementTag; } public IDisplayElement loadOrUpdateElement(NBTTagCompound tag, SyncType type) { int identity = tag.getInteger("identity"); IDisplayElement element = elements.get(identity); if(element==null){ element = DisplayElementHelper.loadElement(tag); addElement(element); return element; } return null; } */

	public boolean hasElementChanged(int identityID) {
		return changedElements.contains(identityID);
	}

	public void markElementChanged(int identityID) {
		ListHelper.addWithCheck(changedElements, identityID);
	}

	public int createDisplayContainerIdentity() {
		display_container_identity_count.increaseBy(1);
		return display_container_identity_count.getObject();
	}

	//// INFO REFERENCES \\\\

	public void resetInfoReferences() {
		infoReferences.clear();
		// containers.values().forEach(e -> addInfoReferences(e.getInfoReferences())); //FIXME
	}

	public void addInfoReferences(List<InfoUUID> uuid) {
		uuid.forEach(this::addInfoReference);
	}

	public void addInfoReference(InfoUUID uuid) {
		Integer current = infoReferences.get(uuid);
		int value = current == null ? 0 : 1;
		infoReferences.put(uuid, value + 1);
	}

	public void removeInfoReferences(List<InfoUUID> uuid) {
		uuid.forEach(this::removeInfoReference);
	}

	public void removeInfoReference(InfoUUID uuid) {
		int current = infoReferences.get(uuid);
		int newValue = current - 1;
		if (newValue == 0) {
			infoReferences.remove(uuid);
		} else {
			infoReferences.put(uuid, newValue);
		}
	}

	public void updateChangedInfo(List<InfoUUID> uuid) {
		for (DisplayElementContainer e : containers.values()) {
			/* FIXME if(e.getInfoReferences().contains(e)){ e.onElementChanged(); } */
		}
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		if (type.isType(SyncType.SAVE)) {
			NBTTagList tagList = nbt.getTagList("elements", NBT.TAG_COMPOUND);
			tagList.forEach(tag -> loadContainer((NBTTagCompound) tag, type));
		}
		NBTTagCompound tag = nbt.getCompoundTag(this.getTagName());
		if (!tag.hasNoTags()) {
			NBTHelper.readSyncParts(tag, type, syncParts);
		}
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		if (type.isType(SyncType.SAVE)) {
			NBTTagList tagList = new NBTTagList();
			containers.values().forEach(c -> tagList.appendTag(saveContainer(c, type)));
			nbt.setTag("containers", tagList);
		}
		NBTTagCompound tag = NBTHelper.writeSyncParts(new NBTTagCompound(), type, syncParts, type.mustSync());
		if (!tag.hasNoTags()) {
			nbt.setTag(this.getTagName(), tag);
		}
		return nbt;
	}

	public NBTTagCompound saveContainer(DisplayElementContainer c, SyncType type) {
		return c.writeData(new NBTTagCompound(), type);
	}

	public DisplayElementContainer loadContainer(NBTTagCompound nbt, SyncType type) {
		int identity = nbt.getInteger("iden");
		DisplayElementContainer container = containers.get(identity);
		if (container == null) {
			container = NBTHelper.instanceNBTSyncable(DisplayElementContainer.class, nbt);
			containers.put(identity, container);
		} else {
			container.readData(nbt, type);
		}
		return container;
	}

	@Override
	public boolean canSync(SyncType sync) {
		return sync.isType(SyncType.SAVE, SyncType.SPECIAL);
	}

	@Override
	public String getTagName() {
		return "gsi";
	}

	@Override
	public void markChanged(IDirtyPart part) {
		markChanged(); // alert the display
	}
}
