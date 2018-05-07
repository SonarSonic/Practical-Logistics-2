package sonar.logistics.api.displays.elements.text;

import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/** lots of copying from FontRenderer, with changes to enable StyledString renders ... Justify, Line wrapping, multiple page text renders. */
public class StyledStringRenderer extends FontRenderer {

	public static final StyledStringRenderer instance = new StyledStringRenderer();

	public static StyledStringRenderer instance() {
		return instance;
	}

	public StyledStringRenderer() {
		super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"), Minecraft.getMinecraft().renderEngine, false);
		if (Minecraft.getMinecraft().gameSettings.language != null) {
			setUnicodeFlag(Minecraft.getMinecraft().isUnicode());
			setBidiFlag(Minecraft.getMinecraft().getLanguageManager().isCurrentLanguageBidirectional());
		}
	}

	public static class SimpleIndex {

		public IStyledString string;
		public int start, end, lineIndex;

		public SimpleIndex(IStyledString string, int start, int end, int lineIndex) {
			this.string = string;
			this.start = start;
			this.end = end;
			this.lineIndex = lineIndex;
		}

		public boolean equals(Object obj) {
			if (obj instanceof SimpleIndex) {
				return ((SimpleIndex) obj).string == string && ((SimpleIndex) obj).start == start && ((SimpleIndex) obj).end == end;
			}
			return false;
		}
	}

	public static class StyledStringRenderHandler {
		public final StyledTextElement text;
		public int line_spacing = 0; // for now.
		public int font_height = instance().FONT_HEIGHT;
		public Map<Integer, List<SimpleIndex>> lines = new HashMap<>();

		public int buildingLine = 0;
		public double buildingX = 0;
		public double buildingY = 0;
		public double build_width;
		public double build_height;
		public List<SimpleIndex> lineBuild = new ArrayList<>();
		public int last_space = -1;
		public int linesPerPage;
		public IStyledString last_space_string = null;

		public StyledStringLine currentLine;
		public IStyledString currentString;

		public StyledStringRenderHandler(StyledTextElement text) {
			this.text = text;
		}

		public void incrementX(double increment) {
			buildingX += increment;
			if (buildingX >= build_width) {
				incrementY(totalLineSize(), false);
			}
		}

		public void incrementY(double increment, boolean finishedLine) {
			List<SimpleIndex> nextLine = new ArrayList<>();
			if (!finishedLine) {
				nextLine = trimToLastSpace(lineBuild);
			}
			saveSimpleIndexs();
			buildingX = 0;
			buildingY += increment;
			buildingLine++;

			if (totalLineSize() > remainingY()) {
				// move to next page
				buildingX = 0;
				buildingY = 0;
			}

			int total_width = 0;
			for (SimpleIndex index : nextLine) {
				total_width += instance().getRenderStringWidthWithStyledString(index.string, index.string.getUnformattedString().substring(index.start, index.end));
			}
			incrementX(total_width);
			lineBuild.addAll(nextLine);
		}

		public double remainingY() {
			return build_height - buildingY;
		}

		public double remainingX() {
			return build_width - buildingX;
		}

		public double totalLineSize() {
			return font_height + line_spacing;
		}

		public void saveSimpleIndexs() {
			lines.put(buildingLine, Lists.newArrayList(lineBuild));
			lineBuild.clear();
		}

		public void addSimpleIndex(SimpleIndex index) {
			if (lineBuild.isEmpty()) {
				String s = index.string.getUnformattedString().substring(index.start, index.end);
				if (s.isEmpty()) {
					return;
				}
				if (s.charAt(0) == ' ') {
					index.start++;
				}
			}

			lineBuild.add(index);
		}

		public List<SimpleIndex> trimToLastSpace(List<SimpleIndex> indexes) {
			if (last_space == -1 || last_space_string == null) {
				return new ArrayList<>();
			}
			List<SimpleIndex> nextLine = new ArrayList<>();
			boolean found = false;
			for (SimpleIndex si : indexes) {
				if (found) {
					nextLine.add(si);
				} else if (si.string == last_space_string && si.start <= last_space && last_space + 1 < si.end) {
					found = true;
					nextLine.add(new SimpleIndex(si.string, last_space + 1, si.end, si.lineIndex));
					si.end = last_space;
				}
			}
			indexes.removeAll(nextLine);
			return nextLine;
		}

		public void wrapLines(double width, double height) {
			lines.clear();
			build_width = width;
			build_height = height;
			buildingX = 0;
			buildingY = 0;
			buildingLine = 0;
			linesPerPage = (int) Math.floor((height / totalLineSize()));
			if (linesPerPage == 0) {
				return;
			}
			for (StyledStringLine line : text) {
				currentLine = line;
				int lineIndex = 0;
				for (IStyledString ss : line) {
					currentString = ss;
					String string = ss.getUnformattedString();
					int startIndex = 0;
					int endIndex = 0;
					double indexed_width = 0;
					for (int i = 0; i < string.length(); ++i) {
						char current_char = string.charAt(i);
						int char_width = instance().getCharRenderWidthFromStyledString(ss, current_char);

						if (current_char == ' ' || current_char == 160) {
							last_space = i;
							last_space_string = ss;
							// find next space
						}

						if (char_width > width) {
							return; // TOO SMALL A SCREEN
						}

						if (char_width <= remainingX() - indexed_width) { // space available
							indexed_width += char_width;
							endIndex++;
						} else { // no space
							if (startIndex != endIndex) {
								addSimpleIndex(new SimpleIndex(ss, startIndex, endIndex, lineIndex));
								incrementX(indexed_width);
								indexed_width = 0;
							}
							startIndex = i;
							endIndex = i + 1;
							incrementY(totalLineSize(), false);
							incrementX(char_width);
						}
					}
					if (startIndex != endIndex) {
						addSimpleIndex(new SimpleIndex(ss, startIndex, endIndex, lineIndex));
						incrementX(indexed_width);
					}
					lineIndex += ss.getStringLength();
				}
				incrementY(totalLineSize(), true);
			}
			saveSimpleIndexs();
			/* for (StyledStringLine line : text) { line.getCachedUnformattedString().indexOf(' '); for (IStyledString ss : line) { String string = ss.getUnformattedString(); int ; int char_width = instance().getCharRenderWidthFromStyledString(ss, current_char); } } current_y += font_height + line_spacing; current_x = 0; } */
		}
	}

	public float getPosX() {
		return posX;
	}

	public float getPosY() {
		return posY;
	}

	public void incrementRenderPositions(double x, double y) {
		this.posX += (float) x;
		this.posY += (float) y;
	}

	public void setRenderPositions(double x, double y) {
		this.posX = (float) x;
		this.posY = (float) y;
	}

	public void renderWrappedText(StyledStringRenderHandler handler, double width, double height, int startLine, int endLine) {
		Map<Integer, List<SimpleIndex>> page = handler.lines;
		if (page != null && !page.isEmpty()) {
			setRenderPositions(0, 0);
			for (Entry<Integer, List<SimpleIndex>> line : page.entrySet()) {
				if (line.getKey() >= startLine && line.getKey() < endLine) {
					for (SimpleIndex index : line.getValue()) {
						renderTextWithStyledString(index.string, index.string.getUnformattedString().substring(index.start, index.end));
					}
					setRenderPositions(0, getPosY() + handler.totalLineSize());
				}
			}
		}
	}

	public static final String TEXT_FORMATTING_IDENTIFIERS = "0123456789abcdefklmnor";
	public static final String ALLOWED_CHARS = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";

	public void startStyledStringRender(IStyledString ss) {
		if (getBidiFlag()) {
			// text = this.bidiReorderWithStyledString(ss, text);
		}
		int red = (ss.getStyle().rgb >> 16) & 0x000000FF;
		int green = (ss.getStyle().rgb >> 8) & 0x000000FF;
		int blue = (ss.getStyle().rgb) & 0x000000FF;
		float alpha = 1;// need to get this info from the styling!
		GlStateManager.enableAlpha();
		GlStateManager.disableLighting();
		GlStateManager.color(red / 256F, green / 256F, blue / 256F, alpha);
	}

	public void renderTextWithStyledString(IStyledString ss, String text) {
		startStyledStringRender(ss);
		for (int i = 0; i < text.length(); ++i) {
			char current_char = text.charAt(i);
			renderChar(ss, current_char);
		}
	}

	public void renderChar(IStyledString ss, char current_char) {
		boolean shadow = false;// need to get this info from the styling!
		if (current_char == 167) {
			// for text formatting, however these should not be in the unformatted string, so ignore them. //++i;
		} else {
			int char_index = ALLOWED_CHARS.indexOf(current_char);

			if (ss.getStyle().obfuscated && char_index != -1) {
				/** cycles randomly through the allowed chars until it finds one of the same width to replace the current char. */
				int target_width = this.getCharWidthFromStyledString(ss, current_char);
				char obfuscated_char;
				do {
					char_index = this.fontRandom.nextInt(ALLOWED_CHARS.length());
					obfuscated_char = ALLOWED_CHARS.charAt(char_index);
				} while (target_width != getCharWidthFromStyledString(ss, obfuscated_char));

				current_char = obfuscated_char;
			}

			float f1 = char_index == -1 || getUnicodeFlag() ? 0.5f : 1f;
			boolean flag = false;

			float char_width = this.renderCharFromStyledString(ss, current_char);

			if (ss.getStyle().bold) {
				this.posX += f1;

				this.renderCharFromStyledString(ss, current_char);
				this.posX -= f1;

				++char_width;
			}
			doDrawWithStyledString(ss, char_width);
		}
	}

	public int getRenderStringWidthWithStyledString(IStyledString ss, String string) {
		int width = 0;
		for (int i = 0; i < string.length(); ++i) {
			char current_char = string.charAt(i);
			width += getCharRenderWidthFromStyledString(ss, current_char);
		}
		return width;
	}

	public float renderCharFromStyledString(IStyledString string, char ch) {
		if (ch == 160)
			return 4.0F; // forge: display nbsp as space. MC-2595
		if (ch == ' ') {
			return 4.0F;
		} else {
			int i = ALLOWED_CHARS.indexOf(ch);
			return i != -1 && !getUnicodeFlag() ? this.renderDefaultChar(i, string.getStyle().italic) : this.renderUnicodeChar(ch, string.getStyle().italic);
		}
	}

	public int getCharWidthFromStyledString(IStyledString string, char character) {
		if (character == 160)
			return 4; // forge: display nbsp as space. MC-2595
		if (character == 167) {
			return -1;
		} else if (character == ' ') {
			return 4;
		} else {
			int i = ALLOWED_CHARS.indexOf(character);

			if (character > 0 && i != -1 && !this.getUnicodeFlag()) {
				return this.charWidth[i];
			} else if (this.glyphWidth[character] != 0) {
				int j = this.glyphWidth[character] & 255;
				int k = j >>> 4;
				int l = j & 15;
				++l;
				return (l - k) / 2 + 1;
			} else {
				return 0;
			}
		}
	}

	public int getCharRenderWidthFromStyledString(IStyledString string, char character) {
		int normal_width = getCharWidthFromStyledString(string, character);
		if (string.getStyle().bold) {
			normal_width += 1;
		}
		return normal_width;
	}

	public void doDrawWithStyledString(IStyledString string, float f) {

		if (string.getStyle().strikethrough) {
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			GlStateManager.disableTexture2D();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
			bufferbuilder.pos((double) this.posX, (double) (this.posY + (float) (this.FONT_HEIGHT / 2)), 0.0D).endVertex();
			bufferbuilder.pos((double) (this.posX + f), (double) (this.posY + (float) (this.FONT_HEIGHT / 2)), 0.0D).endVertex();
			bufferbuilder.pos((double) (this.posX + f), (double) (this.posY + (float) (this.FONT_HEIGHT / 2) - 1.0F), 0.0D).endVertex();
			bufferbuilder.pos((double) this.posX, (double) (this.posY + (float) (this.FONT_HEIGHT / 2) - 1.0F), 0.0D).endVertex();
			tessellator.draw();
			GlStateManager.enableTexture2D();
		}

		if (string.getStyle().underlined) {
			Tessellator tessellator1 = Tessellator.getInstance();
			BufferBuilder bufferbuilder1 = tessellator1.getBuffer();
			GlStateManager.disableTexture2D();
			bufferbuilder1.begin(7, DefaultVertexFormats.POSITION);
			int l = string.getStyle().underlined ? -1 : 0;
			bufferbuilder1.pos((double) (this.posX + (float) l), (double) (this.posY + (float) this.FONT_HEIGHT), 0.0D).endVertex();
			bufferbuilder1.pos((double) (this.posX + f), (double) (this.posY + (float) this.FONT_HEIGHT), 0.0D).endVertex();
			bufferbuilder1.pos((double) (this.posX + f), (double) (this.posY + (float) this.FONT_HEIGHT - 1.0F), 0.0D).endVertex();
			bufferbuilder1.pos((double) (this.posX + (float) l), (double) (this.posY + (float) this.FONT_HEIGHT - 1.0F), 0.0D).endVertex();
			tessellator1.draw();
			GlStateManager.enableTexture2D();
		}

		this.posX += (float) ((int) f);

	}

	public String bidiReorderWithStyledString(IStyledString string, String text) {
		try {
			Bidi bidi = new Bidi((new ArabicShaping(8)).shape(text), 127);
			bidi.setReorderingMode(0);
			return bidi.writeReordered(2);
		} catch (ArabicShapingException var3) {
			return text;
		}
	}
}
