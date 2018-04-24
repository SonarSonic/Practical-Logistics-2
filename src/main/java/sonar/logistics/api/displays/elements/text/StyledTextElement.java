package sonar.logistics.api.displays.elements.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.api.displays.IDisplayAction;
import sonar.logistics.api.displays.elements.AbstractDisplayElement;
import sonar.logistics.api.displays.elements.ElementFillType;
import sonar.logistics.api.displays.elements.IClickableElement;
import sonar.logistics.api.displays.elements.IInfoReferenceElement;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.helpers.DisplayElementHelper;

public abstract class StyledTextElement extends AbstractDisplayElement implements IClickableElement, Iterable<StyledStringLine>, IInfoReferenceElement {

	private Map<Integer, IDisplayAction> actions = Maps.newHashMap();
	private List<StyledStringLine> textLines = new ArrayList<>();
	private List<InfoUUID> uuids;
	public int spacing = 0;
	public int action_id_count = 0;
	public boolean updateTextScaling = true, updateTextContents = true;
	public double textScale = 1;
	public StyledTextRenderStyle render_style = StyledTextRenderStyle.WRAPPED;

	public StyledTextElement() {}

	public StyledTextElement(String string) {
		this(Lists.newArrayList(string));
	}

	public StyledTextElement(List<String> strings) {
		strings.forEach(this::addLine);
	}

	@Override
	public void updateRender() {
		super.updateRender();
		if (updateTextContents) {
			this.uuids = null;
			unscaledWidthHeight = null;
			maxScaling = null;
			actualScaling = null;
			updateTextContents = false;
			getGSI().updateInfoReferences();
		} else if (updateTextScaling) {
			updateTextScaling = false;
		}
	}

	@Override
	public void onElementChanged() {
		markTextContentsChanged();
	}

	public int addAction(IDisplayAction action) {
		int id = action_id_count + 1;
		actions.put(id, action);
		return id;
	}

	public @Nullable IDisplayAction getAction(int action_id) {
		return actions.get(action_id);
	}

	public List<StyledStringLine> getLines() {
		return textLines;
	}

	public List<StyledStringLine> setLines(List<StyledStringLine> textLines) {
		textLines.forEach(ss -> ss.setText(this));
		markTextContentsChanged();
		return this.textLines = textLines;
	}

	public void addLine(String line) {
		addLine(new StyledStringLine(this, line));
	}

	public void addLine(StyledStringLine line) {
		line.setText(this);
		textLines.add(line);
		markTextContentsChanged();
	}

	public void setLine(int i, StyledStringLine line) {
		line.setText(this);
		textLines.set(i, line);
		markTextContentsChanged();
	}

	public void preRender(StyledStringLine c) {}

	public void deleteLine(int lineY) {
		textLines.remove(lineY);
		markTextContentsChanged();
	}

	public void addNewLine(int lineY, StyledStringLine line) {
		line.setText(this);
		if (lineY >= textLines.size()) {
			textLines.add(line);
		} else {
			textLines.add(lineY, line);
		}
		markTextContentsChanged();
	}

	public void postRender(StyledStringLine c) {}

	@Override
	public List<InfoUUID> getInfoReferences() {
		if (uuids == null) {
			List<InfoUUID> references = new ArrayList<>();
			forEach(line -> line.forEach(s -> ListHelper.addWithCheck(references, s.getInfoReferences())));
			uuids = references;
		}
		return uuids;
	}

	public void onInfoReferenceChanged(InfoUUID uuid, IInfo info) {
		forEach(line -> line.forEach(s -> {
			if (s.getInfoReferences().contains(uuid)) {
				s.updateTextContents();
			}
		}));
	}

	public void markTextContentsChanged() {
		this.updateTextContents = true;
	}

	public void markTextScalingChanged() {
		this.updateTextScaling = true;
	}

	@Override
	public String getRepresentiveString() {
		return "Styled Text";
	}

	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
		return -1;
	}

	public static final int INVALID = -1, AFTER = -2, BEFORE = -3;

	public static boolean isValidReturn(int i_return) {
		return i_return != INVALID && i_return != AFTER && i_return != BEFORE;
	}

	@Nonnull
	@Override
	public Iterator<StyledStringLine> iterator() {
		return textLines.iterator();
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		List<StyledStringLine> newLines = new ArrayList<>();
		NBTTagList tagList = nbt.getTagList("ssc", NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound ssTag = tagList.getCompoundTagAt(i);
			StyledStringLine ss = new StyledStringLine(this);
			ss.readData(ssTag, type);
			newLines.add(ss);
		}
		textLines = newLines;

		NBTTagList actionList = nbt.getTagList("actions", NBT.TAG_COMPOUND);
		for (int i = 0; i < actionList.tagCount(); i++) {
			NBTTagCompound actionTag = actionList.getCompoundTagAt(i);
			int saved_id = actionTag.getInteger("saved_id");
			IDisplayAction action = DisplayElementHelper.loadDisplayAction(actionTag);
			actions.put(saved_id, action);
		}

		action_id_count = nbt.getInteger("action_id");
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		List<Integer> actions_to_save = new ArrayList<>();
		NBTTagList tagList = new NBTTagList();
		for (StyledStringLine ss : textLines) {
			tagList.appendTag(ss.writeData(new NBTTagCompound(), type));
			ListHelper.addWithCheck(actions_to_save, ss.getContainedActions());
		}
		if (!tagList.hasNoTags()) {
			nbt.setTag("ssc", tagList);
		}
		NBTTagList actionList = new NBTTagList();
		for (Integer i : actions_to_save) {
			if (i != -1) {
				IDisplayAction action = actions.get(i);
				if (action != null) {
					NBTTagCompound actionTag = new NBTTagCompound();
					DisplayElementHelper.saveDisplayAction(actionTag, action, type);
					actionTag.setInteger("saved_id", i);
					actionList.appendTag(actionTag);
				}
			}
		}
		if (!actionList.hasNoTags()) {
			nbt.setTag("actions", actionList);
		}

		nbt.setInteger("action_id", action_id_count);
		return nbt;
	}

	public int[] createUnscaledWidthHeight() {
		return new int[] { 1, 1 };
	}

	@Override
	public ElementFillType getFillType() {
		return ElementFillType.FILL_SCALED_CONTAINER;
	}

	public int getLineCount() {
		return getLines().size();
	}

	public int getLineIndex(StyledStringLine toFind) {
		int i = 0;
		for (StyledStringLine line : this) {
			if(line==toFind){
				return i;
			}
			i++;
		}
		return -1;
	}

	public int getLineLength(int line) {
		if (line >= getLineCount() || line < 0) {
			return 0;
		}
		return getUnformattedLine(line).length();
	}

	public int getLineWidth(int line) {
		return RenderHelper.fontRenderer.getStringWidth(getFormattedLine(line));
	}

	public String getUnformattedLine(int line) {
		StyledStringLine ss = getLine(line);
		return ss == null ? "" : ss.getCachedUnformattedString();
	}

	public String getFormattedLine(int line) {
		StyledStringLine ss = getLine(line);
		return ss == null ? "" : ss.getCachedFormattedString();
	}

	public StyledStringLine getLine(int line) {
		if (line == -1 || line >= getLineCount()) {
			return null;
		}
		return getLines().get(line);
	}

}
