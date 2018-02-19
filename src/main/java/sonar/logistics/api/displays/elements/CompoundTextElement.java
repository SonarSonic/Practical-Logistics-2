package sonar.logistics.api.displays.elements;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.InfoReference;
import sonar.logistics.api.displays.ReferenceType;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.helpers.InfoRenderer;

@DisplayElementType(id = CompoundTextElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class CompoundTextElement extends AbstractDisplayElement {

	private String unformattedText;
	protected String formattedText;

	protected List<InfoReference> references = Lists.newArrayList();
	private List<InfoUUID> cachedList;

	public CompoundTextElement() {}

	public CompoundTextElement(String unformattedText) {
		super();
		this.unformattedText = unformattedText;
	}
	
	@Override
	public void onElementChanged() {
		super.onElementChanged();
		this.formattedText = null;
		this.cachedList = null;
	}

	@Override
	int[] createUnscaledWidthHeight() {
		return new int[] { InfoRenderer.getStringWidth(getRepresentiveString()), InfoRenderer.getStringHeight() };
	}

	public void setReferences(List<InfoReference> references) {
		this.references = references;
	}

	@Override
	public String getRepresentiveString() {
		if (formattedText == null) {
			formattedText = createFormattedText();
		}
		return formattedText;
	}

	public static final String REF = "£";

	public String createFormattedText() {
		DisplayGSI gsi = getHolder().getContainer().getGSI();
		gsi.updateInfoReferences();
		gsi.updateCachedInfo();
		String text = unformattedText;
		for (InfoReference ref : references) {
			IInfo info = gsi.getCachedInfo(ref.uuid);
			if (info != null) {
				text = text.replaceFirst(REF, ref.refType.getRefString(info));
			}
		}
		return text;
	}

	@Override
	public List<InfoUUID> getInfoReferences() {
		if (cachedList == null) {
			List<InfoUUID> cached = Lists.newArrayList();
			references.forEach(ref -> ListHelper.addWithCheck(cached, ref.uuid));
			cachedList = cached;
		}
		return cachedList;
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		unformattedText = nbt.getString("text");
		
		List<InfoReference> refs = Lists.newArrayList();
		NBTTagList list = nbt.getTagList("refs", NBT.TAG_COMPOUND);
		
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound c = list.getCompoundTagAt(i);
			refs.add(NBTHelper.instanceNBTSyncable(InfoReference.class, c));
		}
		references = refs;

	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		nbt.setString("text", unformattedText);
		nbt.setInteger("refSize", references.size());
		NBTTagList list = new NBTTagList();
		int i =0;
		for (InfoReference e : references) {
			NBTTagCompound tag = new NBTTagCompound();
			//tag.setInteger("pos", i);
			e.writeData(tag, type);
			//tag.setInteger("t", e.getValue().ordinal());
			list.appendTag(tag);
			i++;
		}
		nbt.setTag("refs", list);
		return nbt;
	}

	public static final String REGISTRY_NAME = "comp_text";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}
}
