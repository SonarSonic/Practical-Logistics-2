package sonar.logistics.api.displays.elements.types;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.client.gui.IGuiOrigin;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.elements.AbstractInfoElement;
import sonar.logistics.api.displays.elements.IClickableElement;
import sonar.logistics.api.displays.tiles.DisplayScreenClick;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.client.gsi.GSIClickPacketHelper;
import sonar.logistics.client.gui.display.GuiEditNetworkItem;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.info.types.MonitoredItemStack;

import static net.minecraft.client.renderer.GlStateManager.*;

@DisplayElementType(id = NetworkItemElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class NetworkItemElement extends AbstractInfoElement<MonitoredItemStack> implements IClickableElement {

	public int text_colour = 16777215;

	public NetworkItemElement() {
		super();
	}

	public NetworkItemElement(InfoUUID uuid) {
		super(uuid);
	}

	public void render(MonitoredItemStack info) {
		disableLighting();
		scale(1, 1, 0.1); // compresses the item on the z axis
		rotate(180, 0, 1, 0); // flips the item
		scale(-1, 1, 1);
		RenderHelper.renderItemIntoGUI(info.getItemStack(), 0, 0);
		translate(0, 0, 2);
		depthMask(false);
		RenderHelper.renderStoredItemStackOverlay(info.getItemStack(), 0, 0, 0, text_colour, "" + info.getStored(), false);
		depthMask(true);
		enableLighting();
	}

	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
		this.info = getGSI().getCachedInfo(uuid);
		if (info != null && isType(info)) {
			MonitoredItemStack stack = (MonitoredItemStack) info;
			int networkID = stack.getNetworkSource();
			GSIClickPacketHelper.sendGSIClickPacket(GSIClickPacketHelper.createItemClickPacket(stack.getStoredStack(), networkID), getHolder().getContainer(), click);
		}
		return -1;
	}

	@Override
	public Object getClientEditGui(TileAbstractDisplay obj, Object origin, World world, EntityPlayer player) {
		return IGuiOrigin.withOrigin(new GuiEditNetworkItem(this, obj), origin);
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

	@Override
	public boolean isType(IInfo info) {
		return info instanceof MonitoredItemStack;
	}

	@Override
	public int[] createUnscaledWidthHeight() {
		return new int[] { 16, 16 };
	}

	public static final String REGISTRY_NAME = "n_item";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

}
