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

public class StyledStringLine implements INBTSyncable {

	public List<StyledString> strings = Lists.newArrayList();
	private int cachedWidth = -1;
	private String cachedFormattedString;
	private String cachedUnformattedString;

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

	public int addText(int cursorIndex, String text) {
		if (cursorIndex == -1) {
			return cursorIndex;
		}
		int length = 0;
		for (StyledString ss : strings) {
			int index = cursorIndex - length;
			int sLength = ss.getStringLength();
			if (length + sLength > cursorIndex) {
				String s = ss.getUnformattedString();
				String before = (index != 0 ? s.substring(0, index) : "");
				String after = (index != s.length() ? s.substring(index) : "");
				ss.setUnformattedString(before + text + after);
				return cursorIndex + text.length();
			}else if(index == ss.getStringLength()){
				ss.setUnformattedString(ss.getUnformattedString() + text);
				return cursorIndex + text.length();
			}
			length += sLength;
		}
		return cursorIndex;
	}

	public int deleteText(int cursorIndex, int deleteSize) {
		return removeTextAtIndexs(cursorIndex, cursorIndex + deleteSize);
	}

	public int backspaceText(int cursorIndex, int backspaceSize) {
		return removeTextAtIndexs(cursorIndex - backspaceSize, cursorIndex);
	}

	public int removeTextAtIndexs(int start, int end) {
		start = Math.max(0, start);
		int currentWidth = 0;
		boolean started = false;
		boolean finished = false;
		for (StyledString ss : strings) {
			int ssStart = -1, ssEnd = -1;

			if (started) {
				ssStart = 0;
			} else if (currentWidth + ss.getStringLength() >= start) {
				started = true;
				ssStart = start - currentWidth;
			}
			if (started) {
				if (currentWidth + ss.getStringLength() > end) {
					ssEnd = end - currentWidth;
					finished = true;
				} else {
					ssEnd = ss.getStringLength();
				}
			}

			currentWidth += ss.getStringLength();

			if (ssStart >= 0 && ssEnd > ssStart) {
				String c = ss.getUnformattedString();
				String before = c.substring(0, ssStart);
				String after = ssEnd < c.length() - 1 ? c.substring(ssEnd, c.length()) : "";
				ss.setUnformattedString(before + after);
				return start;
			}
			if (finished) {
				break;
			}
		}
		return start;
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

	public void addWithCombine(List<StyledString> strings, StyledString ss) {
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
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTTagList tagList = new NBTTagList();
		for (StyledString ss : strings) {
			tagList.appendTag(ss.writeData(new NBTTagCompound(), type));
		}
		if (!tagList.hasNoTags())
			nbt.setTag("ss", tagList);
		return nbt;
	}
}
