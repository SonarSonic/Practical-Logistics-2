package sonar.logistics.core.tiles.displays.info.types.text.utils;

import sonar.logistics.core.tiles.displays.info.types.text.StyledTextElement;
import sonar.logistics.core.tiles.displays.info.types.text.styling.IStyledString;
import sonar.logistics.core.tiles.displays.info.types.text.styling.StyledInfo;
import sonar.logistics.core.tiles.displays.info.types.text.styling.StyledString;
import sonar.logistics.core.tiles.displays.info.types.text.styling.StyledStringLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

public class StyledStringFormatter {

	public static void formatTextSelections(StyledTextElement text, List<TextSelection> selections, BiFunction<Integer, IStyledString, IStyledString> formatter) {
		for (TextSelection select : selections) {
			for (int y = select.startY; y <= select.endY; y++) {
				StyledStringLine c = text.getLine(y);
				if (c == null) {
					continue;
				}
				int[] subSelect = select.getSubStringSize(c.getCachedUnformattedString(), y);
				int start = subSelect[0], end = subSelect[1];
				if (start != -1 && end != -1) {
					List<IStyledString> formatted_strings = new ArrayList<>();

					int index_count = 0;
					for (IStyledString ss : c.getStrings()) {
						int subStart = Math.max(index_count, start) - index_count;
						int subEnd = Math.min(index_count + ss.getStringLength(), end) - index_count;

						if (subStart >= 0 && subStart < subEnd) {
							if (ss instanceof StyledInfo) {
								StyledStringHelper.addWithCombine(formatted_strings, formatter.apply(y, ss.copy()));
							} else {
								String[] subStrings = StyledStringHelper.getSubStrings(subStart, subEnd, ss.getUnformattedString());
								StyledStringHelper.addWithCombine(formatted_strings, new StyledString(subStrings[0], ss.getStyle().copy()));
								StyledStringHelper.addWithCombine(formatted_strings, formatter.apply(y, new StyledString(subStrings[1], ss.getStyle().copy())));
								StyledStringHelper.addWithCombine(formatted_strings, new StyledString(subStrings[2], ss.getStyle().copy()));
							}
						} else {
							StyledStringHelper.addWithCombine(formatted_strings, ss);
						}
						index_count += ss.getStringLength();
					}

					c.setStrings(formatted_strings);
				}
			}
		}
		// formatSelectedLines(selects, line -> line.getStrings().isEmpty() ? null : line);
		if (text.getLines().isEmpty()) {
			text.addNewLine(0, new StyledStringLine(text, ""));
		}
	}

	public static void formatSelectedLines(StyledTextElement text, List<TextSelection> selections, Function<StyledStringLine, StyledStringLine> formatter) {
		Map<Integer, StyledStringLine> lines = new HashMap<>();
		for (TextSelection select : selections) {
			for (int y = select.startY; y <= select.endY; y++) {
				StyledStringLine c = text.getLine(y);
				if (c != null) {
					lines.put(y, c);
				}
			}
		}
		List<StyledStringLine> toRemove = new ArrayList<>();
		for (Entry<Integer, StyledStringLine> line : lines.entrySet()) {
			StyledStringLine newLine = formatter.apply(line.getValue());
			if (newLine == null) {
				toRemove.add(text.getLines().get(line.getKey()));
			} else {
				text.setLine(line.getKey(), newLine);
			}
		}
		for (StyledStringLine line : toRemove) {
			if (line != null) {
				text.getLines().remove(line);
			}
		}
	}
}
