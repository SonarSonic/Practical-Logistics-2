package sonar.logistics.api.displays.elements.text;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import sonar.core.client.gui.IGuiOrigin;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.IDisplayAction;
import sonar.logistics.api.displays.elements.text.StyledStringRenderer.SimpleIndex;
import sonar.logistics.api.displays.elements.text.StyledStringRenderer.StyledStringRenderHandler;
import sonar.logistics.api.displays.tiles.DisplayScreenClick;
import sonar.logistics.client.gui.textedit.GuiEditWrappedStyledString;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;

import javax.annotation.Nullable;
import javax.xml.ws.Holder;
import java.util.List;

@DisplayElementType(id = StyledWrappedTextElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class StyledWrappedTextElement extends StyledTextElement {

	public int pageCount = 0;	
	public StyledStringRenderHandler handler = new StyledStringRenderHandler(this);

	public StyledWrappedTextElement() {}

	public StyledWrappedTextElement(String string) {
		super(string);
	}

	public StyledWrappedTextElement(List<String> strings) {
		super(strings);
	}

	@Override
	public void render() {
		handler.wrapLines(getActualScaling()[0] * (textScale * 100), getActualScaling()[1] * (textScale * 100));
		
		boolean needsPages = handler.linesPerPage < handler.lines.size();
		if (needsPages && handler.linesPerPage != 0) {
			handler.linesPerPage = (int) Math.floor((((getActualScaling()[1] - getActualScaling()[1] / 8) * (textScale * 100)) / handler.totalLineSize()));
		}
		int totalPages = (int) (Math.ceil((double) handler.lines.size() / (double) handler.linesPerPage));
		if (pageCount >= totalPages) {
			pageCount = totalPages - 1;
		}

		GlStateManager.scale(textScale / 100D, textScale / 100D, 1D);
		StyledStringRenderer.instance().renderWrappedText(handler, getActualScaling()[0] * (textScale * 100), (needsPages ? (getActualScaling()[1] - getActualScaling()[1] / 8) : getActualScaling()[1]) * (textScale * 100), pageCount*handler.linesPerPage, (pageCount*handler.linesPerPage) +handler.linesPerPage);
		GlStateManager.scale(1D / (textScale / 100D), 1D / (textScale / 100D), 1D / 1D);
		if(needsPages && handler.linesPerPage != 0){			
			DisplayElementHelper.renderPageButons(getActualScaling(), this.pageCount + 1, totalPages);
		}
	}

	@Override
	public Object getClientEditGui(TileAbstractDisplay obj, Object origin, World world, EntityPlayer player) {
		return IGuiOrigin.withOrigin(new GuiEditWrappedStyledString(this, obj), origin);
	}

	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
		Tuple<SimpleIndex, Integer> string = getStringClicked(subClickX, subClickY);
		if(string !=null && string.getFirst()!=null) {
			IDisplayAction action = getAction(string.getFirst().string.getStyle().action_id);
			if (action != null) {
				return action.doAction(click, player, subClickX, subClickY);
			}
		}
        boolean needsPages = handler.linesPerPage < handler.lines.size();
		if (needsPages) {
			int totalPages = (int) (Math.ceil((double) handler.lines.size() / (double) handler.linesPerPage));
			pageCount = DisplayElementHelper.doPageClick(subClickX, subClickY, getActualScaling(), pageCount, totalPages);
		}
		return -1;
	}

	@Nullable
	public int[] getIndexClicked(double subClickX, double subClickY) {
		return getIndexClicked(new Holder(subClickX), new Holder(subClickY));
	}

	@Nullable
	private int[] getIndexClicked(Holder<Double> subClickX, Holder<Double> subClickY) {
		Tuple<List<SimpleIndex>, Integer> line = getLineClicked(subClickX.value, subClickY.value);
		Tuple<SimpleIndex, Integer> string = getStringClicked(subClickX.value, subClickY.value);
		if (string != null && isValidReturn(string.getSecond())) {
			double rough_click = subClickX.value;
			Tuple<Character, Integer> character = getCharClicked(subClickX.value, subClickY.value);
			if (isValidReturn(character.getSecond())) {
				int i = 0;
				int index = 0;
				for (SimpleIndex s : line.getFirst()) {
					if (i == string.getSecond()) {
						break;
					}
					index += s.end - s.start;
					i++;
				}
				int charWidth = StyledStringRenderer.instance().getCharRenderWidthFromStyledString(string.getFirst().string, character.getFirst());

				int total = 0;
				for (SimpleIndex s : line.getFirst()) {
					total += s.end - s.start;
				}

				return new int[] { Math.min(index + character.getSecond(), total), line.getSecond() };
			}
		}
		return null;
	}

	public Tuple<List<SimpleIndex>, Integer> getLineClicked(double subClickX, double subClickY) {
		return getLineClicked(new Holder(subClickX), new Holder(subClickY));
	}

	private Tuple<List<SimpleIndex>, Integer> getLineClicked(Holder<Double> subClickX, Holder<Double> subClickY) {
		double y = 0;
		int i = 0;
		double scale = textScale / 100D;
		double max_width = getMaxScaling()[WIDTH];
		for (List<SimpleIndex> list : this.handler.lines.values()) {
			double height = StyledStringRenderer.instance().FONT_HEIGHT * scale;
			if (y <= subClickY.value && y + height >= subClickY.value) {
				subClickY.value = y;
				double element = StyledStringRenderer.instance().FONT_HEIGHT * scale;

				/* if (c.getAlign() == WidthAlignment.CENTERED) { subClickX.value -= (max_width / 2) - (element / 2); } else if (c.getAlign() == WidthAlignment.RIGHT) subClickX.value -= max_width - element; */
				return new Tuple(list, i);
			}
			y += height + (spacing * scale);
			i++;
		}
		return new Tuple(null, -1);
	}

	public Tuple<SimpleIndex, Integer> getStringClicked(double subClickX, double subClickY) {
		return getStringClicked(new Holder(subClickX), new Holder(subClickY));
	}

	private Tuple<SimpleIndex, Integer> getStringClicked(Holder<Double> subClickX, Holder<Double> subClickY) {
		Tuple<List<SimpleIndex>, Integer> line = getLineClicked(subClickX, subClickY);
		if (isValidReturn(line.getSecond())) {
			return getStringClicked(line, subClickX, subClickY);
		}
		return new Tuple(null, line.getSecond());
	}

	private Tuple<SimpleIndex, Integer> getStringClicked(Tuple<List<SimpleIndex>, Integer> line, Holder<Double> subClickX, Holder<Double> subClickY) {
		double x = 0;
		int i = 0;
		double scale = textScale / 100D;
		for (SimpleIndex index : line.getFirst()) {
			int stringWidth = StyledStringRenderer.instance().getRenderStringWidthWithStyledString(index.string, index.string.getUnformattedString().substring(index.start, index.end));

			double width = stringWidth * scale;
			if (x <= subClickX.value && x + width >= subClickX.value) {
				subClickX.value -= x;
				return new Tuple(index, i);
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
		Tuple<SimpleIndex, Integer> line = getStringClicked(subClickX, subClickY);
		if (isValidReturn(line.getSecond())) {

			String unformatted = line.getFirst().string.getUnformattedString().substring(line.getFirst().start, line.getFirst().end);
			int length = unformatted.length();
			double x = 0;
			double scale = textScale / 100D;
			for (int i = 0; i < length; i++) {
				char charString = unformatted.charAt(i);
				int charStringWidth = StyledStringRenderer.instance().getCharWidthFromStyledString(line.getFirst().string, charString);
				double width = charStringWidth * scale;
				if (x <= subClickX.value && x + width >= subClickX.value) {
					subClickX.value = x;
					return new Tuple(unformatted.charAt(i), i);
				} else if (i == 0 && subClickX.value < x) {
					return new Tuple(null, -2);
				}
				x += width;
			}

		}

		return new Tuple(null, -1);
	}

	public static final String REGISTRY_NAME = "wrapped_text";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}
}
