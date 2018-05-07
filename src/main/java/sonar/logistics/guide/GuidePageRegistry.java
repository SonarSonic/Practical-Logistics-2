package sonar.logistics.guide;

import net.minecraft.item.ItemStack;
import sonar.logistics.PL2Blocks;
import sonar.logistics.PL2Items;
import sonar.logistics.guide.general.ExamplesPages;
import sonar.logistics.guide.pages.*;

import java.util.ArrayList;
import java.util.List;

public class GuidePageRegistry {

	public static List<IGuidePage> pages = new ArrayList<>();

	public static void init() {
		// addGuidePage(new Welcome(0));
		addGuidePage(new GeneralPage(0, "guide.Welcome.title", "guide.Welcome.name"));
		addGuidePage(new GeneralPage(1, "guide.GettingStarted.title", "guide.GettingStarted.name", "guide.GettingStarted2.name"));
		addGuidePage(new ExamplesPages(2));
		addGuidePage(new BaseItemPage(6, new ItemStack(PL2Items.operator)));
		addGuidePage(new BaseItemPage(7, new ItemStack(PL2Blocks.hammer)));
		addGuidePage(new BaseItemPage(8, new ItemStack(PL2Blocks.sapphire_ore)));
		addGuidePage(new BaseItemPage(9, new ItemStack(PL2Items.sapphire)));
		addGuidePage(new BaseItemPage(10, new ItemStack(PL2Items.sapphire_dust)));
		addGuidePage(new BaseItemPage(11, new ItemStack(PL2Items.stone_plate)));
		addGuidePage(new BaseItemPage(12, new ItemStack(PL2Items.etched_plate)));
		addGuidePage(new BaseItemPage(13, new ItemStack(PL2Blocks.data_cable)));
		addGuidePage(new BaseItemPage(14, new ItemStack(PL2Blocks.node)));
		addGuidePage(new BaseItemPage(15, new ItemStack(PL2Blocks.entity_node)));
		addGuidePage(new BaseItemPage(16, new ItemStack(PL2Blocks.array)));
		addGuidePage(new TransferNodePage(17));
		addGuidePage(new BaseItemPage(18, new ItemStack(PL2Items.transceiver)));
		//addGuidePage(new BaseItemPage(19, new ItemStack(PL2Items.entity_transceiver)));
		addGuidePage(new BaseItemPage(20, new ItemStack(PL2Items.wireless_storage_reader)));
		addGuidePage(new InfoReaderPage(21));
		addGuidePage(new InventoryReaderPage(22));
		addGuidePage(new FluidReaderPage(23));
		addGuidePage(new EnergyReaderPage(24));
		addGuidePage(new BaseItemPage(25, new ItemStack(PL2Blocks.display_screen)));
		addGuidePage(new BaseItemPage(26, new ItemStack(PL2Blocks.large_display_screen)));
		addGuidePage(new BaseItemPage(27, new ItemStack(PL2Blocks.holographic_display)));
		addGuidePage(new BaseItemPage(28, new ItemStack(PL2Blocks.data_emitter)));
		addGuidePage(new BaseItemPage(29, new ItemStack(PL2Blocks.data_receiver)));
		addGuidePage(new BaseItemPage(30, new ItemStack(PL2Blocks.redstone_signaller)));
		addGuidePage(new BaseItemPage(31, new ItemStack(PL2Blocks.clock)));

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
