package sonar.logistics.core.tiles.displays.info.types.fluids;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import sonar.core.client.gui.IGuiOrigin;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMDisplayElement;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.gsi.packets.GSIClickPacketHelper;
import sonar.logistics.core.tiles.displays.info.InfoRenderHelper;
import sonar.logistics.core.tiles.displays.info.elements.AbstractInfoElement;
import sonar.logistics.core.tiles.displays.info.elements.base.ElementFillType;
import sonar.logistics.core.tiles.displays.info.elements.base.IClickableElement;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

@ASMDisplayElement(id = ElementNetworkFluid.REGISTRY_NAME, modid = PL2Constants.MODID)
public class ElementNetworkFluid extends AbstractInfoElement<InfoNetworkFluid> implements IClickableElement {

	public int text_colour = 16777215;
	
	public ElementNetworkFluid() {
		super();
	}
	
	public ElementNetworkFluid(InfoUUID uuid) {
		super(uuid);
	}

	public void render(InfoNetworkFluid info) {
		GL11.glPushMatrix();
		GlStateManager.disableLighting();
		GL11.glTranslated(0, 0, 0.001);
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(info.getFluidStack().getFluid().getStill().toString());
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		InfoRenderHelper.renderProgressBarWithSprite(sprite, getActualScaling()[WIDTH], getActualScaling()[HEIGHT], info.fluidStack.getObject().stored, info.fluidStack.getObject().capacity);
		GlStateManager.enableLighting();
		GL11.glPopMatrix();
	}

	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
		this.info = getGSI().getCachedInfo(uuid);
		if (info != null && isType(info)) {
			InfoNetworkFluid stack = (InfoNetworkFluid) info;
			int networkID = stack.getNetworkSource();
			GSIClickPacketHelper.sendGSIClickPacket(GSIClickPacketHelper.createFluidClickPacket(stack.getStoredStack(), networkID), getHolder().getContainer(), click);
	}
		return -1;
	}

	@Override
	public Object getClientEditGui(TileAbstractDisplay obj, Object origin, World world, EntityPlayer player) {
		return IGuiOrigin.withOrigin(new GuiEditNetworkFluid(this, obj), origin);
	}

	@Override
	public boolean isType(IInfo info) {
		return info instanceof InfoNetworkFluid;
	}

	@Override
	public ElementFillType getFillType() {
		return ElementFillType.FILL_CONTAINER;
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		text_colour = nbt.getInteger("colour");
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		nbt.setInteger("colour", text_colour);
		return nbt;
	}

	public static final String REGISTRY_NAME = "n_fluid";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

}
