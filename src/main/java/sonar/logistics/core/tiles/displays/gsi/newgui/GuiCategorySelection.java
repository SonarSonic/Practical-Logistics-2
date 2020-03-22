package sonar.logistics.core.tiles.displays.gsi.newgui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.FMLCommonHandler;
import sonar.core.helpers.FontHelper;
import sonar.logistics.base.data.api.categories.DataCategories;
import sonar.logistics.base.data.api.categories.IDataCategory;
import sonar.logistics.base.gui.GuiSelectionList;
import sonar.logistics.base.gui.PL2Colours;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;

public class GuiCategorySelection extends GuiSelectionList<IDataCategory> {

    public EnumDataSourceType type;
    public DisplayGSI gsi;

    public GuiCategorySelection(DisplayGSI gsi, Container container) {
        super(container, gsi.getDisplay());
        this.gsi = gsi;
        setDataSourceType(EnumDataSourceType.ALL_SOURCES);
        listHeight = 24;
        listWidth = xSize-23;
    }

    @Override
    public int getColour(int i, int type) {
        return -1;
    }

    @Override
    public boolean isPairedInfo(IDataCategory info) {
        return false;
    }

    @Override
    public boolean isSelectedInfo(IDataCategory info) {
        return false;
    }

    @Override
    public boolean isCategoryHeader(IDataCategory info) {
        return false;
    }

    @Override
    public void renderInfo(IDataCategory info, int yPos) {
        bindTexture(info.getIconLocation());
        GlStateManager.scale(0.1, 0.1, 0.1);
        drawTexturedModalRect(12*10, yPos*10, 0, 0, 256,256);
        GlStateManager.scale(1/0.1, 1/0.1, 1/0.1);


        FontHelper.text(info.getID(), 40, yPos+4, PL2Colours.white_text.getRGB());
        FontHelper.text("Sources: " + info.hashCode()/100000000, 40, yPos+4+12, PL2Colours.white_text.getRGB());
    }

    @Override
    public void selectionPressed(GuiButton button, int infoPos, int buttonID, IDataCategory info) {

        GuiDataSelection selection = new GuiDataSelection(this.gsi, this.inventorySlots);
        selection.category = info;
        selection.setOrigin(this);
        FMLCommonHandler.instance().showGuiScreen(selection);

    }

    @Override
    public void setInfo() {
        infoList = DataCategories.categories;
    }

    public void setDataSourceType(EnumDataSourceType type){
        this.type = type;

        ///request packet.
    }
}
