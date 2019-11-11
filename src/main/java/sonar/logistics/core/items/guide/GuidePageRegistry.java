package sonar.logistics.core.items.guide;

import net.minecraft.item.ItemStack;
import sonar.logistics.PL2Blocks;
import sonar.logistics.PL2Items;
import sonar.logistics.core.items.guide.pages.pages.*;

import java.util.ArrayList;
import java.util.List;

public class GuidePageRegistry {

	public static List<IGuidePage> pages = new ArrayList<>();

	public static void init() {
		// addGuidePage(new Welcome(0));
		int pageID = 0;
		addGuidePage(new GeneralPage(pageID++, "guide.Welcome.title", "guide.Welcome.name"));
		addGuidePage(new GeneralPage(pageID++, "guide.GettingStarted.title", "guide.GettingStarted.name", "guide.GettingStarted2.name"));
		addGuidePage(new ExamplesPages(pageID++));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Items.operator)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Blocks.hammer)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Blocks.sapphire_ore)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Items.sapphire)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Items.sapphire_dust)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Items.stone_plate)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Items.etched_plate)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Items.signalling_plate)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Items.wireless_plate)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Blocks.data_cable)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Blocks.node)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Blocks.entity_node)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Blocks.array)));
		addGuidePage(new TransferNodePage(pageID++));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Items.transceiver)));
		//addGuidePage(new BaseItemPage(19, new ItemStack(PL2Items.entity_transceiver)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Items.wireless_storage_reader)));
		addGuidePage(new InfoReaderPage(pageID++));
		addGuidePage(new InventoryReaderPage(pageID++));
		addGuidePage(new FluidReaderPage(pageID++));
		addGuidePage(new EnergyReaderPage(pageID++));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Blocks.display_screen)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Blocks.large_display_screen)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Blocks.holographic_display)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Blocks.data_emitter)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Blocks.data_receiver)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Blocks.redstone_signaller)));
		addGuidePage(new BaseItemPage(pageID++, new ItemStack(PL2Blocks.clock)));

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
