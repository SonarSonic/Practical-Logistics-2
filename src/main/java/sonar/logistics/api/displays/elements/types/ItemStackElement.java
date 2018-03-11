package sonar.logistics.api.displays.elements.types;

import static net.minecraft.client.renderer.GlStateManager.scale;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.elements.AbstractDisplayElement;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;

@DisplayElementType(id = ItemStackElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class ItemStackElement extends AbstractDisplayElement {

	public StoredItemStack stack;

	public ItemStackElement(StoredItemStack stack) {
		super();
		this.stack = stack;
	}

	public void render() {
		scale(1, 1 , 0.1); //compresses the item on the z axis
		//enablePolygonOffset();
		GL11.glRotated(180, 0, 1, 0); // flips the item
		GL11.glScaled(-1, 1, 1);
		//doPolygonOffset(-1, -1);
		RenderHelper.renderItemIntoGUI(getItem(), 0, 0);
		//disablePolygonOffset();
		GlStateManager.translate(0, 0, 2);
		GlStateManager.depthMask(false);
		RenderHelper.renderStoredItemStackOverlay(getItem(), 0, 0, 0, "" + stack.stored, false);
		GlStateManager.depthMask(true);
		
		/*
		ItemStack item = stack.item;
		GL11.glPushMatrix();
		GlStateManager.enableDepth();
		GL11.glTranslated(-(1 - width / 2 - 0.0625), -0.68 + height / 2, 0.00);
		GL11.glRotated(180, 0, 1, 0);
		GL11.glScaled(-1, 1, 1);
		GlStateManager.disableLighting();
		GlStateManager.enablePolygonOffset();
		GlStateManager.doPolygonOffset(-1, -1);
		GlStateManager.enableCull();
		RenderHelper.renderItemIntoGUI(item, 0, 0);
		GlStateManager.disablePolygonOffset();
		GlStateManager.translate(0, 0, 2);
		GlStateManager.depthMask(false);
		GlStateManager.depthMask(true);
		GlStateManager.disableDepth();
		GL11.glPopMatrix();
		*/
	}

	@Override
	public Object getClientEditGui(TileAbstractDisplay obj, Object origin, World world, EntityPlayer player) {
		//FIXME
		return null;
	}
	
	public StoredItemStack getStoredItem() {
		return stack;
	}

	public StoredItemStack setStoredItem(StoredItemStack item) {
		return stack = item;
	}

	public ItemStack getItem() {
		return stack.getItemStack();
	}

	public ItemStack setItem(ItemStack item) {
		return stack.item = item;
	}

	public long getCount() {
		return stack.stored;
	}

	public long setCount(long stored) {
		return stack.stored = stored;
	}

	@Override
	public List<InfoUUID> getInfoReferences() {
		return Lists.newArrayList();
	}

	@Override
	public String getRepresentiveString() {
		return getItem().getDisplayName() + " - " + getCount();
	}

	@Override
	public int[] createUnscaledWidthHeight() {
		return new int[] { 16, 16 };
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		stack = new StoredItemStack();
		stack.readData(nbt, type);		
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		stack.writeData(nbt, type);
		return nbt;		
	}

	public static final String REGISTRY_NAME = "s_item";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

}
