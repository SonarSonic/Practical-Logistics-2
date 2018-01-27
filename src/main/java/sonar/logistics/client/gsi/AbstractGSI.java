package sonar.logistics.client.gsi;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.render.DisplayInfo;
import sonar.logistics.api.info.render.IDisplayInfo;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.helpers.InteractionHelper;
import sonar.logistics.packets.PacketGSIClick;

@SideOnly(Side.CLIENT)
public abstract class AbstractGSI<I extends IInfo> extends Gui implements IGSI<I> {

	public static final ResourceLocation BUTTON_TEX = new ResourceLocation(PL2Constants.MODID + ":textures/gui/filter_buttons.png");
	public InfoContainer container;
	public DisplayInfo renderInfo;
	public EntityPlayer player;
	public World world;
	public List<GSIButton> buttons;
	public Map<Integer, Long> buttonClickCount = Maps.newHashMap();

	public AbstractGSI() {}
	
	public void initGSI(DisplayInfo renderInfo){
		this.renderInfo = renderInfo;
		this.container = renderInfo.container;
		this.player = Minecraft.getMinecraft().player;
		this.world = player.getEntityWorld();
	}

	@Override
	public void resetGSI() {
		List<GSIButton> newButtonList = Lists.newArrayList();
		initButtons(newButtonList);
		buttons = newButtonList;
	}

	@Override
	public void renderGSIBackground(I info, InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		bindTexture(BUTTON_TEX);
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GL11.glTranslated(-1, -0.0625 * 12, +0.004);

		for (GSIButton button : buttons) {
			GlStateManager.pushMatrix();
			int texPixelWidth = 16;
			double pixel = 0.0625;
			RenderHelper.drawModalRectWithCustomSizedTexture(button.posX, button.posY, (double) button.buttonX / ((double) texPixelWidth / (button.width / pixel)), (double) button.buttonY / ((double) texPixelWidth / (button.height / pixel)), button.width, button.height, button.width / pixel, button.width / pixel);
			if (shouldRenderClick(button.buttonID)) {
				/** renders a blue select box around the button if has been clicked */
				RenderHelper.drawModalRectWithCustomSizedTexture(button.posX, button.posY, (double) 15 / ((double) texPixelWidth / (button.width / pixel)), (double) 15 / ((double) texPixelWidth / (button.height / pixel)), button.width, button.height, button.width / pixel, button.width / pixel);
			}
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();
	}

	private boolean shouldRenderClick(int buttonID) {
		Long click = buttonClickCount.get(buttonID);
		if (click != null) {
			boolean canDelete = (System.currentTimeMillis()-click) > 1000; //holds for one second
			if (canDelete) {
				buttonClickCount.remove(buttonID);
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public void renderGSIForeground(I info, InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		// nothing needed at the moment
	}

	@Override
	public boolean canInteractWith(I info, DisplayScreenClick click, EnumHand hand) {
		return InteractionHelper.canBeClickedStandard(renderInfo, click);
	}
	
	@Override
	public void onGSIClicked(I info, DisplayScreenClick click, EnumHand hand) {
		for (GSIButton button : buttons) {
			if (button.isClickOver(click.clickX, click.clickY)) {
				onButtonClicked(button, click, hand);
				buttonClickCount.put(button.buttonID, System.currentTimeMillis());
				// SimpleProfiler.start(key);
			}
		}
	}
	
	public void sendGSIPacket(NBTTagCompound tag, I info, DisplayScreenClick click, EnumHand hand){
		if (!tag.hasNoTags()){
			PL2.network.sendToServer(new PacketGSIClick(renderInfo.getInfoPosition(), click, renderInfo.getInfoUUID(), tag));
		}
	}

	public void initButtons(List<GSIButton> buttons) {}

	public void bindTexture(ResourceLocation resource) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(resource);
	}
}
