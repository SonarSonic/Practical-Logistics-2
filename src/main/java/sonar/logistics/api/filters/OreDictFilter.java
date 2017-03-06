package sonar.logistics.api.filters;

import java.awt.Color;
import java.util.ArrayList;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.oredict.OreDictionary;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.core.network.sync.SyncTagTypeList;
import sonar.logistics.Logistics;
import sonar.logistics.api.asm.NodeFilter;
import sonar.logistics.api.nodes.TransferType;

@NodeFilter(id = OreDictFilter.id, modid = Logistics.MODID)
public class OreDictFilter extends BaseFilter implements IItemFilter {

	public static final String id = "oredict";
	public SyncTagTypeList<String> oreDict = new SyncTagTypeList(NBT.TAG_STRING, 1);
	public ArrayList<Integer> ints;

	{
		syncList.addPart(oreDict);
	}

	@Override
	public String getNodeID() {
		return id;
	}

	public ArrayList<String> getOreIDs() {
		return oreDict.objs;
	}

	public void addOreDict(String oreName) {
		for (String string : oreDict.getObjects()) {
			if (string.equals(oreName)) {
				return;
			}
		}
		oreDict.addObject(oreName);
	}

	public void removeOreDict(String oreName) {
		ArrayList<String> toRemove = new ArrayList();
		for (String string : oreDict.getObjects()) {
			if (string.equals(oreName)) {
				toRemove.add(string);
			}
		}
		toRemove.forEach(remove -> oreDict.removeObject(remove));
	}

	@Override
	public boolean canTransferItem(StoredItemStack stack) {
		if (oreDict != null && !oreDict.getObjects().isEmpty() && stack != null) {
			ArrayList<Integer> oreIDs = getOres();
			int[] names = OreDictionary.getOreIDs(stack.item);
			if (names.length != 0 && !oreIDs.isEmpty()) {
				for (Integer id : names) {
					for (Integer oreName : oreIDs) {
						if (id.equals(oreName)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public ArrayList<Integer> getOres() {
		if (ints == null) {
			ArrayList ores = new ArrayList();
			for (String string : oreDict.objs) {
				int id = OreDictionary.getOreID(string);
				if (!ores.contains(id)) {
					ores.add(id);
				}
			}
			ints = ores;
		}
		return ints;
	}

	@Override
	public TransferType[] getTypes() {
		return new TransferType[] { TransferType.ITEMS };
	}

	@Override
	public void renderInfoInList(GuiSonar screen, int yPos) {
		GlStateManager.scale(0.75, 0.75, 0.75);
		FontHelper.text("Ore Filter", 16, (int) ((yPos + 2) * 1 / 0.75), Color.white.getRGB());
		FontHelper.text("Type: " + this.getTransferMode().name(), 88, (int) ((yPos + 2) * 1 / 0.75), Color.white.getRGB());
		FontHelper.text("List Type: " + this.getListType().name(), 200, (int) ((yPos + 2) * 1 / 0.75), Color.white.getRGB());
		FontHelper.text("Keys: " + FontHelper.getStringListToText(oreDict.objs), 16, (int) ((yPos + 14) * 1 / 0.75), Color.white.getRGB());
		GlStateManager.scale(1 / 0.75, 1 / 0.75, 1 / 0.75);

	}

	@Override
	public boolean isValidFilter() {
		return !oreDict.getObjects().isEmpty();
	}

}
