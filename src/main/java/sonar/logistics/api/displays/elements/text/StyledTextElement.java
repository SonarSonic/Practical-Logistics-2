package sonar.logistics.api.displays.elements.text;

import static net.minecraft.client.renderer.GlStateManager.translate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.xml.ws.Holder;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.IDisplayAction;
import sonar.logistics.api.displays.WidthAlignment;
import sonar.logistics.api.displays.elements.AbstractDisplayElement;
import sonar.logistics.api.displays.elements.ElementFillType;
import sonar.logistics.api.displays.elements.IClickableElement;
import sonar.logistics.api.displays.elements.ISpecialAlignment;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.gui.textedit.GuiEditStyledStrings;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;

@DisplayElementType(id = StyledTextElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class StyledTextElement extends AbstractDisplayElement implements IClickableElement, Iterable<StyledStringLine>, ISpecialAlignment {

	private Map<Integer, IDisplayAction> actions = Maps.newHashMap();
	private List<StyledStringLine> textLines = new ArrayList<>();
	private List<InfoUUID> uuids;
	public int spacing = 0;
	public int action_id_count = 0;
	public boolean updateTextScaling = true, updateTextContents = true;

	public StyledTextElement() {}

	public StyledTextElement(String string) {
		this(Lists.newArrayList(string));
	}

	public StyledTextElement(List<String> strings) {
		strings.forEach(s -> addLine(s));
	}

	@Override
	public void updateRender() {
		super.updateRender();
		if (updateTextContents) {
			if (uuids != null && !uuids.isEmpty()) {
				// this.getGSI().removeInfoReferences(uuids);
			}
			this.uuids = null;
			this.cachedWidth = -1;
			this.cachedHeight = -1;
			/// getGSI().addInfoReferences(getInfoReferences());
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
		updateTextContents();
	}

	public int addAction(IDisplayAction action) {
		int id = action_id_count + 1;
		actions.put(id, action);
		return id;
	}

	public @Nullable IDisplayAction getAction(int action_id) {
		return actions.get(action_id);
	}

	@Override
	public void render() {
		double[] scaling = DisplayElementHelper.getScaling(this.getUnscaledWidthHeight(), this.getMaxScaling(), 100);
		double max_width = getMaxScaling()[WIDTH];

		GlStateManager.disableLighting();
		for (StyledStringLine s : this) {
			preRender(s);
			GlStateManager.pushMatrix();
			double element = s.getStringWidth() * scaling[2];
			if (s.getAlign() == WidthAlignment.CENTERED)
				translate((max_width / 2) - (element / 2), 0, 0);
			if (s.getAlign() == WidthAlignment.RIGHT)
				translate(max_width - element, 0, 0);
			GlStateManager.scale(scaling[2], scaling[2], 1);
			s.render();
			GlStateManager.scale(1 / scaling[2], 1 / scaling[2], 1);
			GlStateManager.popMatrix();
			GL11.glTranslated(0, (s.getStringHeight() + spacing) * scaling[2], 0);
			postRender(s);
		}
		GlStateManager.disableLighting();
	}

	@Override
	public Object getClientEditGui(TileAbstractDisplay obj, Object origin, World world, EntityPlayer player) {
		return GuiSonar.withOrigin(new GuiEditStyledStrings(this, obj), origin);
	}

	public List<StyledStringLine> getLines() {
		return textLines;
	}

	public List<StyledStringLine> setLines(List<StyledStringLine> textLines) {
		textLines.forEach(ss -> ss.setText(this));
		updateTextContents();
		return this.textLines = textLines;
	}

	public void addLine(String line) {
		addLine(new StyledStringLine(this, line));
	}

	public void addLine(StyledStringLine line) {
		line.setText(this);
		textLines.add(line);
		updateTextContents();
	}

	public void setLine(int i, StyledStringLine line) {
		line.setText(this);
		textLines.set(i, line);
		updateTextContents();
	}

	public void preRender(StyledStringLine c) {}

	public void deleteLine(int lineY) {
		textLines.remove(lineY);
		updateTextContents();
	}

	public void addNewLine(int lineY, StyledStringLine line) {
		line.setText(this);
		if (lineY >= textLines.size()) {
			textLines.add(line);
		} else {
			textLines.add(lineY, line);
		}
		updateTextContents();
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

	public void updateTextContents() {
		this.updateTextContents = true;
	}

	public void updateTextScaling() {
		this.updateTextScaling = true;
	}

	@Override
	public String getRepresentiveString() {
		return "Styled Text";
	}

	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
		Tuple<IStyledString, Integer> string = getStringClicked(subClickX, subClickY);
		if (string.getFirst() != null) {
			IDisplayAction action = getAction(string.getFirst().getStyle().action_id);
			if (action != null) {
				return action.doAction(click, player, subClickX, subClickY);
			}
		}

		int[] index = getIndexClicked(new Holder(subClickX), new Holder(subClickY));
		if (index != null) {
			player.sendMessage(new TextComponentTranslation("Index: " + "x: " + index[0] + " y: " + index[1]));
		} else {
			player.sendMessage(new TextComponentTranslation("Index: " + "null"));
		}
		return -1;
	}

	@Nullable
	public int[] getIndexClicked(double subClickX, double subClickY) {
		return getIndexClicked(new Holder(subClickX), new Holder(subClickY));
	}

	public static final int INVALID = -1, AFTER = -2, BEFORE = -3;

	public static final boolean isValidReturn(int i_return) {
		return i_return != INVALID && i_return != AFTER && i_return != BEFORE;
	}

	@Nullable
	private int[] getIndexClicked(Holder<Double> subClickX, Holder<Double> subClickY) {
		double[] scaling = DisplayElementHelper.getScaling(this.getUnscaledWidthHeight(), this.getMaxScaling(), 100);
		Tuple<StyledStringLine, Integer> line = getLineClicked(subClickX.value, subClickY.value);
		Tuple<IStyledString, Integer> string = getStringClicked(subClickX.value, subClickY.value);
		if (string != null && isValidReturn(string.getSecond())) {
			double rough_click = subClickX.value;
			Tuple<Character, Integer> character = getCharClicked(subClickX.value, subClickY.value);
			if (isValidReturn(character.getSecond())) {
				int i = 0;
				int index = 0;
				for (IStyledString s : line.getFirst()) {
					if (i == string.getSecond()) {
						break;
					}
					index += s.getStringLength();
					i++;
				}
				double actual_click = subClickX.value / scaling[SCALE];
				int charWidth = RenderHelper.fontRenderer.getStringWidth(string.getFirst().getTextFormattingStyle() + character.getFirst());
				// if (rough_click - charWidth / 2 > actual_click) {
				// index++;
				// }
				return new int[] { Math.min(index + character.getSecond(), line.getFirst().getCachedUnformattedString().length()), line.getSecond() };
			}
		}
		return null;
	}

	public Tuple<StyledStringLine, Integer> getLineClicked(double subClickX, double subClickY) {
		return getLineClicked(new Holder(subClickX), new Holder(subClickY));
	}

	private Tuple<StyledStringLine, Integer> getLineClicked(Holder<Double> subClickX, Holder<Double> subClickY) {
		double[] scaling = DisplayElementHelper.getScaling(this.getUnscaledWidthHeight(), this.getMaxScaling(), 100);
		double max_width = getMaxScaling()[WIDTH];
		double y = 0;
		int i = 0;

		for (StyledStringLine c : this) {
			double height = c.getStringHeight() * scaling[SCALE];
			if (y <= subClickY.value && y + height >= subClickY.value) {
				subClickY.value = y;
				double element = c.getStringWidth() * scaling[2];

				if (c.getAlign() == WidthAlignment.CENTERED) {
					subClickX.value -= (max_width / 2) - (element / 2);
				} else if (c.getAlign() == WidthAlignment.RIGHT)
					subClickX.value -= max_width - element;

				return new Tuple(c, i);
			}
			y += height + (spacing * scaling[SCALE]);
			i++;
		}
		return new Tuple(null, -1);
	}

	public Tuple<IStyledString, Integer> getStringClicked(double subClickX, double subClickY) {
		return getStringClicked(new Holder(subClickX), new Holder(subClickY));
	}

	private Tuple<IStyledString, Integer> getStringClicked(Holder<Double> subClickX, Holder<Double> subClickY) {
		Tuple<StyledStringLine, Integer> line = getLineClicked(subClickX, subClickY);
		if (isValidReturn(line.getSecond())) {
			return getStringClicked(line, subClickX, subClickY);
		}
		return new Tuple(null, line.getSecond());
	}

	private Tuple<IStyledString, Integer> getStringClicked(Tuple<StyledStringLine, Integer> line, Holder<Double> subClickX, Holder<Double> subClickY) {
		double[] scaling = DisplayElementHelper.getScaling(this.getUnscaledWidthHeight(), this.getMaxScaling(), 100);
		double x = 0;
		int i = 0;
		for (IStyledString string : line.getFirst()) {
			double width = string.getStringWidth() * scaling[SCALE];
			if (x <= subClickX.value && x + width >= subClickX.value) {
				subClickX.value -= x;
				return new Tuple(string, i);
			} else if (i == 0 && subClickX.value < x) {
				return new Tuple(null, BEFORE);
			}

			x += width;
			i++;
		}
		return new Tuple(null, AFTER);
	}

	public Tuple<Character, Integer> getCharClicked(double subClickX, double subClickY) {
		return getCharClicked(new Holder(subClickX), new Holder(subClickY));
	}

	private Tuple<Character, Integer> getCharClicked(Holder<Double> subClickX, Holder<Double> subClickY) {
		Tuple<IStyledString, Integer> line = getStringClicked(subClickX, subClickY);
		if (isValidReturn(line.getSecond())) {
			return line.getFirst().getCharClicked(line.getSecond(), subClickX, subClickY);
		}
		return new Tuple(null, -1);
	}

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

	private int cachedWidth = -1;

	public int getWidth() {
		if (cachedWidth == -1) {
			int width = 0;
			for (StyledStringLine ss : this) {
				int w = ss.getStringWidth();
				if (w > width) {
					width = w;
				}
			}
			cachedWidth = width;
		}
		return cachedWidth;
	}

	private int cachedHeight = -1;

	public int getHeight() {
		if (cachedHeight == -1) {
			int height = 0;
			for (StyledStringLine ss : this) {
				int h = ss.getStringHeight() + spacing;
				height += h;
			}
			cachedHeight = height;
		}
		return cachedHeight;
	}

	public int[] createUnscaledWidthHeight() {
		return new int[] { getWidth(), getHeight() };
	}

	@Override
	public ElementFillType getFillType() {
		return ElementFillType.FILL_SCALED_CONTAINER;
	}

	@Override
	public double[] getAlignmentTranslation() {
		double[] scaling = DisplayElementHelper.getScaling(this.getUnscaledWidthHeight(), this.getMaxScaling(), 100);
		double[] align_array = DisplayElementHelper.alignArray(this.getHolder().getContainer().getContainerMaxScaling(), this.getActualScaling(), this.getWidthAlignment(), this.getHeightAlignment());

		double maxHeight = getMaxScaling()[HEIGHT];
		double height = this.getHeight() * scaling[2];
		switch (this.height_align) {
		case CENTERED:
			align_array[1] += (maxHeight / 2) - (height / 2);
			break;
		case TOP:
			break;
		case BOTTOM:
			align_array[1] += maxHeight - height;
			break;
		}

		return align_array;
	}

	public static final String REGISTRY_NAME = "styled_text";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

}
