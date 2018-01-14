package sonar.logistics.info.types;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.helpers.FontHelper;
import sonar.core.network.sync.SyncNBTAbstract;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.info.IBasicClickableInfo;
import sonar.logistics.api.info.IComparableInfo;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.IJoinableInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.info.render.DisplayInfo;
import sonar.logistics.api.info.render.IDisplayInfo;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.IMonitoredValueInfo;
import sonar.logistics.api.lists.values.FluidCount;
import sonar.logistics.api.tiles.signaller.ComparableObject;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.networking.handlers.FluidNetworkHandler;

@LogicInfoType(id = MonitoredFluidStack.id, modid = PL2Constants.MODID)
public class MonitoredFluidStack extends BaseInfo<MonitoredFluidStack> implements IJoinableInfo<MonitoredFluidStack>, INameableInfo<MonitoredFluidStack>, IBasicClickableInfo, IComparableInfo<MonitoredFluidStack>, IMonitoredValueInfo<MonitoredFluidStack> {

	public static final String id = "fluid";
	private SyncNBTAbstract<StoredFluidStack> fluidStack = new SyncNBTAbstract<StoredFluidStack>(StoredFluidStack.class, 0);
	private final SyncTagType.INT networkID = (INT) new SyncTagType.INT(1).setDefault(-1);

	{
		syncList.addParts(fluidStack, networkID);
	}

	public MonitoredFluidStack() {}

	public MonitoredFluidStack(StoredFluidStack stack) {
		this.fluidStack.setObject(stack);
	}

	public MonitoredFluidStack(StoredFluidStack stack, int networkID) {
		this.fluidStack.setObject(stack);
		this.networkID.setObject(networkID);
	}

	@Override
	public boolean isIdenticalInfo(MonitoredFluidStack info) {
		return getStoredStack().equals(info.getStoredStack()) && networkID.getObject().equals(info.networkID.getObject());
	}

	@Override
	public boolean isMatchingInfo(MonitoredFluidStack info) {
		return getStoredStack().equalStack(info.getFluidStack());
	}

	@Override
	public boolean isMatchingType(IInfo info) {
		return info instanceof MonitoredFluidStack;
	}

	@Override
	public FluidNetworkHandler getHandler() {
		return FluidNetworkHandler.INSTANCE;
	}

	@Override
	public boolean canJoinInfo(MonitoredFluidStack info) {
		return isMatchingInfo(info);
	}

	@Override
	public IJoinableInfo joinInfo(MonitoredFluidStack info) {
		fluidStack.getObject().add(info.fluidStack.getObject());
		return this;
	}

	@Override
	public boolean isValid() {
		return fluidStack.getObject() != null && fluidStack.getObject().fluid != null;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public MonitoredFluidStack copy() {
		return new MonitoredFluidStack(fluidStack.getObject().copy(), networkID.getObject());
	}

	@Override
	public void renderInfo(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		FluidStack stack = fluidStack.getObject().fluid;
		if (stack != null) {
			GL11.glPushMatrix();
			GL11.glPushMatrix();
			GlStateManager.disableLighting();
			GL11.glTranslated(-1, -0.0625 * 12, +0.004);
			TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(stack.getFluid().getStill().toString());
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			InfoRenderer.renderProgressBarWithSprite(sprite, width, height, scale, fluidStack.getObject().stored, fluidStack.getObject().capacity);
			GlStateManager.enableLighting();
			GL11.glTranslated(0, 0, -0.001);
			GL11.glPopMatrix();
			InfoRenderer.renderNormalInfo(container.display.getDisplayType(), width, height, scale, displayInfo.getFormattedStrings());
			GL11.glPopMatrix();
		}
	}

	@Override
	public String getClientIdentifier() {
		return (fluidStack.getObject() != null && fluidStack.getObject().fluid != null ? fluidStack.getObject().fluid.getLocalizedName() : "FLUIDSTACK");
	}

	@Override
	public String getClientObject() {
		return fluidStack.getObject() != null ? "" + FontHelper.formatFluidSize(fluidStack.getObject().stored) : "ERROR";
	}

	@Override
	public String getClientType() {
		return "fluid";
	}

	@Override
	public boolean onStandardClick(TileAbstractDisplay part, DisplayInfo renderInfo, BlockInteractionType type, World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (InfoHelper.canBeClickedStandard(part, renderInfo, type, world, pos, state, player, hand, facing, hitX, hitY, hitZ)) {
			if (!player.getEntityWorld().isRemote) {
				InfoHelper.onScreenFluidStackClicked(part, renderInfo, type, fluidStack.getObject(), world, pos, state, player, hand, facing, hitX, hitY, hitZ);
			}
			return true;
		}
		return false;
	}

	@Override
	public List<ComparableObject> getComparableObjects(List<ComparableObject> objects) {
		StoredFluidStack stack = fluidStack.getObject();
		objects.add(new ComparableObject(this, "Stored", stack.stored));
		objects.add(new ComparableObject(this, "Capacity", stack.capacity));
		return objects;
	}

	public String toString() {
		return fluidStack.getObject().toString();
	}

	public FluidStack getFluidStack() {
		return this.fluidStack.getObject().getFullStack();
	}

	public StoredFluidStack getStoredStack() {
		return this.fluidStack.getObject();
	}

	public long getStored() {
		return this.fluidStack.getObject().stored;
	}

	@Override
	public IMonitoredValue<MonitoredFluidStack> createMonitoredValue() {
		return new FluidCount(this);
	}

}
