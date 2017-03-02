package sonar.logistics.guide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.common.MinecraftForge;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.utils.Pair;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.GuiGuide;

public abstract class BaseInfoPage implements IGuidePage {

	public int pageID;
	public int pageCount = 1;
	public int currentSubPage = 0;
	public HashMap<String, GuidePageInfoFormatted> pageInfo = new HashMap();
	public List<GuidePageLink> currentLinks = new ArrayList();
	public List<GuidePageInfoFormatted> currentData = new ArrayList();
	public ArrayList<IGuidePageElement> elements = new ArrayList();
	private GuiButton selectedButton;
	public List<GuiButton> guideButtons = new ArrayList();
	// public Pair<List<String>, List<PageLink>> current = new Pair(new ArrayList(), new ArrayList());

	public BaseInfoPage(int pageID) {
		this.pageID = pageID;
	}

	public void initGui(GuiGuide gui, int subPage) {
		currentSubPage = subPage;
		pageInfo.clear();
		guideButtons.clear();
		elements = getElements(new ArrayList());
		List<GuidePageLink> newLinks = new ArrayList();
		List<GuidePageInfoFormatted> newData = new ArrayList();
		int ordinal = 0;
		int lineTally = 0;
		ArrayList<GuidePageInfo> pgInfo = getPageInfo(new ArrayList());
		for (GuidePageInfo info : pgInfo) {
			int to = 0;
			int from = 0;
			if (info.newPage && lineTally != 0) {
				ordinal++; // gives the info a new sub page
			}
			ArrayList<GuidePageLink> links = new ArrayList();
			// Iterator<GuidePageLink> iterator = links.iterator();
			ArrayList<String> lines = GuidePageHelper.getList(this, lineTally, info, links);

			int numPagesNeeded = ((lines.size() + lineTally) / GuidePageHelper.maxLinesPerPage) + 1;
			int currentPages = numPagesNeeded;
			while (currentPages > 0) {
				boolean firstPage = numPagesNeeded == currentPages;
				from = Math.min(GuidePageHelper.maxLinesPerPage * (numPagesNeeded - currentPages), to);
				to = Math.min((GuidePageHelper.maxLinesPerPage * ((numPagesNeeded + 1) - currentPages)) - lineTally, lines.size());

				ArrayList<GuidePageLink> pageLinks = new ArrayList();
				for (GuidePageLink link : links) {
					if (link.lineNum >= from && link.lineNum <= to) {
						link.setDisplayPosition(ordinal, link.index + (int) (((firstPage && this instanceof BaseItemPage && ordinal == 0) ? 96 : 0) * 0.75), (int) (25 + ((lineTally + link.lineNum - 1) * 12) * 0.75));
						pageLinks.add(link);
					} else {
						break;
					}
				}
				links.removeAll(pageLinks);

				List<String> wrapLines = lines.subList(from, to);
				GuidePageInfo infoSource = new GuidePageInfo(info.key, info.additionals);
				GuidePageInfoFormatted infoFormatted = new GuidePageInfoFormatted(ordinal, infoSource, wrapLines, pageLinks);
				infoFormatted.setDisplayPosition(0, lineTally == 0 ? 0 : (int) ((lineTally) * 12));
				if (ordinal == subPage) {
					newData.add(infoFormatted);
					newLinks.addAll(infoFormatted.links);
				}
				pageInfo.put(info.key, infoFormatted);
				lineTally += wrapLines.size();
				if (lineTally >= GuidePageHelper.maxLinesPerPage) {
					ordinal++;
					lineTally = 0;
				} else {
					lineTally++;
				}

				currentPages--;
			}
		}
		pageCount = Math.max(1 + ordinal, !elements.isEmpty() ? (elements.get(elements.size() - 1).getDisplayPage() + 1) : 0);
		currentLinks = newLinks;
		currentData = newData;
	}

	public void drawPageInGui(GuiGuide gui, int yPos) {
		FontHelper.text(getDisplayName(), 28, yPos + 3, -1);
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

	}

	public void drawPage(GuiGuide gui, int x, int y, int page) {
		GL11.glScaled(0.75, 0.75, 0.75);
		int listTally = 0;
		for (GuidePageInfoFormatted guidePage : currentData) {
			// int wrapPosition = Math.max(7 - listTally, 0);
			/* if (wrapPosition != 0 && currentSubPage == 0) { FontHelper.text(guidePage.formattedList, 0, wrapPosition, 12, 96, 25 + guidePage.displayY, LogisticsColours.white_text.getRGB()); FontHelper.text(guidePage.formattedList, wrapPosition, 16, 12, 5, 25 + guidePage.displayY, LogisticsColours.white_text.getRGB()); } else { } */
			List<String> info = guidePage.formattedList;
			for (int i = 0; i < Math.min(16, info.size()); i++) {
				String s = info.get(i);
				FontHelper.text(s, 8 + getLineOffset(i + listTally), 25 + (i + listTally) * 12, LogisticsColours.white_text.getRGB());
			}

			listTally += guidePage.formattedList.size() + 1;
		}
		GL11.glScaled(1 / 0.75, 1 / 0.75, 1 / 0.75);

		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();
		for (GuidePageLink pageLink : currentLinks) {
			if (pageLink!=null && pageLink.isMouseOver(gui, x - gui.getGuiLeft(), y - gui.getGuiTop())) {
				gui.drawSonarCreativeTabHoveringText(TextFormatting.BLUE + "Open: " + TextFormatting.RESET + pageLink.getGuidePage() == null ? "ERROR" : pageLink.getGuidePage().getDisplayName(), x - gui.getGuiLeft(), y - gui.getGuiTop());
			}
		}
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
		for (IGuidePageElement element : elements) {
			if (element.getDisplayPage() == currentSubPage) {
				element.drawElement(gui, this, element.getSizing()[0], element.getSizing()[1], page);
			}
		}
		for (int i = 0; i < this.guideButtons.size(); ++i) {
			((GuiButton) this.guideButtons.get(i)).drawButton(gui.mc, x, y);
		}
	}

	public void mouseClicked(GuiGuide gui, int x, int y, int button) {
		if (button == 0) {
			for (int i = 0; i < this.guideButtons.size(); ++i) {
				GuiButton guibutton = (GuiButton) this.guideButtons.get(i);

				if (guibutton.mousePressed(gui.mc, x - gui.getGuiLeft(), y - gui.getGuiTop())) {
					ActionPerformedEvent.Pre event = new ActionPerformedEvent.Pre(gui, guibutton, this.guideButtons);
					if (MinecraftForge.EVENT_BUS.post(event))
						break;
					guibutton = event.getButton();
					this.selectedButton = guibutton;
					guibutton.playPressSound(gui.mc.getSoundHandler());
					this.actionPerformed(guibutton);
					if (this.equals(gui.mc.currentScreen))
						MinecraftForge.EVENT_BUS.post(new ActionPerformedEvent.Post(gui, event.getButton(), this.guideButtons));
				}
			}
		}
		for (GuidePageLink pageLink : currentLinks) {
			if (pageLink.isMouseOver(gui, x - gui.getGuiLeft(), y - gui.getGuiTop())) {
				gui.setCurrentPage(pageLink.guidePageLink, 0);
			}
		}
	}

	public void actionPerformed(GuiButton button) {

	}

	public ArrayList<IGuidePageElement> getElements(ArrayList<IGuidePageElement> elements) {
		return elements;
	}

	public int getLineWidth(int linePos) {
		int wrapWidth = 242;
		int pos = 10 + (int) ((linePos * 12) * 1 / 0.75);

		for (IGuidePageElement e : elements) {
			if (e.getDisplayPage() == currentSubPage) {
				int[] position = e.getSizing();
				if ((position[1] + position[3]) * 1 / 0.75 > pos) {
					wrapWidth -= ((position[2] + position[0]) * 0.75);
				}
			}
		}
		return (int) ((wrapWidth) * (1 / 0.75));
	}

	public int getLineOffset(int linePos) {
		int pos = (int) (15 + linePos * 12);
		int offset = 0;
		for (IGuidePageElement e : elements) {
			if (e.getDisplayPage() == currentSubPage) {
				int[] position = e.getSizing();
				if (position[1] <= pos && position[1] + position[3] >= pos) {
					if (position[2] > offset) {
						offset = position[2];
					}

				}
			}
		}
		return (int) (offset);
	}

	@Override
	public int pageID() {
		return pageID;
	}

	@Override
	public int getPageCount() {
		return pageCount;
	}

}