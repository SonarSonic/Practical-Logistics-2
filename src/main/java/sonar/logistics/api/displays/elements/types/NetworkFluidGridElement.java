package sonar.logistics.api.displays.elements.types;

import static net.minecraft.client.renderer.GlStateManager.*;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gsi.GSIClickPacketHelper;
import sonar.logistics.client.gui.display.GuiEditNetworkFluidlist;
import sonar.logistics.client.gui.display.GuiEditNetworkItemlist;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.LogicInfoList;
import sonar.logistics.info.types.MonitoredFluidStack;

@DisplayElementType(id = NetworkFluidGridElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class NetworkFluidGridElement extends NetworkGridElement<MonitoredFluidStack> {

	public NetworkFluidGridElement() {
		super();
	}

	public NetworkFluidGridElement(InfoUUID uuid) {
		super(uuid);
	}

	public double getRenderWidth() {
		return Math.min(element_size, Math.min(getActualScaling()[0], getActualScaling()[1]));
	}

	public double getRenderHeight() {
		return Math.min(element_size, Math.min(getActualScaling()[0], getActualScaling()[1]));
	}

	public void renderGridElement(MonitoredFluidStack stack, int index) {
		// disableLighting();
		
		grid_fill_percentage = 0.9;
		double elementWidth = width * grid_fill_percentage;
		double elementHeight = height * grid_fill_percentage;
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(stack.getFluidStack().getFluid().getStill().toString());
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		
		sonar.core.helpers.RenderHelper.saveBlendState();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
		DisplayElementHelper.drawRect(0, 0, elementWidth, elementHeight, LogisticsColours.backgroundColour.getRGB());

		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 1);
		GlStateManager.disableBlend();
		GlStateManager.color(1, 1, 1, 1);
		disableLighting();
		sonar.core.helpers.RenderHelper.restoreBlendState();
		
		translate(0, 0, 0.001);
		InfoRenderer.renderProgressBarWithSprite(sprite, elementWidth, elementHeight, stack.getStored(), stack.getStoredStack().capacity);
		translate(0, 0, 0.001);
		InfoRenderer.renderCenteredStringsWithUniformScaling(Lists.newArrayList(stack.getClientIdentifier(), stack.getClientObject()), elementWidth, elementHeight, 10, 0.75, text_colour);
		
	}

	public void onGridElementClicked(DisplayScreenClick click, LogicInfoList list, @Nullable MonitoredFluidStack stack) {
		int networkID = (stack == null || stack.getNetworkSource() == -1) ? list.networkID.getObject() : stack.getNetworkSource();
		GSIClickPacketHelper.sendGSIClickPacket(GSIClickPacketHelper.createFluidClickPacket(stack == null ? null : stack.getStoredStack(), networkID), getHolder().getContainer(), click);
	}

	@Override
	public Object getClientEditGui(TileAbstractDisplay obj, Object origin, World world, EntityPlayer player) {
		return GuiSonar.withOrigin(new GuiEditNetworkFluidlist(this, obj), origin);
	}

	public static final String REGISTRY_NAME = "n_fluid_l";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}
}
