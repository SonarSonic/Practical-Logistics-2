package sonar.logistics.core.tiles.displays.gsi.newgui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import sonar.logistics.base.data.api.categories.IDataCategory;
import sonar.logistics.base.data.methods.MethodRegistry;
import sonar.logistics.base.gui.GuiSelectionList;
import sonar.logistics.base.gui.PL2Colours;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.info.InfoRenderHelper;

import java.util.ArrayList;

public class GuiDataSelection extends GuiSelectionList<GuiDataSelection.DataType> {

    public IDataCategory category;

    public GuiDataSelection(DisplayGSI gsi, Container container) {
        super(container, gsi.getDisplay());
        this.xSize = 182 + 66;
    }


    @Override
    public int getColour(int i, int type) {
        return 0;
    }

    @Override
    public boolean isPairedInfo(DataType info) {
        return false;
    }

    @Override
    public boolean isSelectedInfo(DataType info) {
        return false;
    }

    @Override
    public boolean isCategoryHeader(DataType info) {
        return false;
    }

    @Override
    public void renderInfo(DataType info, int yPos) {
        InfoRenderHelper.renderTripleStringIntoGUI(info.method_name, "obj", info.return_type, yPos, PL2Colours.white_text.getRGB());
    }

    @Override
    public void selectionPressed(GuiButton button, int infoPos, int buttonID, DataType info) {

    }

    @Override
    public void setInfo() {
        infoList = new ArrayList<>();
        MethodRegistry.blockFunction.stream().filter(m -> m.getIdentifier().getResourceDomain().equals(category.getID())).forEach(m -> infoList.add(new DataType(m.getIdentifier().getResourceDomain(), m.getIdentifier().getResourcePath())));
        MethodRegistry.tileEntityFunction.stream().filter(m -> m.getIdentifier().getResourceDomain().equals(category.getID())).forEach(m -> infoList.add(new DataType(m.getIdentifier().getResourceDomain(), m.getIdentifier().getResourcePath())));
        MethodRegistry.worldFunction.stream().filter(m -> m.getIdentifier().getResourceDomain().equals(category.getID())).forEach(m -> infoList.add(new DataType(m.getIdentifier().getResourceDomain(), m.getIdentifier().getResourcePath())));
    }

    public static class DataType{

        public String method_name;
        public String return_type;

        public DataType(String method_name, String return_type){
            this.method_name = method_name;
            this.return_type = return_type;
        }
    }

}
