package sonar.logistics.api.displays.elements;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;
import javax.xml.ws.Holder;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.core.api.IFlexibleGui;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.gui.textedit.GuiEditStyledStrings;
import sonar.logistics.client.gui.textedit.StyledString;
import sonar.logistics.client.gui.textedit.StyledStringLine;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;

@DisplayElementType(id = StyledTextElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class StyledTextElement extends AbstractDisplayElement implements IClickableElement, IFlexibleGui<TileAbstractDisplay>, Iterable<StyledStringLine> {

	private List<StyledStringLine> textLines = Lists.newArrayList();
	private int spacing = 0;

	public StyledTextElement() {}

	public StyledTextElement(String string) {
		this(Lists.newArrayList(string));
	}

	public StyledTextElement(List<String> strings) {
		strings.forEach(s -> textLines.add(new StyledStringLine(s)));
	}

	@Override
	public void render() {
		textLines.forEach(s -> {
			preRender(s);
			s.render();
			postRender(s);
		});
	}

	public List<StyledStringLine> getLines() {
		return textLines;
	}

	public List<StyledStringLine> setLines(List<StyledStringLine> textLines) {
		return this.textLines = textLines;
	}

	public void preRender(StyledStringLine c) {

	}

	public void deleteLine(int lineY) {
		textLines.remove(lineY);
	}

	public void addNewLine(int lineY, StyledStringLine line) {
		if (lineY >= textLines.size()) {
			textLines.add(line);
		} else {
			textLines.add(lineY, line);
		}
	}

	public void postRender(StyledStringLine c) {
		GL11.glTranslated(0, 9, 0);
	}

	@Override
	public List<InfoUUID> getInfoReferences() {
		return Lists.newArrayList(); // FIXME
	}

	@Override
	public String getRepresentiveString() {
		return "TEXT LIST";
	}

	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
		//Tuple<StyledStringLine, Integer> line = getLineClicked(new Holder(subClickX), new Holder(subClickY));
		//Tuple<StyledString, Integer> string = getStringClicked(new Holder(subClickX), new Holder(subClickY));
		//Tuple<Character, Integer> chara = getCharClicked(new Holder(subClickX), new Holder(subClickY));
		int[] index = getIndexClicked(new Holder(subClickX), new Holder(subClickY));
		//player.sendMessage(new TextComponentTranslation("Line: " + line.getFirst()));
		//player.sendMessage(new TextComponentTranslation("String: " + string.getFirst()));
		//player.sendMessage(new TextComponentTranslation("Char: " + chara.getFirst()));
		player.sendMessage(new TextComponentTranslation("Index: " + index==null? "null" : ("x: " + String.valueOf(index[0]) + " y: " + String.valueOf(index[1]))));
		return 0;
	}

	@Nullable
	public int[] getIndexClicked(double subClickX, double subClickY) {
		return getIndexClicked(new Holder(subClickX), new Holder(subClickY));
	}

	@Nullable
	private int[] getIndexClicked(Holder<Double> subClickX, Holder<Double> subClickY) {
		Tuple<StyledStringLine, Integer> line = getLineClicked(subClickX.value, subClickY.value);
		Tuple<StyledString, Integer> string = getStringClicked(subClickX.value, subClickY.value);
		if (string != null && string.getSecond() >= 0) {
			double rough_click = subClickX.value;
			Tuple<Character, Integer> character = getCharClicked(subClickX.value, subClickY.value);
			if (character.getSecond() != -1) {
				int i = 0;
				int index = 0;
				for (StyledString s : line.getFirst()) {
					if (i == string.getSecond()) {
						break;
					}
					index += s.getStringLength();
					i++;
				}
				double actual_click = subClickX.value / this.getActualScaling()[SCALE];
				int charWidth = RenderHelper.fontRenderer.getStringWidth(string.getFirst().getTextFormattingStyle() + character.getFirst());
				//if (rough_click - charWidth / 2 > actual_click) {
				//	index++;
				//}
				return new int[] { Math.min(index + character.getSecond(), line.getFirst().getCachedUnformattedString().length()), line.getSecond() };
			}
		}
		return null;
	}

	public Tuple<StyledStringLine, Integer> getLineClicked(double subClickX, double subClickY) {
		return getLineClicked(new Holder(subClickX), new Holder(subClickY));
	}

	private Tuple<StyledStringLine, Integer> getLineClicked(Holder<Double> subClickX, Holder<Double> subClickY) {
		double y = 0;
		int i = 0;
		for (StyledStringLine c : this) {
			double height = c.getStringHeight() * getActualScaling()[SCALE];
			if (y <= subClickY.value && y + height >= subClickY.value) {
				subClickY.value = y;
				return new Tuple(c, i);
			}
			y += height + (spacing * getActualScaling()[SCALE]);
			i++;
		}
		return new Tuple(null, -1);
	}

	public Tuple<StyledString, Integer> getStringClicked(double subClickX, double subClickY) {
		return getStringClicked(new Holder(subClickX), new Holder(subClickY));
	}

	private Tuple<StyledString, Integer> getStringClicked(Holder<Double> subClickX, Holder<Double> subClickY) {
		Tuple<StyledStringLine, Integer> line = getLineClicked(subClickX, subClickY);
		if (line.getSecond() != -1) {
			return getStringClicked(line, subClickX, subClickY);
		}
		return new Tuple(null, -1);
	}

	private Tuple<StyledString, Integer> getStringClicked(Tuple<StyledStringLine, Integer> line, Holder<Double> subClickX, Holder<Double> subClickY) {
		double x = 0;
		int i = 0;
		for (StyledString string : line.getFirst()) {
			double width = string.getStringWidth() * getActualScaling()[SCALE];
			if (x <= subClickX.value && x + width >= subClickX.value) {
				subClickX.value -= x;
				return new Tuple(string, i);
			}
			x += width;
			i++;
		}
		return new Tuple(null, -1);
	}

	public Tuple<Character, Integer> getCharClicked(double subClickX, double subClickY) {
		return getCharClicked(new Holder(subClickX), new Holder(subClickY));
	}

	private Tuple<Character, Integer> getCharClicked(Holder<Double> subClickX, Holder<Double> subClickY) {
		Tuple<StyledString, Integer> line = getStringClicked(subClickX, subClickY);
		if (line.getSecond() != -1) {
			return getCharClicked(line, subClickX, subClickY);
		}
		return new Tuple(null, -1);
	}

	private Tuple<Character, Integer> getCharClicked(Tuple<StyledString, Integer> string, Holder<Double> subClickX, Holder<Double> subClickY) {
		String unformatted = string.getFirst().getUnformattedString();
		String formatting = string.getFirst().getTextFormattingStyle();
		int length = unformatted.length();
		double x = 0;
		for (int i = 0; i < length; i++) {
			String charString = formatting + unformatted.charAt(i);
			int charStringWidth = RenderHelper.fontRenderer.getStringWidth(charString);

			double width = charStringWidth * getActualScaling()[SCALE];
			if (x <= subClickX.value && x + width >= subClickX.value) {
				subClickX.value = x;
				return new Tuple(unformatted.charAt(i), i);
			}
			x += width;
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
		List<StyledStringLine> newLines = Lists.newArrayList();
		NBTTagList tagList = nbt.getTagList("ssc", NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound ssTag = tagList.getCompoundTagAt(i);
			StyledStringLine ss = new StyledStringLine();
			ss.readData(ssTag, type);
			newLines.add(ss);
		}
		textLines = newLines;
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		NBTTagList tagList = new NBTTagList();
		for (StyledStringLine ss : textLines) {
			tagList.appendTag(ss.writeData(new NBTTagCompound(), type));
		}
		if (!tagList.hasNoTags()) {
			nbt.setTag("ssc", tagList);
		}
		return nbt;
	}

	@Override
	public Object getServerElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return new ContainerMultipartSync(obj);
	}

	@Override
	public Object getClientElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return new GuiEditStyledStrings(this, this.getHolder().getContainer());
	}

	public int getWidth() {
		int width = 0;
		for (StyledStringLine ss : this) {
			int w = ss.getStringWidth();
			if (w > width) {
				width = w;
			}
		}
		return width;
	}

	public int getHeight() {
		int height = 0;
		for (StyledStringLine ss : this) {
			int h = ss.getStringHeight() + spacing;
			height += h;
		}
		return height;
	}

	public int[] createUnscaledWidthHeight() {
		return new int[] { getWidth(), getHeight() };
	}

	public static final String REGISTRY_NAME = "styled_text";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

}
