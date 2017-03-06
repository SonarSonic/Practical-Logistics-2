package sonar.logistics.guide;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import sonar.logistics.LogisticsBlocks;
import sonar.logistics.LogisticsItems;
import sonar.logistics.guide.general.ExamplesPages;
import sonar.logistics.guide.pages.*;

public class GuidePageRegistry {

	public static ArrayList<IGuidePage> pages = new ArrayList<IGuidePage>();

	public static void init() {
		// addGuidePage(new Welcome(0));
		addGuidePage(new GeneralPage(0, "guide.Welcome.title", "guide.Welcome.name"));
		addGuidePage(new ExamplesPages(1));
		addGuidePage(new BaseItemPage(6, new ItemStack(LogisticsItems.operator)));
		addGuidePage(new BaseItemPage(7, new ItemStack(LogisticsBlocks.hammer)));
		addGuidePage(new BaseItemPage(8, new ItemStack(LogisticsBlocks.sapphire_ore)));
		addGuidePage(new BaseItemPage(9, new ItemStack(LogisticsItems.sapphire)));
		addGuidePage(new BaseItemPage(10, new ItemStack(LogisticsItems.sapphire_dust)));
		addGuidePage(new BaseItemPage(11, new ItemStack(LogisticsItems.stone_plate)));
		addGuidePage(new BaseItemPage(12, new ItemStack(LogisticsItems.partCable)));
		addGuidePage(new BaseItemPage(13, new ItemStack(LogisticsItems.partNode)));
		addGuidePage(new BaseItemPage(14, new ItemStack(LogisticsItems.partArray)));
		addGuidePage(new TransferNodePage(15));
		addGuidePage(new BaseItemPage(16, new ItemStack(LogisticsItems.transceiver)));
		addGuidePage(new BaseItemPage(17, new ItemStack(LogisticsItems.entityTransceiver)));
		addGuidePage(new BaseItemPage(18, new ItemStack(LogisticsItems.largeDisplayScreen)));
		addGuidePage(new BaseItemPage(19, new ItemStack(LogisticsItems.holographicDisplay)));
		addGuidePage(new BaseItemPage(20, new ItemStack(LogisticsItems.displayScreen)));
		addGuidePage(new BaseItemPage(21, new ItemStack(LogisticsItems.partEmitter)));
		addGuidePage(new BaseItemPage(22, new ItemStack(LogisticsItems.partReceiver)));
		addGuidePage(new BaseItemPage(23, new ItemStack(LogisticsItems.partRedstoneSignaller)));
		addGuidePage(new BaseItemPage(24, new ItemStack(LogisticsItems.partClock)));
		addGuidePage(new InfoReaderPage(25));
		addGuidePage(new InventoryReaderPage(26));
		addGuidePage(new FluidReaderPage(27));
		addGuidePage(new EnergyReaderPage(28));

	}

	public static void addGuidePage(IGuidePage page) {
		pages.add(page);
	}

	public static IGuidePage getGuidePage(ItemStack stack) {
		for (IGuidePage page : pages) {
			if (page instanceof BaseItemPage && ItemStack.areItemsEqual(stack, ((BaseItemPage) page).getItemStack())) {
				return page;
			}
		}
		return null;
	}

	public static IGuidePage getGuidePage(int pageID) {
		for (IGuidePage page : pages) {
			if (page.pageID() == pageID) {
				return page;
			}
		}
		return null;
	}

	public static IGuidePage getGuidePage(String unlocalizedName) {
		for (IGuidePage page : pages) {
			if (page instanceof BaseItemPage && unlocalizedName.equals(((BaseItemPage) page).unlocalizedName.substring(5))) {
				return page;
			}
		}
		return null;
	}
}
