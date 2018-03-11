package sonar.logistics.api.displays.elements.types;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.elements.AbstractInfoElement;
import sonar.logistics.api.displays.elements.ElementFillType;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.ProgressInfo;

@DisplayElementType(id = ProgressBarElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class ProgressBarElement extends AbstractInfoElement<ProgressInfo> {

	public ProgressBarElement() {
		super();
	}

	public ProgressBarElement(InfoUUID uuid) {
		super(uuid);
	}

	@Override
	public void render(ProgressInfo info) {		
		GL11.glPushMatrix();
		GlStateManager.disableLighting();
		GL11.glTranslated(0, 0, 0.001);
		Minecraft.getMinecraft().getTextureManager().bindTexture(LogisticsColours.colourTex1);
		double num1 = (info.compare == 1 ? info.secondNum : info.firstNum);
		double num2 = (info.compare == 1 ? info.firstNum : info.secondNum);
		InfoRenderer.renderProgressBar(getActualScaling()[WIDTH], getActualScaling()[HEIGHT], num1 < 0 ? 0 : num1, num2);
		GlStateManager.enableLighting();
		GL11.glPopMatrix();
	}

	@Override
	public Object getClientEditGui(TileAbstractDisplay obj, Object origin, World world, EntityPlayer player) {
		//FIXME
		return null;
	}

	@Override
	public boolean isType(IInfo info) {
		return info instanceof ProgressInfo;
	}

	@Override
	public ElementFillType getFillType() {
		return ElementFillType.FILL_CONTAINER;
	}

	public static final String REGISTRY_NAME = "p_bar";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}


}
