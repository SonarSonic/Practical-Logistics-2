package sonar.logistics.client.gui.textedit;

import static net.minecraft.client.renderer.GlStateManager.popMatrix;
import static net.minecraft.client.renderer.GlStateManager.pushMatrix;
import static net.minecraft.client.renderer.GlStateManager.translate;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.xml.ws.Holder;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.config.GuiUtils;
import sonar.core.helpers.RenderHelper;
import sonar.core.utils.CustomColour;
import sonar.logistics.api.displays.WidthAlignment;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.types.StyledTextElement;
import sonar.logistics.api.displays.references.InfoReference;
import sonar.logistics.api.displays.storage.DisplayElementContainer;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.gui.GuiAbstractEditElement;
import sonar.logistics.client.gui.GuiInfoReferenceSource;
import sonar.logistics.helpers.DisplayElementHelper;

public class GuiEditStyledStrings extends GuiStyledStringFunctions implements ILineCounter, IInfoReferenceRequirementGui {
	public long lastCursorClick = 0;
	public boolean isDragging = false;

	public GuiEditStyledStrings(StyledTextElement text, DisplayElementContainer c) {
		super(text, c);
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		this.buttonList.add(new LogisticsButton(this, 0, guiLeft + 198, guiTop + 150, 11 * 16, 0, "Align Left", "Aligns the element to the left"));
		this.buttonList.add(new LogisticsButton(this, 1, guiLeft + 198 + 20, guiTop + 150, 11 * 16, 16, "Align Centre", "Aligns the element to the centre"));
		this.buttonList.add(new LogisticsButton(this, 2, guiLeft + 198 + 40, guiTop + 150, 11 * 16, 32, "Align Right", "Aligns the element to the right"));
		this.buttonList.add(new SpecialFormatButton(this, TextFormatting.BOLD, 3, guiLeft + 198, guiTop + 150 + 20, 11 * 16, 48, "Bold", "Make the selected text bold"));
		this.buttonList.add(new SpecialFormatButton(this, TextFormatting.ITALIC, 4, guiLeft + 198 + 20, guiTop + 150 + 20, 11 * 16, 64, "Italic", "Italicize the selected text"));
		this.buttonList.add(new SpecialFormatButton(this, TextFormatting.UNDERLINE, 5, guiLeft + 198 + 40, guiTop + 150 + 20, 11 * 16, 80, "Underline", "Underline the selected text"));
		this.buttonList.add(new SpecialFormatButton(this, TextFormatting.STRIKETHROUGH, 6, guiLeft + 198, guiTop + 150 + 40, 11 * 16, 96, "Strikethrough", "Draw a line through the middle of the selected text"));
		this.buttonList.add(new SpecialFormatButton(this, TextFormatting.OBFUSCATED, 7, guiLeft + 198 + 20, guiTop + 150 + 40, 2 * 16, 16 * 5, "Obfuscate", "Obfuscates the selected text"));
		this.buttonList.add(new LogisticsButton(this, 8, guiLeft + 198 + 40, guiTop + 150 + 40, 11 * 16, 112, "Font Colour", "Change the colour of the selected text"));
		this.buttonList.add(new LogisticsButton(this, 9, guiLeft + 198 - 20, guiTop + 150, 11 * 16, 10 * 16, "Add Info", "Add a reference to a info"));
		this.buttonList.add(new LogisticsButton(this, 10, guiLeft + 198 - 20, guiTop + 150 + 20, 11 * 16, 12 * 16, "Add Hyperlink", "Add a hyperlink to the selected text"));
		this.buttonList.add(new LogisticsButton(this, 11, guiLeft + 198 - 20, guiTop + 150 + 40, 11 * 16, 13 * 16, "Add Action", "Add an action to the selected text"));

		for (int i = 0; i < 16; i++) {
			TextFormatting format = TextFormatting.values()[i];
			this.buttonList.add(new TextColourButton(this, 16 + i, guiLeft + 2 + i * 16, guiTop + 210, format));
		}
		this.setSpacingScroller(text.spacing / 50);
		this.cursorPosition.setYToLast(this);
		this.cursorPosition.setXToLast(this);
	}

	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button instanceof TextColourButton) {
			changeSelectedColour(((TextColourButton) button).colour);
		}
		if (button instanceof SpecialFormatButton) {
			toggleSpecialFormatting(((SpecialFormatButton) button).specialFormat);
		}
		if (button instanceof LogisticsButton) {
			List<TextSelection> select = getAllSelections();
			if (select.isEmpty())
				select = Lists.newArrayList(this.getTypeBox());
			switch (button.id) {
			case 0:
				formatSelectedLines(select, line -> {
					line.setAlign(WidthAlignment.LEFT);
					return line;
				});
				break;
			case 1:
				formatSelectedLines(select, line -> {
					line.setAlign(WidthAlignment.CENTERED);
					return line;
				});
				break;
			case 2:
				formatSelectedLines(select, line -> {
					line.setAlign(WidthAlignment.RIGHT);
					return line;
				});
				break;
			case 8:
				setTextColourOnSelected(currentColour);
				break;
			case 9:
				if (cursorPosition.validPosition()) {
					FMLClientHandler.instance().showGuiScreen(new GuiInfoReferenceSource(new InfoReferenceRequest(this, 1), text.getGSI(), this.inventorySlots));
				}
				break;
			case 10:
				// hyperlink
				break;
			case 11:
				// action
				break;
			}
		}
	}

	@Override
	public void setSpacingScroller(float scaling) {		
		//spacing_scroller.currentScroll = scaling;
		//text.spacing = (int) (scaling * 50);
		///c.updateActualScaling();
	}

	@Override
	public void onRequirementCompleted(List<InfoReference> selected) {
		if (selected.isEmpty()) {
			return;
		}
		List newStrings = Lists.newArrayList();
		selected.forEach(ref -> newStrings.add(new StyledInfo(ref.uuid, ref.refType, createStylingFromEnabled())));
		addStyledStrings(newStrings);
	}

	public boolean doContainerClick(double clickX, double clickY, int key) {
		if (isDoubleClick()) {
			GuiActions.SELECT_ALL.trigger(this);
			lastCursorClick = -1;
		} else {
			if (!GuiScreen.isShiftKeyDown()) {
				if (GuiScreen.isCtrlKeyDown() && cursorPosition.validPosition() && selectPosition.validPosition()) {
					TextSelection.addWithCombine(savedSelections, new TextSelection(cursorPosition, selectPosition));
				} else {
					savedSelections.clear();
				}
				selectPosition.removeCursor();
			}
			cursorPosition.setCursor(getDragPositionFromContainerXY(clickX, clickY));
			lastCursorClick = mc.getSystemTime();
		}
		return true;
	}

	//// TEXT DRAGGING \\\\

	public void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
		boolean setTextDrag = true;
		Tuple<Boolean, double[]> canClick = canClickContainer(x, y);
		if (canClick.getFirst() && lastCursorClick != -1) {
			if (isDragging) {
				boolean canContinue = Mouse.isButtonDown(0);
				if (!canContinue) {
					isDragging = false;
				} else {
					cursorPosition.setCursor(getDragPositionFromMouseXY(x, y));
				}
			} else if (lastCursorClick + 100 < mc.getSystemTime()) {
				isDragging = Mouse.isButtonDown(0);
				lastCursorClick = mc.getSystemTime();
				if (isDragging) {
					selectPosition.setCursor(cursorPosition.x, cursorPosition.y);
				}
			}
		}
		super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException {
		StyledStringLine ss = cursorPosition.validPosition() ? cursorPosition.getTypingLine(this) : null;
		boolean triggered = HotKeyFunctions.checkFunction(this, ss, c, i);
		GuiActions.UPDATE_TEXT_SCALING.trigger(this);
		if (triggered) {
			return;
		}
		if (isCloseKey(i)) {
			save();
		}
		super.keyTyped(c, i);
	}
	/* public static String getClipboardString() { try { Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents((Object) null); if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) { return (String) transferable.getTransferData(DataFlavor.stringFlavor); } } catch (Exception var1) { ; } return ""; } /** Stores the given string in the system clipboard public static void setClipboardString(String copyText) { if (!StringUtils.isEmpty(copyText)) { try { StringSelection stringselection = new StringSelection(copyText); Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, (ClipboardOwner) null); } catch (Exception var2) { ; } } } */

}