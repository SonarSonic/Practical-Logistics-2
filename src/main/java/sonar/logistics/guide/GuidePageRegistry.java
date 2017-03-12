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
		addGuidePage(new GeneralPage(1, "guide.GettingStarted.title", "guide.GettingStarted.name", "guide.GettingStarted2.name"));
		addGuidePage(new ExamplesPages(2));
		addGuidePage(new BaseItemPage(6, new ItemStack(LogisticsItems.operator)));
		addGuidePage(new BaseItemPage(7, new ItemStack(LogisticsBlocks.hammer)));
		addGuidePage(new BaseItemPage(8, new ItemStack(LogisticsBlocks.sapphire_ore)));
		addGuidePage(new BaseItemPage(9, new ItemStack(LogisticsItems.sapphire)));
		addGuidePage(new BaseItemPage(10, new ItemStack(LogisticsItems.sapphire_dust)));
		addGuidePage(new BaseItemPage(11, new ItemStack(LogisticsItems.stone_plate)));
		addGuidePage(new BaseItemPage(12, new ItemStack(LogisticsItems.etched_plate)));
		addGuidePage(new BaseItemPage(13, new ItemStack(LogisticsItems.partCable)));
		addGuidePage(new BaseItemPage(14, new ItemStack(LogisticsItems.partNode)));
		addGuidePage(new BaseItemPage(15, new ItemStack(LogisticsItems.partEntityNode)));
		addGuidePage(new BaseItemPage(16, new ItemStack(LogisticsItems.partArray)));
		addGuidePage(new TransferNodePage(17));
		addGuidePage(new BaseItemPage(18, new ItemStack(LogisticsItems.transceiver)));
		addGuidePage(new BaseItemPage(19, new ItemStack(LogisticsItems.entityTransceiver)));
		addGuidePage(new BaseItemPage(20, new ItemStack(LogisticsItems.wirelessStorage)));
		addGuidePage(new InfoReaderPage(21));
		addGuidePage(new InventoryReaderPage(22));
		addGuidePage(new FluidReaderPage(23));
		addGuidePage(new EnergyReaderPage(24));
		addGuidePage(new BaseItemPage(25, new ItemStack(LogisticsItems.displayScreen)));
		addGuidePage(new BaseItemPage(26, new ItemStack(LogisticsItems.largeDisplayScreen)));
		addGuidePage(new BaseItemPage(27, new ItemStack(LogisticsItems.holographicDisplay)));
		addGuidePage(new BaseItemPage(28, new ItemStack(LogisticsItems.partEmitter)));
		addGuidePage(new BaseItemPage(29, new ItemStack(LogisticsItems.partReceiver)));
		addGuidePage(new BaseItemPage(30, new ItemStack(LogisticsItems.partRedstoneSignaller)));
		addGuidePage(new BaseItemPage(31, new ItemStack(LogisticsItems.partClock)));

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
