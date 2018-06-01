package sonar.logistics.base.filters.types;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.handlers.inventories.ItemStackHelper;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.network.sync.SyncNBTAbstractList;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMFilter;
import sonar.logistics.api.core.tiles.nodes.TransferType;
import sonar.logistics.base.filters.IItemFilter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@ASMFilter(id = ItemFilter.id, modid = PL2Constants.MODID)
public class ItemFilter extends BaseFilter implements IItemFilter {

	public static final String id = "items";

	public SyncNBTAbstractList<StoredItemStack> list = new SyncNBTAbstractList<>(StoredItemStack.class, 1);
	public SyncTagType.BOOLEAN matchNBT = new SyncTagType.BOOLEAN(2), ignoreDamage = new SyncTagType.BOOLEAN(3), matchOreDict = new SyncTagType.BOOLEAN(4), matchModid = new SyncTagType.BOOLEAN(5);
	{
		syncList.addParts(list, matchNBT, ignoreDamage, matchOreDict, matchModid);
	}

	@Override
	public String getNodeID() {
		return id;
	}

	public List<StoredItemStack> getItemFilters() {
		return list.objs;
	}

	public void addItem(StoredItemStack stack) {
		for (StoredItemStack item : list.objs) {
			if (item.equalStack(stack.item)) {
				return;
			}
		}
		list.addObject(stack);
	}

	public void removeItem(StoredItemStack stack) {
		ArrayList<StoredItemStack> toRemove = new ArrayList<>();
		for (StoredItemStack item : list.objs) {
			if (item.equalStack(stack.item)) {
				toRemove.add(item);
			}
		}
		toRemove.forEach(remove -> list.removeObject(remove));
	}

	@Override
	public boolean canTransferItem(StoredItemStack stack) {
		for (StoredItemStack item : list.objs) {
			ItemStack item1 = item.item, item2 = stack.item;
			if (item1.getItem() == item2.getItem() || (matchModid.getObject() && ItemStackHelper.matchingModid(item1, item2)) || (matchOreDict.getObject() && ItemStackHelper.matchingOreDictID(item1, item2))) {
				if (ignoreDamage.getObject() || item1.getItemDamage() == item2.getItemDamage()) {
					if (!matchNBT.getObject() || ItemStack.areItemStackTagsEqual(item1, item2)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public TransferType[] getTypes() {
		return new TransferType[] { TransferType.ITEMS };
	}

	@Override
	public void renderInfoInList(GuiSonar screen, int yPos) {
		GlStateManager.scale(0.75, 0.75, 0.75);
		FontHelper.text("Item Filter", 16, (int)((yPos + 2) /0.75), Color.white.getRGB());
		FontHelper.text("Type: " + this.getTransferMode().name(), 88, (int)((yPos + 2) /0.75), Color.white.getRGB());
		FontHelper.text("List Type: " + this.getListType().name(), 200, (int)((yPos + 2) /0.75), Color.white.getRGB());
		GlStateManager.scale(1/0.75, 1/0.75, 1/0.75);
		
		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
		// GlStateManager.scale(1.0/0.75, 1.0/0.75, 1.0/0.75);
		GlStateManager.translate(0, 12, 0);
		int yOffset = 0;
		for (int i = 0; i < Math.min(12, list.objs.size()); i++) {
            StoredItemStack item = list.objs.get(i);
			RenderHelper.renderItem(screen, 13 + i * 18,  -2 + yPos, item.item);
			RenderHelper.renderStoredItemStackOverlay(item.item, 0, 13 + i * 18,  -2 + yPos + yOffset * 18, null, true);
			RenderHelper.restoreBlendState();
		}
		GlStateManager.translate(0, -12, 0);
		// GlStateManager.scale(0.75, 0.75, 0.75);

	}

	@Override
	public boolean isValidFilter() {
		return !list.getObjects().isEmpty();
	}

}