package sonar.logistics.api.displays.elements.text;

import javax.xml.ws.Holder;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.StyledStringType;
import sonar.logistics.helpers.DisplayElementHelper;

@StyledStringType(id = StyledString.REGISTRY_NAME, modid = PL2Constants.MODID)
public class StyledString implements IStyledString, INBTSyncable {

	public String string;
	private SonarStyling style;

	private String formattingString;
	private StyledStringLine line;

	public StyledString() {}

	public StyledString(String string) {
		this(string, new SonarStyling());
	}

	public StyledString(String string, SonarStyling style) {
		this.string = string;
		this.style = style;
	}

	public IStyledString setLine(StyledStringLine line) {
		this.line = line;
		return this;
	}

	public StyledStringLine getLine() {
		return line;
	}

	public String setUnformattedString(String s) {
		string = s;
		updateTextContents();
		return getUnformattedString();
	}

	public String getUnformattedString() {
		return string;
	}

	public String getTextFormattingStyle() {
		return style.getTextFormattingString();
	}

	private String cachedFormattedString = null;

	@Override
	public String getFormattedString() {
		if (cachedFormattedString == null) {
			cachedFormattedString = getTextFormattingStyle() + getUnformattedString();
		}
		return cachedFormattedString;
	}

	@Override
	public SonarStyling setStyle(SonarStyling f) {
		style = f;
		onStyleChanged();
		return getStyle();
	}

	@Override
	public SonarStyling getStyle() {
		return style;
	}

	@Override
	public String toString() {
		return getFormattedString();
	}

	@Override
	public void onStyleChanged() {
		updateTextContents();
	}

	public void updateTextContents() {
		this.cachedWidth = -1;
		this.cachedFormattedString = null;
		if (this.getLine() != null) {
			this.getLine().updateTextContents();
		}
	}

	public void updateTextScaling() {
		if (this.getLine() != null) {
			this.getLine().updateTextScaling();
		}
	}

	@Override
	public int getStringLength() {
		return getUnformattedString().length();
	}

	private int cachedWidth = -1;

	@Override
	public int getStringWidth() {
		if (cachedWidth == -1) {
			cachedWidth = RenderHelper.fontRenderer.getStringWidth(getFormattedString());
		}
		return cachedWidth;
	}

	@Override
	public IStyledString copy() {
		return new StyledString(string, style.copy());
	}

	@Override
	public Tuple<Character, Integer> getCharClicked(int yPos, Holder<Double> subClickX, Holder<Double> subClickY) {
		double[] scaling = DisplayElementHelper.getScaling(getText().getUnscaledWidthHeight(), getText().getMaxScaling(), 100);
		String unformatted = getUnformattedString();
		String formatting = getTextFormattingStyle();
		int length = unformatted.length();
		double x = 0;
		for (int i = 0; i < length; i++) {
			String charString = formatting + unformatted.charAt(i);
			int charStringWidth = RenderHelper.fontRenderer.getStringWidth(charString);

			double width = charStringWidth * scaling[2];
			if (x <= subClickX.value && x + width >= subClickX.value) {
				subClickX.value = x;
				return new Tuple(unformatted.charAt(i), i);
			} else if (i == 0 && subClickX.value < x) {
				return new Tuple(null, -2);
			}
			x += width;
		}
		return new Tuple(null, -1);
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		string = nbt.getString("s");
		(style = new SonarStyling()).readData(nbt, type);
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		nbt.setString("s", string);
		style.writeData(nbt, type);
		return nbt;
	}

	@Override
	public boolean canCombine(IStyledString ss) {
		return ss instanceof StyledString && ss.getStyle().matching(style);
	}

	public void combine(IStyledString ss) {
		int previousLength = getStringLength();
		this.setUnformattedString(this.getUnformattedString() + ss.getUnformattedString());
	}

	public static final String REGISTRY_NAME = "s_s";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}
}
