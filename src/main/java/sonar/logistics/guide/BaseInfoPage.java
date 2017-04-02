package sonar.logistics.guide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.common.MinecraftForge;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.guide.elements.ElementInfo;
import sonar.logistics.guide.elements.ElementInfoFormatted;
import sonar.logistics.guide.elements.ElementLink;

public abstract class BaseInfoPage implements IGuidePage {

	public int pageID;
	public int pageCount = 1;
	public int currentSubPage = 0;
	public HashMap<String, ElementInfoFormatted> pageInfo = new HashMap();
	public List<ElementLink> currentLinks = new ArrayList();
	public List<ElementInfoFormatted> currentData = new ArrayList();
	public ArrayList<IGuidePageElement> elements = new ArrayList();
	private GuiButton selectedButton;
	public List<GuiButton> guideButtons = new ArrayList();
	// public Pair<List<String>, List<PageLink>> current = new Pair(new ArrayList(), new ArrayList());

	public BaseInfoPage(int pageID) {
		this.pageID = pageID;
	}

	@Override
	public int pageID() {
		return pageID;
	}

	@Override
	public int getPageCount() {
		return pageCount;
	}
	
	//// CREATE \\\\

	public ArrayList<IGuidePageElement> getElements(GuiGuide gui, ArrayList<IGuidePageElement> elements) {
		return elements;
	}
	
	public void initGui(GuiGuide gui, int subPage) {
		currentSubPage = subPage;
		pageInfo.clear();
		guideButtons.clear();
		elements = getElements(gui, new ArrayList());
		List<ElementLink> newLinks = new ArrayList();
		List<ElementInfoFormatted> newData = new ArrayList();
		boolean newPage = false;
		int ordinal = 0;
		int lineTally = 0;
		ArrayList<ElementInfo> pgInfo = getPageInfo(gui, new ArrayList());
		int currentInfoPos = 0;

		for (ElementInfo info : pgInfo) {

			int to = 0;
			int from = 0;
			if (!newPage && info.newPage && lineTally != 0) {
				ordinal++; // gives the info a new sub page
				lineTally = 0;
				newPage = false;
			} else if (newPage) {
				//lineTally = 0;
				newPage = false;
			}

			ArrayList<ElementLink> links = new ArrayList();
			ArrayList<String> lines = GuidePageHelper.getLines(this, ordinal, lineTally, info, links);

			int numPagesNeeded = ((lines.size() + lineTally) / GuidePageHelper.maxLinesPerPage) + 1;
			int currentPages = numPagesNeeded;
			while (currentPages > 0) {
				// newPage=false;
				boolean firstPage = numPagesNeeded == currentPages;
				from = Math.min(GuidePageHelper.maxLinesPerPage * (numPagesNeeded - currentPages), to);
				to = Math.min((GuidePageHelper.maxLinesPerPage * ((numPagesNeeded + 1) - currentPages)) - lineTally, lines.size());

				ArrayList<ElementLink> pageLinks = new ArrayList();
				for (ElementLink link : links) {
					if (link.lineNum >= from && link.lineNum <= to) {
						int linePos = lineTally + link.lineNum - 1;
						link.setDisplayPosition(ordinal, (int) (this.getLineOffset(linePos, ordinal) * 0.75 + link.index), (int) (25 + ((linePos-((numPagesNeeded - currentPages)*GuidePageHelper.maxLinesPerPage)) * 12) * 0.75));
						pageLinks.add(link);
					} else {
						break;
					}
				}
				links.removeAll(pageLinks);

				List<String> wrapLines = lines.subList(from, to);
				if (!wrapLines.isEmpty()) {
					ElementInfo infoSource = new ElementInfo(info.key, info.additionals);
					ElementInfoFormatted infoFormatted = new ElementInfoFormatted(ordinal, infoSource, wrapLines, pageLinks);
					infoFormatted.setDisplayPosition(8, lineTally == 0 ? 0 : (int) ((lineTally) * 12));

					if (ordinal == subPage) {
						newData.add(infoFormatted);
						newLinks.addAll(infoFormatted.links);
					}

					pageInfo.put(info.key, infoFormatted);
					lineTally += wrapLines.size();
					// if (to != lines.size()-1 || currentInfoPos != pgInfo.size()-1) {
					if (to != lines.size()-1 || !(currentInfoPos + 1 >= pgInfo.size())) {
						if (lineTally + 1 >= GuidePageHelper.maxLinesPerPage) {
							ordinal++;
							lineTally = 0;
							newPage = true;
						} else {
							lineTally++;
						}
					}
				}
				currentPages--;
			}
			currentInfoPos++;
		}
		pageCount = Math.max(1 + ordinal, !elements.isEmpty() ? (elements.get(elements.size() - 1).getDisplayPage() + 1) : 0);
		currentLinks = newLinks;
		currentData = newData;
	}
	
	//// DRAWING \\\\

	public int getLineWidth(int linePos, int page) {
		int wrapWidth = 242;
		int pos = (int) (25 + (linePos * 12) * 1 / 0.75);

		for (IGuidePageElement e : elements) {
			if (e.getDisplayPage() == page) {
				int[] position = e.getSizing();
				if ((position[1] + position[3]) * 1 / 0.75 > pos) {
					wrapWidth -= ((position[2] + position[0]) * 0.75);
					break;
				}
			}
		}
		return (int) ((wrapWidth) * (1 / 0.75));
	}

	public int getLineOffset(int linePos, int page) {
		int pos = (int) (25 + linePos * 12);
		int offset = 0;
		for (IGuidePageElement e : elements) {
			if (e.getDisplayPage() == page) {
				int[] position = e.getSizing();
				if (/* position[1] <= pos && */position[1] + position[3] >= pos) {
					if ((position[0] + position[2]) > offset) {
						offset = position[0] + position[2];
					}
				}
			}
		}
		return (int) (offset);
	}
	
	public void drawPageInGui(GuiGuide gui, int yPos) {
		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
		FontHelper.text(getDisplayName(), 28, yPos + 3, -1);
	}

	public void drawPage(GuiGuide gui, int x, int y, int page) {

		for (ElementLink pageLink : currentLinks) {
			if (pageLink != null && pageLink.isMouseOver(gui, x - gui.getGuiLeft(), y - gui.getGuiTop())) {
				GlStateManager.disableDepth();
				gui.drawSonarCreativeTabHoveringText(TextFormatting.BLUE + "Open: " + TextFormatting.RESET + (pageLink.getGuidePage() == null ? "ERROR" : pageLink.getGuidePage().getDisplayName()), x, y);

				GlStateManager.disableLighting();
				break;
			}
		}
		for (IGuidePageElement element : elements) {
			if (element.getDisplayPage() == page) {
				RenderHelper.saveBlendState();
				element.drawElement(gui, gui.getGuiLeft() + element.getSizing()[0], gui.getGuiTop() + element.getSizing()[1], page, x, y);
				RenderHelper.restoreBlendState();
			}
		}
	}

	public void drawBackgroundPage(GuiGuide gui, int x, int y, int page) {
		for (IGuidePageElement element : elements) {
			if (element.getDisplayPage() == page) {
				RenderHelper.saveBlendState();
				element.drawBackgroundElement(gui, gui.getGuiLeft() + element.getSizing()[0], gui.getGuiTop() + element.getSizing()[1], page, x, y);
				RenderHelper.restoreBlendState();
			}
		}
	}

	public void drawForegroundPage(GuiGuide gui, int x, int y, int page) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		for (int i = 0; i < this.guideButtons.size(); ++i) {
			GuiButton button = ((GuiButton) this.guideButtons.get(i));
			button.drawButtonForegroundLayer(x, y);
		}
		GL11.glScaled(0.75, 0.75, 0.75);
		int listTally = 0;
		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
		//GlStateManager.enableLighting();
		for (ElementInfoFormatted guidePage : currentData) {
			List<String> info = guidePage.formattedList;
			for (int i = 0; i < Math.min(16, info.size()); i++) {
				String s = info.get(i);
				FontHelper.text(s, guidePage.displayX + getLineOffset(i + listTally, currentSubPage), 25 + (i + listTally) * 12, -1);
			}
			listTally += (listTally == 0 ? 1 : 1) + guidePage.formattedList.size();
		}
		GL11.glScaled(1 / 0.75, 1 / 0.75, 1 / 0.75);
		for (IGuidePageElement element : elements) {
			if (element.getDisplayPage() == currentSubPage) {
				RenderHelper.saveBlendState();
				element.drawForegroundElement(gui, element.getSizing()[0], element.getSizing()[1], page, x, y);
				RenderHelper.restoreBlendState();
			}
		}

		for (int i = 0; i < this.guideButtons.size(); ++i) {
			GuiButton button = ((GuiButton) this.guideButtons.get(i));
			int left = x + gui.getGuiLeft(), top = y + gui.getGuiTop();
			button.drawButton(gui.mc, left, top);
		}
	}
	
	//// INTERACTION \\\\

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
		for (ElementLink pageLink : currentLinks) {
			if (pageLink.isMouseOver(gui, x - gui.getGuiLeft(), y - gui.getGuiTop())) {
				gui.setCurrentPage(pageLink.guidePageLink, 0);
			}
		}
		for (IGuidePageElement element : elements) {
			if (element.getDisplayPage() == currentSubPage && element.mouseClicked(gui, this, x, y, button)) {
				return;
			}
		}
	}

	public void actionPerformed(GuiButton button) {}
}