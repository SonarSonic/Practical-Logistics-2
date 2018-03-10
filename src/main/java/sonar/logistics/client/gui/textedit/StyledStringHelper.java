package sonar.logistics.client.gui.textedit;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import net.minecraftforge.fml.client.config.GuiUtils;
import sonar.core.utils.CustomColour;
import sonar.logistics.api.displays.elements.types.StyledTextElement;

public class StyledStringHelper {

	public static String[] getSubStrings(int subStart, int subEnd, String s) {
		String before_string = subStart == 0 ? "" : s.substring(0, subStart);
		String format_string = s.substring(subStart, subEnd);
		String after_string = subEnd == s.length() ? "" : s.substring(subEnd, s.length());
		return new String[] { before_string, format_string, after_string };
	}

	public static void addWithCombine(List<IStyledString> strings, IStyledString ss) {
		if (ss == null || ss.getUnformattedString().isEmpty()) {
			return; // deletion
		}
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

	public static List<StyledString> getStyledStringsFromText(String text) {
		List<StyledString> lines = Lists.newArrayList();
		StringBuilder build = new StringBuilder();
		SonarStyling style = new SonarStyling();

		for (int i = 0; i < text.length(); ++i) {
			char c0 = text.charAt(i);
			if (c0 == 167 && i + 1 < text.length()) {
				int i1 = "0123456789abcdefklmnor".indexOf(String.valueOf(text.charAt(i + 1)).toLowerCase(Locale.ROOT).charAt(0));
				if (build.length() != 0) {
					lines.add(new StyledString(build.toString(), style.copy()));
					build = new StringBuilder();
				}
				if (i1 < 16) {
					// colour info
					style.obfuscated = false;
					style.bold = false;
					style.strikethrough = false;
					style.underlined = false;
					style.italic = false;
					if (i1 < 0 || i1 > 15) {
						i1 = 15;
					}
					int formattingColour = GuiUtils.colorCodes[i1];
					int r = (int) (formattingColour >> 16 & 255);
					int g = (int) (formattingColour >> 8 & 255);
					int b = (int) (formattingColour & 255);
					style.rgb = new CustomColour(r, g, b).getRGB();

				} else if (i1 == 16) {
					style.obfuscated = true;
				} else if (i1 == 17) {
					style.bold = true;
				} else if (i1 == 18) {
					style.strikethrough = true;
				} else if (i1 == 19) {
					style.underlined = true;
				} else if (i1 == 20) {
					style.italic = true;
				} else if (i1 == 21) {
					// reset
					style.obfuscated = false;
					style.bold = false;
					style.strikethrough = false;
					style.underlined = false;
					style.italic = false;
				}

				++i;
			} else {
				build.append(c0);
			}
		}
		if (build.length() != 0)
			lines.add(new StyledString(build.toString(), style.copy()));
		return lines;
	}

}
