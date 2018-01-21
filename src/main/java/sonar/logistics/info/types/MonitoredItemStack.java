package sonar.logistics.info.types;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.SyncNBTAbstract;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.info.IAdvancedClickableInfo;
import sonar.logistics.api.info.IBasicClickableInfo;
import sonar.logistics.api.info.IComparableInfo;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.IJoinableInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.render.DisplayInfo;
import sonar.logistics.api.info.render.IDisplayInfo;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.IMonitoredValueInfo;
import sonar.logistics.api.lists.values.ItemCount;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.api.register.RegistryType;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.api.tiles.signaller.ComparableObject;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.helpers.InteractionHelper;
import sonar.logistics.networking.handlers.ItemNetworkHandler;

@LogicInfoType(id = MonitoredItemStack.id, modid = PL2Constants.MODID)
public class MonitoredItemStack extends BaseInfo<MonitoredItemStack> implements IProvidableInfo<MonitoredItemStack>, IJoinableInfo<MonitoredItemStack>, IAdvancedClickableInfo, INameableInfo<MonitoredItemStack>, IComparableInfo<MonitoredItemStack>, IMonitoredValueInfo<MonitoredItemStack> {

	public static final String id = "item";
	private final SyncNBTAbstract<StoredItemStack> itemStack = new SyncNBTAbstract<StoredItemStack>(StoredItemStack.class, 0);
	private final SyncTagType.INT networkID = (INT) new SyncTagType.INT(1).setDefault(-1);
	{
		syncList.addParts(itemStack, networkID);
	}

	public MonitoredItemStack() {}

	public MonitoredItemStack(StoredItemStack stack, int networkID) {
		this(stack);
		this.networkID.setObject(networkID);
	}

	public MonitoredItemStack(StoredItemStack stack) {
		this.itemStack.setObject(stack);
	}

	@Override
	public boolean isIdenticalInfo(MonitoredItemStack info) {
		return getStoredStack().equals(info.getStoredStack());
	}

	@Override
	public boolean isMatchingInfo(MonitoredItemStack info) {
		return getStoredStack().equalStack(info.getStoredStack().getItemStack()) && networkID.getObject().equals(networkID.getObject());
	}

	@Override
	public boolean isMatchingType(IInfo info) {
		return info instanceof MonitoredItemStack;
	}

	@Override
	public ItemNetworkHandler getHandler() {
		return ItemNetworkHandler.INSTANCE;
	}

	@Override
	public boolean canJoinInfo(MonitoredItemStack info) {
		return isMatchingInfo(info);
	}

	@Override
	public IJoinableInfo joinInfo(MonitoredItemStack info) {
		itemStack.getObject().add(info.itemStack.getObject());
		return this;
	}

	@Override
	public boolean isValid() {
		return itemStack.getObject() != null && itemStack.getObject().item != null;
	}

	@Override
	public String getID() {
		return id;
	}

	public String toString() {
		if (itemStack.getObject() != null)
			return itemStack.getObject().toString();
		return super.toString() + " : NULL";
	}

	public ItemStack getItemStack() {
		return this.itemStack.getObject().getItemStack();
	}

	public StoredItemStack getStoredStack() {
		return this.itemStack.getObject();
	}

	public long getStored() {
		return this.itemStack.getObject().stored;
	}

	public int getNetworkSource() {
		return networkID.getObject();
	}

	public void setNetworkSource(int id) {
		networkID.setObject(id);
	}

	@Override
	public MonitoredItemStack copy() {
		return new MonitoredItemStack(itemStack.getObject().copy(), networkID.getObject());
	}

	@Override
	public void renderInfo(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		if (itemStack.getObject() != null) {
			DisplayType type = container.display.getDisplayType();
			StoredItemStack stack = itemStack.getObject();
			ItemStack item = stack.item;
			GlStateManager.pushAttrib();
			GL11.glPushMatrix();
			GlStateManager.enableDepth();
			GL11.glTranslated(-(1 - width / 2 - 0.0625), -0.68 + height / 2, 0.00);
			GL11.glRotated(180, 0, 1, 0);
			GL11.glScaled(-1, 1, 1);
			double actualScale = type == DisplayType.LARGE ? scale * 3 : scale * 2;
			GL11.glScaled(actualScale, actualScale, 0.01);
			double trans = type == DisplayType.SMALL ? 4 : -(7);
			GL11.glTranslated(-8, -8, 0);
			GlStateManager.disableLighting();
			GlStateManager.enablePolygonOffset();
			GlStateManager.doPolygonOffset(-1, -1);
			GlStateManager.enableCull();
			RenderHelper.renderItemIntoGUI(item, 0, 0);
			GlStateManager.disablePolygonOffset();
			GlStateManager.translate(0, 0, 2);
			GlStateManager.depthMask(false);
			RenderHelper.renderStoredItemStackOverlay(item, 0, 0, 0, "" + stack.stored, false);
			GlStateManager.depthMask(true);			
			GlStateManager.disableDepth();
			GL11.glPopMatrix();
			GlStateManager.popAttrib();
		}
	}

	@Override
	public String getClientIdentifier() {
		return "Item: " + (itemStack.getObject() != null && itemStack.getObject().getItemStack() != null ? itemStack.getObject().getItemStack().getDisplayName() : "ITEMSTACK");
	}

	@Override
	public String getClientObject() {
		return itemStack.getObject() != null ? "" + itemStack.getObject().stored : "ERROR";
	}

	@Override
	public String getClientType() {
		return "item";
	}

	@Override
	public List<ComparableObject> getComparableObjects(List<ComparableObject> objects) {
		StoredItemStack stack = itemStack.getObject();
		objects.add(new ComparableObject(this, "Stored", stack.stored));
		objects.add(new ComparableObject(this, "Damage", stack != null ? stack.getItemDamage() : -1));
		objects.add(new ComparableObject(this, "NBT", stack.item.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound()));
		return objects;

	}

	@Override
	public RegistryType getRegistryType() {
		return RegistryType.TILE; // may want to be something different, may need to be configurable - tile would be the most likely, but not necessarily correct
	}

	@Override
	public MonitoredItemStack setRegistryType(RegistryType type) {
		return this;
	}

	@Override
	public void setFromReturn(LogicPath path, Object returned) {}

	public static MonitoredItemStack findItemStack(List<MonitoredItemStack> stacks, ItemStack item) {
		for (MonitoredItemStack i : stacks) {
			if (i.getStoredStack().equalStack(item)) {
				return i;
			}
		}
		return null;
	}

	@Override
	public IMonitoredValue<MonitoredItemStack> createMonitoredValue() {
		return new ItemCount(this);
	}

	@Override
	public boolean canClick(DisplayScreenClick click, DisplayInfo renderInfo, EntityPlayer player, EnumHand hand) {
		return InteractionHelper.canBeClickedStandard(renderInfo, click);
	}

	@Override
	public NBTTagCompound createClickPacket(DisplayScreenClick click, DisplayInfo renderInfo, EntityPlayer player, EnumHand hand) {
		return itemStack.writeData(new NBTTagCompound(), SyncType.SAVE);
	}

	@Override
	public void runClickPacket(DisplayScreenClick click, DisplayInfo displayInfo, EntityPlayer player, NBTTagCompound clickTag) {
		MonitoredItemStack clicked = NBTHelper.instanceNBTSyncable(MonitoredItemStack.class, clickTag);
		InfoHelper.screenItemStackClicked(networkID.getObject(), clicked != null ? clicked.getStoredStack() : null, click, displayInfo, player, clickTag);
	}

}