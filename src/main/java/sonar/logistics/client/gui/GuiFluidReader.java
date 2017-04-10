package sonar.logistics.client.gui;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.client.gui.GuiHelpOverlay;
import sonar.core.client.gui.SonarTextField;
import sonar.core.network.FlexibleGuiHandler;
import sonar.logistics.PL2Constants;
import sonar.logistics.PL2Translate;
import sonar.logistics.api.tiles.readers.FluidReader;
import sonar.logistics.api.tiles.readers.FluidReader.Modes;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.gui.generic.GuiSelectionGrid;
import sonar.logistics.common.containers.ContainerFluidReader;
import sonar.logistics.common.multiparts.FluidReaderPart;
import sonar.logistics.info.types.MonitoredFluidStack;

public class GuiFluidReader extends GuiSelectionGrid<MonitoredFluidStack> {

	public static final ResourceLocation stackBGround = new ResourceLocation(PL2Constants.MODID + ":textures/gui/inventoryReader_stack.png");
	public static final ResourceLocation clearBGround = new ResourceLocation(PL2Constants.MODID + ":textures/gui/inventoryReader_clear.png");

	public FluidReaderPart part;
	private SonarTextField slotField;
	private SonarTextField searchField;
	public static final int STACK = 0, POS = 1, INV = 2, STORAGE = 3;

	public EntityPlayer player;

	public GuiFluidReader(FluidReaderPart part, EntityPlayer player) {
		super(new ContainerFluidReader(part, player), part);
		this.part = part;
		this.player = player;
	}

	public FluidReader.Modes getSetting() {
		return part.setting.getObject();
	}

	public void initGui() {
		super.initGui();
		initButtons();
		switch (getSetting()) {
		case POS:
			slotField = new SonarTextField(2, this.fontRendererObj, 63, 10, 32, 14);
			slotField.setMaxStringLength(7);
			slotField.setText("" + part.posSlot.getObject());
			slotField.setDigitsOnly(true);
			fieldList.add(slotField);
			break;
		default:
			break;
		}
		searchField = new SonarTextField(3, this.fontRendererObj, 135, 10, 104, 14);
		searchField.setMaxStringLength(20);
		fieldList.add(searchField);
	}

	public void initButtons() {
		super.initButtons();

		int start = 8;
		this.buttonList.add(new LogisticsButton.CHANNELS(this, 2, guiLeft + 8, guiTop + 9));
		this.buttonList.add(new LogisticsButton.HELP(this, 3, guiLeft + start + 18 * 1, guiTop + 9));
		this.buttonList.add(new LogisticsButton(this, -1, guiLeft + start + 18 * 2, guiTop + 9, 128, 16 * part.setting.getObject().ordinal(), getSetting().getClientName(), getSetting().getDescription()));

		this.buttonList.add(new LogisticsButton(this, 0, guiLeft + xSize - 168 + 18, guiTop + 9, 32, 16 * part.sortingOrder.getObject().ordinal(), PL2Translate.BUTTON_SORTING_ORDER.t(), ""));
		this.buttonList.add(new LogisticsButton(this, 1, guiLeft + xSize - 168 + 18 * 2, guiTop + 9, 64 + 48, 16 * part.sortingType.getObject().ordinal(), part.sortingType.getObject().getClientName(), ""));
	
	}

	public void onTextFieldChanged(SonarTextField field) {
		super.onTextFieldChanged(field);
		if (field == slotField) {
			part.posSlot.setObject(field.getIntegerFromText());
			part.sendByteBufPacket(4);
		}
	}

	public void actionPerformed(GuiButton button) {
		if (button != null) {
			switch (button.id) {
			case -1:
				part.setting.incrementEnum();
				part.sendByteBufPacket(2);
				reset();
				break;
			case 0:
				part.sortingOrder.incrementEnum();
				part.sendByteBufPacket(5);
				initButtons();
				break;
			case 1:
				part.sortingType.incrementEnum();
				part.sendByteBufPacket(6);
				initButtons();
				break;
			case 2:
				FlexibleGuiHandler.changeGui(part, 1, 0, player.getEntityWorld(), player);
				break;
			case 3:
				GuiHelpOverlay.enableHelp = !GuiHelpOverlay.enableHelp;
				reset();
				break;
			}
		}
	}

	@Override
	public MonitoredList<MonitoredFluidStack> getGridList() {
		String search = searchField.getText();
		if (search == null || search.isEmpty() || search.equals(" ") || search.equals(""))
			return part.getMonitoredList();
		else {
			MonitoredList<MonitoredFluidStack> searchList = MonitoredList.<MonitoredFluidStack>newMonitoredList(part.getNetworkID());
			for (MonitoredFluidStack stack : (List<MonitoredFluidStack>) part.getMonitoredList().clone()) {
				StoredFluidStack fluidStack = stack.getStoredStack();
				if (stack != null && fluidStack.fluid != null && fluidStack.fluid.getLocalizedName().toLowerCase().contains(searchField.getText().toLowerCase())) {
					searchList.add(stack);
				}
			}
			return searchList;
		}
	}

	@Override
	public void onGridClicked(MonitoredFluidStack selection, int pos, int button, boolean empty) {
		if (empty) {
			return;
		}
		if (getSetting() == Modes.SELECTED) {
			part.selected.setInfo(selection);
			part.sendByteBufPacket(1);
		}
		if (getSetting() == Modes.POS) {
			List<MonitoredFluidStack> currentList = (List<MonitoredFluidStack>) this.getGridList().clone();
			int position = 0;
			for (MonitoredFluidStack stack : currentList) {
				if (stack != null) {
					if (stack.equals(selection)) {
						String posString = String.valueOf(position);
						slotField.setText(posString);
						part.posSlot.setObject(slotField.getIntegerFromText());
						part.sendByteBufPacket(4);
					}
				}
				position++;
			}

		}
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		if (this.getSetting() == FluidReader.Modes.SELECTED) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(playerInv);
			drawTexturedModalRect(guiLeft + 62, guiTop + 8, 0, 0, 18, 18);
		}
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
	}

	public void preRender() {
		final int br = 16 << 20 | 16 << 4;
		final int var11 = br % 65536;
		final int var12 = br / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, var11 * 0.8F, var12 * 0.8F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();
		if (getGridList() != null) {
			GlStateManager.disableLighting();
			net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
		}

	}

	public void postRender() {
		if (this.getSetting() == FluidReader.Modes.SELECTED && part.selected.getMonitoredInfo() != null) {
			MonitoredFluidStack stack = part.selected.getMonitoredInfo();
			if (stack != null) {
				StoredFluidStack fluidStack = stack.getStoredStack();
				final int br = 16 << 20 | 16 << 4;
				final int var11 = br % 65536;
				final int var12 = br / 65536;
				GL11.glPushMatrix();
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(fluidStack.fluid.getFluid().getStill().toString());
				Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				this.drawTexturedModalRect(63, 9, sprite, 16, 16);
				GL11.glPopMatrix();
			}
		}
	}

	@Override
	public void renderSelection(MonitoredFluidStack selection, int x, int y, int slot) {
		StoredFluidStack fluidStack = selection.getStoredStack();
		if (fluidStack.fluid != null) {
			GL11.glPushMatrix();
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(fluidStack.fluid.getFluid().getStill().toString());
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			drawTexturedModalRect(13 + (x * 18), 32 + (y * 18), sprite, 16, 16);
			GL11.glPopMatrix();
		}
	}

	@Override
	public void renderToolTip(MonitoredFluidStack selection, int x, int y) {
		StoredFluidStack fluidStack = selection.getStoredStack();
		List list = Lists.newArrayList();
		list.add(fluidStack.fluid.getFluid().getLocalizedName(fluidStack.fluid));
		if (fluidStack.stored != 0) {
			list.add(TextFormatting.GRAY + (String) PL2Translate.BUTTON_STORED.t() + ": " + fluidStack.stored + " mB");
		}
		drawHoveringText(list, x, y, fontRendererObj);
	}

	@Override
	public ResourceLocation getBackground() {
		if (getSetting() == Modes.SELECTED) {
			return stackBGround;
		}
		return clearBGround;
	}

	@Override
	public void renderStrings(int x, int y) {
	}
}
