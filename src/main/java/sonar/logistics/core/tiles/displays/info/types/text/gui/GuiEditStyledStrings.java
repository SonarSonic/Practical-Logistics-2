package sonar.logistics.core.tiles.displays.info.types.text.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import sonar.core.client.gui.IGuiOrigin;
import sonar.core.client.gui.SonarTextField;
import sonar.core.client.gui.widgets.ScrollerOrientation;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.logistics.base.gui.GuiLogistics;
import sonar.logistics.base.gui.buttons.LogisticsButton;
import sonar.logistics.base.requests.colour.CustomColourButton;
import sonar.logistics.base.requests.colour.GuiColourSelection;
import sonar.logistics.base.requests.colour.SpecialFormatButton;
import sonar.logistics.base.requests.colour.TextColourButton;
import sonar.logistics.base.requests.hyperlink.GuiHyperlinkAdd;
import sonar.logistics.base.requests.hyperlink.HyperlinkRequest;
import sonar.logistics.base.requests.hyperlink.IHyperlinkRequirementGui;
import sonar.logistics.base.requests.reference.GuiInfoReferenceSource;
import sonar.logistics.base.requests.reference.IInfoReferenceRequirementGui;
import sonar.logistics.base.requests.reference.InfoReferenceRequest;
import sonar.logistics.core.tiles.displays.gsi.interaction.actions.ClickHyperlink;
import sonar.logistics.core.tiles.displays.info.elements.base.WidthAlignment;
import sonar.logistics.core.tiles.displays.info.references.InfoReference;
import sonar.logistics.core.tiles.displays.info.types.text.StyledTextElement;
import sonar.logistics.core.tiles.displays.info.types.text.gui.hotkeys.GuiActions;
import sonar.logistics.core.tiles.displays.info.types.text.gui.hotkeys.HotKeyFunctions;
import sonar.logistics.core.tiles.displays.info.types.text.styling.StyledInfo;
import sonar.logistics.core.tiles.displays.info.types.text.styling.StyledStringLine;
import sonar.logistics.core.tiles.displays.info.types.text.utils.StyledStringEditor;
import sonar.logistics.core.tiles.displays.info.types.text.utils.TextSelection;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class GuiEditStyledStrings extends GuiStyledStringFunctions implements IInfoReferenceRequirementGui, IHyperlinkRequirementGui {
	public long lastCursorClick = -1;
	public boolean isDragging = false;

	public GuiEditStyledStrings(StyledTextElement text, TileAbstractDisplay display) {
		super(text, display);
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
			this.buttonList.add(new TextColourButton(this, 16 + i, guiLeft + 2 + i * 14, guiTop + 210, format) {
				public boolean isSelected() {
					return GuiLogistics.getCurrentColour() == colourRGB;
				}
			});
		}
		this.buttonList.add(new CustomColourButton(this, 15, guiLeft + 8 + 16 * 14, guiTop + 210, "Configure Custom Colour") {
			public boolean isSelected() {
				return false;
			}
		});
		
		scaling_scroller = new SonarScroller(this.guiLeft + 90, this.guiTop + 151, 16, 80);
		scaling_scroller.setOrientation(ScrollerOrientation.HORIZONTAL);
		setScalingScroller((float) c.percentageScale);

		spacing_scroller = new SonarScroller(this.guiLeft + 90, this.guiTop + 151 + 20, 16, 80);
		spacing_scroller.setOrientation(ScrollerOrientation.HORIZONTAL);

		scaling_field = new SonarTextField(0, fontRenderer, 20, 153, 40, 11);
		scaling_field.setDigitsOnly(true);
		scaling_field.setMaxStringLength(3);
		scaling_field.setText(String.valueOf((int) (scaling_scroller.currentScroll * 100)));
		fieldList.add(scaling_field);

		spacing_field = new SonarTextField(1, fontRenderer, 20, 153 + 20, 40, 11);
		spacing_field.setDigitsOnly(true);
		spacing_field.setMaxStringLength(3);
		spacing_field.setText(String.valueOf((int) (spacing_scroller.currentScroll * 100)));
		fieldList.add(spacing_field);
		
		this.setSpacingScroller(text.spacing / 50);
		this.cursorPosition.setYToLast(text);
		this.cursorPosition.setXToLast(text);
	}

	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button instanceof TextColourButton) {
			changeSelectedColour(((TextColourButton) button).colour);
			return;
		}
		if (button instanceof CustomColourButton) {
			FMLCommonHandler.instance().showGuiScreen(IGuiOrigin.withOrigin(new GuiColourSelection(inventorySlots, entity), this));
			return;
		}
		if (button instanceof SpecialFormatButton) {
			toggleSpecialFormatting(((SpecialFormatButton) button).specialFormat);
			return;
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
				setTextColourOnSelected(GuiLogistics.getCurrentColour());
				break;
			case 9:
				if (cursorPosition.validPosition()) {
					FMLClientHandler.instance().showGuiScreen(new GuiInfoReferenceSource(new InfoReferenceRequest(this, 1), text.getGSI(), this.inventorySlots));
				}
				break;
			case 10:
				if (cursorPosition.validPosition()) {
					FMLClientHandler.instance().showGuiScreen(new GuiHyperlinkAdd(new HyperlinkRequest(this), text.getGSI(), this.inventorySlots));
				}
				break;
			case 11:
				// action
				break;
			}
		}
	}

	@Override
	public void setSpacingScroller(float scaling) {
		// spacing_scroller.currentScroll = scaling;
		// text.spacing = (int) (scaling * 50);
		/// c.updateActualScaling();
	}

	@Override
	public void onReferenceRequirementCompleted(List<InfoReference> selected) {
		if (selected.isEmpty()) {
			return;
		}
		List newStrings = new ArrayList<>();
		selected.forEach(ref -> newStrings.add(new StyledInfo(ref.uuid, ref.refType, createStylingFromEnabled())));
		StyledStringEditor.addStyledStrings(text, cursorPosition, newStrings);
	}

	@Override
	public void onHyperlinkRequirementCompleted(String hyperlink) {
		int id = text.addAction(new ClickHyperlink(hyperlink));

		formatSelections((line, string) -> {
			string.getStyle().setActionID(id);
			return string;
		});
	}

	public boolean doDisplayScreenClick(double clickX, double clickY, int key) {
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
			lastCursorClick = Minecraft.getSystemTime();
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
			} else if (lastCursorClick + 100 < Minecraft.getSystemTime()) {
				isDragging = Mouse.isButtonDown(0);
				lastCursorClick = Minecraft.getSystemTime();
				if (isDragging) {
					selectPosition.setCursor(cursorPosition.x, cursorPosition.y);
				}
			}
		}
		super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException {
		StyledStringLine ss = cursorPosition.validPosition() ? cursorPosition.getTypingLine(text) : null;
		boolean triggered = HotKeyFunctions.checkFunction(this, ss, c, i);
		GuiActions.UPDATE_TEXT_SCALING.trigger(this);
		if (triggered) {
			return;
		}
		super.keyTyped(c, i);
	}
	/* public static String getClipboardString() { try { Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents((Object) null); if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) { return (String) transferable.getTransferData(DataFlavor.stringFlavor); } } catch (Exception var1) { ; } return ""; } /** Stores the given string in the system clipboard public static void setClipboardString(String copyText) { if (!StringUtils.isEmpty(copyText)) { try { StringSelection stringselection = new StringSelection(copyText); Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, (ClipboardOwner) null); } catch (Exception var2) { ; } } } */

}