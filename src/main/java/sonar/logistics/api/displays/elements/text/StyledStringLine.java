package sonar.logistics.api.displays.elements.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.WidthAlignment;
import sonar.logistics.helpers.DisplayElementHelper;

public class StyledStringLine implements INBTSyncable, Iterable<IStyledString> {

	private StyledTextElement text;
	private List<IStyledString> strings = new ArrayList<>();
	private int cachedWidth = -1;
	private String cachedFormattedString;
	private String cachedUnformattedString;
	private int[] unformattedSize;
	private WidthAlignment align = WidthAlignment.CENTERED;

	public StyledStringLine(StyledTextElement text) {
		this.setText(text);
	}

	public StyledStringLine(StyledTextElement text, String s) {
		this(text, new StyledString(s));
	}

	public StyledStringLine(StyledTextElement text, IStyledString s) {
		this.setText(text);
		setStrings(Lists.newArrayList(s));
	}

	public StyledTextElement setText(StyledTextElement text) {
		return this.text = text;
	}

	public StyledTextElement getText() {
		return text;
	}

	public DisplayGSI getGSI() {
		return getText().getGSI();
	}

	public void render() {
		int offset = 0;
		for (IStyledString s : strings) {
			FontHelper.text(s.getFormattedString(), offset, 0, s.getStyle().getFontColour());
			offset += RenderHelper.fontRenderer.getStringWidth(s.getFormattedString());
		}
	}

	public List<IStyledString> getStrings() {
		return strings;
	}

	public List<IStyledString> setStrings(List<IStyledString> strings) {
		strings.forEach(ss -> ss.setLine(this));
		this.strings = strings;
		this.updateTextContents();
		return this.strings;
	}
	
	public List<Integer> getContainedActions(){
		List<Integer> actions = new ArrayList<>();
		forEach(ss -> ListHelper.addWithCheck(actions, ss.getStyle().getActionID()));
		return actions;
	}

	public WidthAlignment getAlign() {
		return align;
	}

	public void setAlign(WidthAlignment align) {
		this.align = align;
		updateTextScaling();
	}

	public void updateTextScaling() {
		if (getText() != null) {
			getText().updateTextScaling();
		}
	}

	public void updateTextContents() {
		if (getText() != null) {
			getText().updateTextContents();
			cachedUnformattedString = null;
			cachedFormattedString = null;
			cachedWidth = -1;
			updateTextScaling();
		}
	}

	public Tuple<Integer, IStyledString> getCursorPosition(int cursorIndex) {
		if (cursorIndex == -1) {
			return null;
		}
		int length = 0;
		for (IStyledString ss : strings) {
			int sLength = ss.getStringLength();
			if (length + sLength > cursorIndex) {
				return new Tuple(cursorIndex - length, ss);
			}
			length += sLength;
		}
		return null;
	}

	public void addWithCombine(IStyledString ss) {
		ss.setLine(this);
		addWithCombine(strings, ss);
		updateTextContents();
	}

	public static void addWithCombine(List<IStyledString> strings, IStyledString ss) {
		if (strings.isEmpty()) {
			strings.add(ss);
			return;
		} else {
			IStyledString lastSS = strings.get(strings.size() - 1);
			if (lastSS.canCombine(ss)) {
				lastSS.combine(ss);
			} else {
				strings.add(ss);
			}
		}
	}

	public String getCachedFormattedString() {
		if (cachedFormattedString == null) {
			StringBuilder build = new StringBuilder();
			strings.forEach(s -> build.append(s.getFormattedString() + TextFormatting.RESET));
			cachedFormattedString = build.toString();
		}
		return cachedFormattedString;
	}

	public String getCachedUnformattedString() {
		if (cachedUnformattedString == null) {
			StringBuilder build = new StringBuilder();
			strings.forEach(s -> build.append(s.getUnformattedString()));
			cachedUnformattedString = build.toString();
		}
		return cachedUnformattedString;
	}

	public String toString() {
		return getCachedFormattedString();
	}

	public int getStringWidth() {
		if (cachedWidth == -1) {
			int width = 0;
			for (IStyledString ss : strings) {
				width += ss.getStringWidth();
			}
			cachedWidth = width;
		}
		return cachedWidth;
	}

	public int getStringHeight() {
		return RenderHelper.fontRenderer.FONT_HEIGHT;
	}

	public StyledStringLine lightCopy(StyledTextElement text) {
		StyledStringLine line = new StyledStringLine(text);
		line.setStrings(Lists.newArrayList(strings));
		return line;
	}

	public StyledStringLine deepCopy(StyledTextElement text) {
		StyledStringLine line = new StyledStringLine(text);
		List<IStyledString> newStrings = new ArrayList<>();
		for (IStyledString ss : strings) {
			newStrings.add(ss.copy());
		}
		line.setStrings(newStrings);
		return line;
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		strings.clear();
		NBTTagList tagList = nbt.getTagList("ss", NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound ssTag = tagList.getCompoundTagAt(i);
			strings.add(DisplayElementHelper.loadStyledString(this, ssTag));
		}
		setAlign(WidthAlignment.values()[nbt.getInteger("wa")]);
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTTagList tagList = new NBTTagList();
		for (IStyledString ss : strings) {
			tagList.appendTag(DisplayElementHelper.saveStyledString(new NBTTagCompound(), ss, type));
		}
		if (!tagList.hasNoTags())
			nbt.setTag("ss", tagList);
		nbt.setInteger("wa", getAlign().ordinal());
		return nbt;
	}

	@Override
	public Iterator<IStyledString> iterator() {
		return strings.iterator();
	}
}
