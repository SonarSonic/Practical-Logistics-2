package sonar.logistics.api.displays.elements.types;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.client.gui.IGuiOrigin;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.elements.AbstractInfoElement;
import sonar.logistics.api.displays.elements.ElementFillType;
import sonar.logistics.api.displays.elements.IClickableElement;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.gsi.GSIClickPacketHelper;
import sonar.logistics.client.gui.display.GuiEditNetworkFluid;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.MonitoredFluidStack;
import sonar.logistics.info.types.MonitoredItemStack;

@DisplayElementType(id = NetworkFluidElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class NetworkFluidElement extends AbstractInfoElement<MonitoredFluidStack> implements IClickableElement {

	public int text_colour = 16777215;
	
	public NetworkFluidElement() {
		super();
	}
	
	public NetworkFluidElement(InfoUUID uuid) {
		super(uuid);
	}

	public void render(MonitoredFluidStack info) {
		GL11.glPushMatrix();
		GlStateManager.disableLighting();
		GL11.glTranslated(0, 0, 0.001);
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(info.getFluidStack().getFluid().getStill().toString());
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		InfoRenderer.renderProgressBarWithSprite(sprite, getActualScaling()[WIDTH], getActualScaling()[HEIGHT], info.fluidStack.getObject().stored, info.fluidStack.getObject().capacity);
		GlStateManager.enableLighting();
		GL11.glPopMatrix();
	}

	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
		this.info = getGSI().getCachedInfo(uuid);
		if (info != null && isType(info)) {
			MonitoredFluidStack stack = (MonitoredFluidStack) info;
			int networkID = stack.getNetworkSource();
			GSIClickPacketHelper.sendGSIClickPacket(GSIClickPacketHelper.createFluidClickPacket(stack == null ? null : stack.getStoredStack(), networkID), getHolder().getContainer(), click);
	}
		return -1;
	}

	@Override
	public Object getClientEditGui(TileAbstractDisplay obj, Object origin, World world, EntityPlayer player) {
		return IGuiOrigin.withOrigin(new GuiEditNetworkFluid(this, obj), origin);
	}

	@Override
	public boolean isType(IInfo info) {
		return info instanceof MonitoredFluidStack;
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
