package sonar.logistics.guide;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import net.minecraft.item.ItemStack;

public class GuidePageRegistry {

	public static LinkedHashMap<ItemStack, IGuidePage> itemPages = new LinkedHashMap<ItemStack, IGuidePage>();
	public static ArrayList<IGuidePage> pages = new ArrayList<IGuidePage>();

	public static void init() {
		addGuidePage(new OperatorPage());
		addGuidePage(new ForgingHammerPage());		
		addGuidePage(new SapphireOrePage());
		addGuidePage(new SapphirePage());
		addGuidePage(new SapphireDustPage());
		addGuidePage(new StonePlatePage());		
		addGuidePage(new CablePage());
		addGuidePage(new NodePage());
		addGuidePage(new ArrayPage());
		addGuidePage(new TransferNodePage());
		addGuidePage(new TransceiverPage());
		addGuidePage(new EntityTransceiverPage());		
		addGuidePage(new LargeDisplayScreenPage());
		addGuidePage(new DisplayScreenPage());
		addGuidePage(new DataEmitterPage());
		addGuidePage(new DataReceiverPage());
		addGuidePage(new RedstoneSignallerPage());
		addGuidePage(new InfoReaderPage());
		addGuidePage(new InventoryReaderPage());
		addGuidePage(new FluidReaderPage());
		addGuidePage(new EnergyReaderPage());
		
	}

	public static void addGuidePage(IGuidePage page) {
		itemPages.put(page.getItemStack(), page);
		pages.add(page);
	}

	public static IGuidePage getGuidePage(ItemStack stack) {
		for (Entry<ItemStack, IGuidePage> entry : itemPages.entrySet()) {
			if (ItemStack.areItemsEqual(stack, entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}
}
