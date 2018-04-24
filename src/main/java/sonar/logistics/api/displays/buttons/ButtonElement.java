package sonar.logistics.api.displays.buttons;

import static net.minecraft.client.renderer.GlStateManager.*;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.elements.AbstractDisplayElement;
import sonar.logistics.api.displays.elements.IClickableElement;
import sonar.logistics.api.displays.elements.ILookableElement;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.LogisticsColours;

@DisplayElementType(id = ButtonElement.REGISTRY_NAME, modid = PL2Constants.MODID)
//only works for square buttons atm
public class ButtonElement extends AbstractDisplayElement implements IClickableElement, ILookableElement {

	public static final ResourceLocation BUTTON_TEX = new ResourceLocation(PL2Constants.MODID + ":textures/gui/filter_buttons.png");
	public int buttonID;
	// the x/y position of this button on the filter button texture, (counted in )
	public int buttonX, buttonY;
	public String hoverString;

	public int unscaledWidth = 4;
	public int unscaledHeight = 4;

	public ButtonElement() {}

	public ButtonElement(int buttonID, int buttonX, int buttonY, String hoverString) {
		this.buttonID = buttonID;
		this.buttonX = buttonX;
		this.buttonY = buttonY;
		this.hoverString = hoverString;
	}

	@Override
	public void render() {
		Minecraft.getMinecraft().getTextureManager().bindTexture(BUTTON_TEX);
		GlStateManager.disableLighting();
		RenderHelper.drawModalRectWithCustomSizedTexture(0, 0, (buttonX * 16) * 0.0625, (buttonY * 16) * 0.0625, 1, 1, 16, 16);
		
		if (isPlayerLooking()) {
			/* renders a blue select box around the button if has been clicked */
			RenderHelper.drawModalRectWithCustomSizedTexture(0, 0, (15 * 16) * 0.0625, (15 * 16) * 0.0625, 1, 1, 16, 16);

			double displayScale = 0.008;// FIXME
			pushMatrix();
			translate(getActualScaling()[WIDTH] / getActualScaling()[SCALE], 0, -0.005);
			scale(1 / getActualScaling()[SCALE], 1 / getActualScaling()[SCALE], 1);
			scale(displayScale, displayScale, 1);
			int textWidth = RenderHelper.fontRenderer.getStringWidth(hoverString);

			RenderHelper.saveBlendState();

			disableTexture2D();
			enableBlend();
			enableAlpha();
			disableLighting();
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			int left = 0, top = 0, right = textWidth, bottom = 8, colour = LogisticsColours.blue_overlay.getRGB();
			GuiSonar.drawTransparentRect(left, top, right, bottom, colour * 4);
			translate(0, 0, -0.004);
			FontHelper.text(hoverString, 0, 0, -1);

			enableLighting();
			disableAlpha();
			disableBlend();
			enableTexture2D();
			RenderHelper.restoreBlendState();
			popMatrix();

		}
	}

	@Override
	public String getRepresentiveString() {
		return hoverString.isEmpty() ? "BUTTON" : hoverString;
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		return nbt;
	}

	@Override
	public int[] createUnscaledWidthHeight() {
		return new int[] { 1, 1 };
	}

	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
		/* switch(buttonID){ case 0: GSIHelper.sendGSIPacket(GSIHelper.createBasicPacket(GSIPackets.SOURCE_BUTTON), getHolder().getContainer(), click); return true; } */
		return -1;
	}

	public static final String REGISTRY_NAME = "button_element";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

}
