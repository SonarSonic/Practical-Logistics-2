package sonar.logistics.api.displays.elements.text;

import static net.minecraft.client.renderer.GlStateManager.translate;

import java.util.List;

import javax.annotation.Nullable;
import javax.xml.ws.Holder;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import sonar.core.client.gui.IGuiOrigin;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.IDisplayAction;
import sonar.logistics.api.displays.WidthAlignment;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.gui.textedit.GuiEditTitleStyledString;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;

@DisplayElementType(id = StyledTitleElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class StyledTitleElement extends StyledTextElement{

	public StyledTitleElement() {}

	public StyledTitleElement(String string) {
		super(string);
	}

	public StyledTitleElement(List<String> strings) {
		super(strings);
	}
	
	@Override
	public void updateRender() {
		super.updateRender();
		if(updateTextContents){
			this.cachedWidth = -1;
			this.cachedHeight = -1;
		}
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
			s.render(render_style);
			GlStateManager.scale(1 / scaling[2], 1 / scaling[2], 1);
			GlStateManager.popMatrix();
			GL11.glTranslated(0, (s.getStringHeight() + spacing) * scaling[2], 0);
			postRender(s);
		}
		GlStateManager.disableLighting();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public Object getClientEditGui(TileAbstractDisplay obj, Object origin, World world, EntityPlayer player) {
		return IGuiOrigin.withOrigin(new GuiEditTitleStyledString(this, obj), origin);
	}

	@Override
	public double[] getAlignmentTranslation(double[] maxScaling, double[] actualScaling) {
		double[] scaling = DisplayElementHelper.getScaling(this.getUnscaledWidthHeight(), getMaxScaling(), 100);
		double[] align_array = DisplayElementHelper.alignArray(maxScaling, this.getActualScaling(), this.getWidthAlignment(), this.getHeightAlignment());

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
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
		Tuple<IStyledString, Integer> string = getStringClicked(subClickX, subClickY);
        string.getFirst();
        IDisplayAction action = getAction(string.getFirst().getStyle().action_id);
        if (action != null) {
            return action.doAction(click, player, subClickX, subClickY);
        }
        return -1;
	}

	@Nullable
	public int[] getIndexClicked(double subClickX, double subClickY) {
		return getIndexClicked(new Holder(subClickX), new Holder(subClickY));
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

	public static final String REGISTRY_NAME = "styled_title";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

}
