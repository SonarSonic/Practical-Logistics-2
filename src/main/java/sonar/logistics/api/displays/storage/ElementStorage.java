package sonar.logistics.api.displays.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.displays.elements.IClickableElement;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.IElementStorageHolder;
import sonar.logistics.api.displays.elements.ILookableElement;
import sonar.logistics.helpers.DisplayElementHelper;

public class ElementStorage implements INBTSyncable, Iterable<IDisplayElement> {

	public static final String TAG_NAME = "element_storage";

	protected Map<Integer, List<IDisplayElement>> elements = new HashMap<>();
	protected List<IClickableElement> clickables = new ArrayList<>();
	protected List<ILookableElement> lookables = new ArrayList<>();
	protected List<IElementStorageHolder> holders = new ArrayList<>();
	public IElementStorageHolder holder;
	public int elementCount;

	public ElementStorage(IElementStorageHolder holder) {
		this.holder = holder;
	}

	public void addElement(IDisplayElement e) {
		int id = DisplayElementHelper.getRegisteredID(e);
		elements.putIfAbsent(id, new ArrayList<>());
		if (ListHelper.addWithCheck(elements.get(id), e)) {
			if (e instanceof IClickableElement) {
				ListHelper.addWithCheck(clickables, (IClickableElement) e);
			}
			onElementAdded(e);
		}
	}

	public void removeElement(IDisplayElement e) {
		int id = DisplayElementHelper.getRegisteredID(e);
		if (elements.getOrDefault(id, new ArrayList<>()).remove(e)) {
			if (e instanceof IClickableElement) {
				clickables.remove((IClickableElement) e);
			}
			onElementRemoved(e);
		}
	}

	public IDisplayElement getElementFromIdentity(int identity) {
		for (IDisplayElement e : this) {
			if (e.getElementIdentity() == identity) {
				return e;
			}
		}
		for(IElementStorageHolder holder : this.getSubHolders()){
			IDisplayElement e = holder.getElements().getElementFromIdentity(identity);
			if(e!=null){
				return e;
			}
		}
		return null;
	}

	/** the replaced element */
	public IDisplayElement replaceElement(IDisplayElement element, int identity) {
		int id = DisplayElementHelper.getRegisteredID(element);
		elements.putIfAbsent(id, new ArrayList<>());
		IDisplayElement toReplace = null;
		for (IDisplayElement e : holder.getElements()) {
			if (e.getElementIdentity() == identity) {
				toReplace = e;
				break;
			}
		}
		addElement(element);
		if (toReplace != null) {
			removeElement(toReplace);
		}
		return null;
	}

	public void onElementAdded(IDisplayElement e) {
		elementCount++;
		e.setHolder(holder);
		if (e instanceof IClickableElement) {
			clickables.add((IClickableElement) e);
		}
		if (e instanceof ILookableElement) {
			lookables.add((ILookableElement) e);
		}
		if (e instanceof IElementStorageHolder) {
			holders.add((IElementStorageHolder) e);
		}

		holder.onElementAdded(e);
	}

	public void onElementRemoved(IDisplayElement e) {
		elementCount--;
		if (e instanceof IClickableElement) {
			clickables.remove((IClickableElement) e);
		}
		if (e instanceof ILookableElement) {
			lookables.remove((ILookableElement) e);
		}
		if (e instanceof IElementStorageHolder) {
			holders.remove((IElementStorageHolder) e);
		}
		holder.onElementRemoved(e);
	}

	public void forEachElement(Consumer<IDisplayElement> action) {
		elements.values().forEach(l -> l.forEach(action));
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		//Map<Integer, List<IDisplayElement>> newElements = new HashMap<>();// make sure you get old ones if there are any.
		//clickables.clear();
		////lookables.clear();
		//holders.clear();
		if(type.isType(SyncType.SAVE)){
			elements.clear();
		}
		
		NBTTagList tagList = nbt.getTagList(TAG_NAME, NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound elementsTag = tagList.getCompoundTagAt(i);
			if (!elementsTag.hasNoTags()) {
				int registryID = elementsTag.getInteger("rID");
				Class<? extends IDisplayElement> currentClass = DisplayElementHelper.getElementClass(registryID);

				NBTTagList subList = elementsTag.getTagList("list", NBT.TAG_COMPOUND);
				//newElements.putIfAbsent(registryID, new ArrayList<>());
				//List<IDisplayElement> elements = newElements.get(registryID);
				for (int s = 0; s < subList.tagCount(); s++) {
					NBTTagCompound eTag = subList.getCompoundTagAt(s);
					int iden = eTag.getInteger("identity");
					IDisplayElement ide = this.getElementFromIdentity(iden);
					if(ide!=null){
						ide.readData(eTag, type);
					}else{
						IDisplayElement e = DisplayElementHelper.loadElement(eTag, holder);
						addElement(e);						
					}
				}
			}
		}
		//elements = newElements;
		// find the elements in one list and not the other and mark them as removed?? - doesn't matter as client doesn't need to know
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTTagList tagList = new NBTTagList();
		for (Entry<Integer, List<IDisplayElement>> map : elements.entrySet()) {
			if (map.getValue().isEmpty()) {
				continue;
			}
			NBTTagList subList = new NBTTagList();
			for (IDisplayElement e : map.getValue()) {
				NBTTagCompound eTag = DisplayElementHelper.saveElement(new NBTTagCompound(), e, type);
				subList.appendTag(eTag);
			}
			if (!subList.hasNoTags()) {
				NBTTagCompound elementsTag = new NBTTagCompound();
				elementsTag.setInteger("rID", map.getKey());
				elementsTag.setTag("list", subList);
				tagList.appendTag(elementsTag);
			}
		}
		nbt.setTag(TAG_NAME, tagList);
		return nbt;
	}

	public int getElementCount() {
		return elementCount;
	}

	public boolean hasClickables() {
		if (!clickables.isEmpty()) {
			return true;
		}
		List<IElementStorageHolder> allHolders = getAllSubHolders(new ArrayList<>());
		for (IElementStorageHolder holder : allHolders) {
			if (!holder.getElements().getClickables().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public List<IClickableElement> getClickables() {
		return clickables;
	}

	public boolean hasLookables() {
		if (!lookables.isEmpty()) {
			return true;
		}
		List<IElementStorageHolder> allHolders = getAllSubHolders(new ArrayList<>());
		for (IElementStorageHolder holder : allHolders) {
			if (!holder.getElements().getClickables().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public List<ILookableElement> getLookables() {
		return lookables;
	}

	public boolean hasSubHolders() {
		return !holders.isEmpty();
	}

	public List<IElementStorageHolder> getSubHolders() {
		return holders;
	}

	public List<IElementStorageHolder> getAllSubHolders(List<IElementStorageHolder> holders) {
		getSubHolders().forEach(holder -> {
			ListHelper.addWithCheck(holders, holder);
			holder.getElements().getAllSubHolders(holders);
		});
		return holders;
	}

	@Override
	public Iterator<IDisplayElement> iterator() {
		return new StorageIterator(this);
	}

	public class StorageIterator implements Iterator<IDisplayElement> {
		public Iterator<Entry<Integer, List<IDisplayElement>>> entries;
		public Iterator<IDisplayElement> elements;

		public StorageIterator(ElementStorage s) {
			this.entries = s.elements.entrySet().iterator();
		}

		@Override
		public boolean hasNext() {
			if (elements != null && elements.hasNext()) {
				return true;
			}
			Iterator<IDisplayElement> nextList = null;
			while (nextList == null || !nextList.hasNext()) {
				if (entries.hasNext()) {
					nextList = entries.next().getValue().iterator();
				} else {
					return false;
				}
			}
			elements = nextList;
			return true;
		}

		@Override
		public IDisplayElement next() {
			return elements.next();
		}

	}

}
