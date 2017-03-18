package sonar.logistics.guide;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.text.TextFormatting;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.guide.elements.ElementInfo;
import sonar.logistics.guide.elements.ElementLink;

public class GuidePageHelper {

	public static String OBJ = "&&";
	public static String FORMAT_START = "<";
	public static String FORMAT_END = ">";
	public static String PAGE_LINK = "#";
	public static String PAGE_LINK_PLACEHOLDER = "\u00a3";
	public static String PAGE_LINK_ERROR = TextFormatting.RED + "PAGE NOT FOUND";
	public static String CURRENT = "CURRENT";

	public static int maxLinesPerPage = 13;

	/**this formats the {@link ElementInfo} to add any page links, formatting, or object inserts prior to splitting the lines with 'createLines'*/
	public static ArrayList<String> getLines(IGuidePage current, int ordinal, int lineTally, ElementInfo info, ArrayList<ElementLink> links) {
		String value = FontHelper.translate(info.key);
		for (String add : info.additionals) {
			value = value.replaceFirst(OBJ, add);
		}
		for (TextFormatting format : TextFormatting.values()) {
			value = value.replaceAll(FORMAT_START + format.getFriendlyName() + FORMAT_END, format.toString());
		}

		int index = 0;
		ArrayList<Integer> pageIDs = new ArrayList();
		while ((index = value.indexOf(PAGE_LINK)) != -1) {
			String toTest = value.substring(index + 1);
			int side = toTest.indexOf(PAGE_LINK);
			if (side > 0) {
				IGuidePage linkedPage = null;
				String pageString = toTest.substring(0, side);
				if (!pageString.equals(CURRENT)) {
					if (Character.isDigit(toTest.charAt(0))) {
						int pgNum = Integer.parseInt(pageString);
						linkedPage = GuidePageRegistry.getGuidePage(pgNum);
					} else {
						linkedPage = GuidePageRegistry.getGuidePage(pageString);
					}
					pageString = PAGE_LINK + pageString + PAGE_LINK;
					value = value.replaceFirst(pageString, PAGE_LINK_PLACEHOLDER + (linkedPage == null ? PAGE_LINK_ERROR : TextFormatting.BLUE + "" + TextFormatting.UNDERLINE + linkedPage.getDisplayName()) + TextFormatting.RESET);
					pageIDs.add(linkedPage != null ? linkedPage.pageID() : -1);
				} else {
					pageString = PAGE_LINK + pageString + PAGE_LINK;
					value = value.replaceAll(pageString, current.getDisplayName());
				}
			} else {
				break;
			}
		}

		return GuidePageHelper.createLines(current, ordinal, lineTally, info, new ArrayList(), ((ArrayList<Integer>) pageIDs.clone()).iterator(), links, value);
	}
	/**splits the {@link ElementInfo} info individual page lines to be rendered, it also adds and {@link ElementLink}s to the list */
	public static ArrayList<String> createLines(IGuidePage current, int ordinal, int lineTally, ElementInfo info, ArrayList<String> lines, Iterator<Integer> pageNums, ArrayList<ElementLink> pageLinks, String str) throws StackOverflowError {
		FontRenderer render = Minecraft.getMinecraft().fontRendererObj;
		String[] split = str.split("-");

		for (String sp : split) {
			int pg = ordinal + ((lines.size() + lineTally) / maxLinesPerPage);
			int i = FontHelper.sizeStringToWidth(render, sp, current.getLineWidth(lines.size() + lineTally - (pg * maxLinesPerPage), pg));
			if (sp.length() <= i) {
				addNewLine(sp, lines, pageNums, pageLinks);
			} else {
				String s = sp.substring(0, i);
				char c0 = sp.charAt(i);
				boolean flag = c0 == 32 || c0 == 10;
				String s1 = render.getFormatFromString(s) + sp.substring(i + (flag ? 1 : 0));
				addNewLine(s, lines, pageNums, pageLinks);
				createLines(current, ordinal, lineTally, info, lines, pageNums, pageLinks, s1);
			}
		}
		return lines;
	}

	public static void addNewLine(String line, ArrayList<String> lines, Iterator<Integer> iterator, ArrayList<ElementLink> pageLinks) {
		int index = 0;
		while ((index = line.indexOf(PAGE_LINK_PLACEHOLDER)) != -1 && iterator.hasNext()) {
			int link = iterator.next();
			IGuidePage linked = GuidePageRegistry.getGuidePage(link);
			String before = line.substring(0, index);
			pageLinks.add(new ElementLink(link, (int) ((RenderHelper.fontRenderer.getStringWidth(linked != null ? linked.getDisplayName() : PAGE_LINK_ERROR)) * 0.75), lines.size(), (int) (RenderHelper.fontRenderer.getStringWidth(before) * 0.75)));

			line = before + line.substring(index + 1);
		}
		lines.add(line);
	}

	public static Object getRecipeForItem(ItemStack stack) {
		List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
		for (IRecipe recipe : recipes) {
			ItemStack output = recipe.getRecipeOutput();
			if (output != null && ItemStack.areItemsEqual(stack, output)) {
				return recipe;
			}
		}
		return null;
	}
}
