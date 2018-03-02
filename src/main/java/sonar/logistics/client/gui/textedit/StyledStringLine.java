package sonar.logistics.client.gui.textedit;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.api.displays.elements.WidthAlignment;

public class StyledStringLine implements INBTSyncable, Iterable<StyledString> {

	private List<StyledString> strings = Lists.newArrayList();
	private int cachedWidth = -1;
	private String cachedFormattedString;
	private String cachedUnformattedString;
	public WidthAlignment align = WidthAlignment.CENTERED;

	public StyledStringLine() {}

	public StyledStringLine(String s) {
		this(new StyledString(s));
	}

	public StyledStringLine(StyledString s) {
		strings.add(s);
	}

	public void render() {
		int offset = 0;
		for (StyledString s : strings) {
			FontHelper.text(s.getFormattedString(), offset, 0, s.style.getFontColour());
			offset += RenderHelper.fontRenderer.getStringWidth(s.getFormattedString());
		}

	}
	
	public List<StyledString> getStrings(){
		return strings;
	}
	
	public List<StyledString> setStrings(List<StyledString> strings){
		return this.strings = strings;
	}

	public Tuple<Integer, StyledString> getCursorPosition(int cursorIndex) {
		if (cursorIndex == -1) {
			return null;
		}
		int length = 0;
		for (StyledString ss : strings) {
			int sLength = ss.getStringLength();
			if (length + sLength > cursorIndex) {
				return new Tuple(cursorIndex - length, ss);
			}
			length += sLength;
		}
		return null;
	}

	public void addWithCombine(StyledString ss) {
		addWithCombine(strings, ss);
	}
	
	public static void addWithCombine(List<StyledString> strings, StyledString ss) {
		if (strings.isEmpty()) {
			strings.add(ss);
			return;
		} else {
			StyledString lastSS = strings.get(strings.size() - 1);
			if (lastSS.canCombine(ss)) {
				lastSS.combine(ss);
			} else {
				strings.add(ss);
			}
		}
	}

	public String getCachedFormattedString() {
		StringBuilder build = new StringBuilder();
		strings.forEach(s -> build.append(s.getFormattedString() + TextFormatting.RESET));
		return cachedFormattedString = build.toString();
	}

	public String getCachedUnformattedString() {
		StringBuilder build = new StringBuilder();
		strings.forEach(s -> build.append(s.getUnformattedString()));
		cachedUnformattedString = build.toString();
		return cachedUnformattedString = build.toString();
	}

	public String toString() {
		return getCachedFormattedString();
	}

	public int getStringWidth() {
		int width = 0;
		for (StyledString ss : strings) {
			width += ss.getStringWidth();
		}
		return cachedWidth = width;
	}

	public int getStringHeight() {
		return RenderHelper.fontRenderer.FONT_HEIGHT;
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		strings.clear();		
		NBTTagList tagList = nbt.getTagList("ss", NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound ssTag = tagList.getCompoundTagAt(i);
			StyledString ss = new StyledString();
			ss.readData(ssTag, type);
			strings.add(ss);
		}
		align = WidthAlignment.values()[nbt.getInteger("wa")];
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTTagList tagList = new NBTTagList();
		for (StyledString ss : strings) {
			tagList.appendTag(ss.writeData(new NBTTagCompound(), type));
		}
		if (!tagList.hasNoTags())
			nbt.setTag("ss", tagList);
		nbt.setInteger("wa", align.ordinal());
		return nbt;
	}

	@Override
	public Iterator<StyledString> iterator() {
		return strings.iterator();
	}
}
