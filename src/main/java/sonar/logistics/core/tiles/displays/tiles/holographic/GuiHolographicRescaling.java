package sonar.logistics.core.tiles.displays.tiles.holographic;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.FMLCommonHandler;
import sonar.core.client.gui.IGuiOrigin;
import sonar.core.client.gui.widgets.ScrollerOrientation;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.helpers.FontHelper;
import sonar.logistics.base.gui.GuiLogistics;
import sonar.logistics.base.gui.buttons.LogisticsButton;
import sonar.logistics.base.requests.colour.CustomColourButton;
import sonar.logistics.base.requests.colour.GuiColourSelection;
import sonar.logistics.core.tiles.displays.info.elements.DisplayElementHelper;

import javax.xml.ws.Holder;
import java.io.IOException;
import java.util.List;

public class GuiHolographicRescaling extends GuiLogistics {

    public TileAdvancedHolographicDisplay display;
    public HolographicScroller pitchScroller;
    public HolographicScroller yawScroller;
    public HolographicScroller rollScroller;

    public HolographicScroller widthScroller;
    public HolographicScroller heightScroller;
    public HolographicScroller xScroller;
    public HolographicScroller yScroller;
    public HolographicScroller zScroller;
    public List<HolographicScroller> scrollers;

    public GuiHolographicRescaling(Container container, TileAdvancedHolographicDisplay display) {
        super(container, display);
        this.display = display;
        this.ySize = 220;
        this.xSize = 200;
    }

    public abstract class HolographicScroller extends SonarScroller{

        public HolographicScroller(int scrollerLeft, int scrollerStart, int length, int width) {
            super(scrollerLeft, scrollerStart, length, width);
        }

        public abstract void set(TileAdvancedHolographicDisplay display);
        public abstract boolean update(TileAdvancedHolographicDisplay display);
    }

    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.add(new CustomColourButton(this, 0, guiLeft + 4, guiTop + 4, "Select Screen Colour") {
            @Override
            public boolean isSelected() {
                return false;
            }
        });
        this.buttonList.add(new LogisticsButton(this, 1, guiLeft + 30, guiTop + 4, 32, 32, "Reset Colour", ""));
        pitchScroller = new HolographicScroller(this.guiLeft + 10, this.guiTop + 30, 12, 180){

            @Override
            public void set(TileAdvancedHolographicDisplay display) {
                currentScroll = (float)(display.getPitch()+90)/180F;
            }

            @Override
            public boolean update(TileAdvancedHolographicDisplay display) {
                double newPitch = (int)(currentScroll*180F)-90;
                if(display.getPitch() != newPitch){
                    display.setRotation(newPitch, display.getYaw(), display.getRoll());
                    return true;
                }
                return false;
            }
        };
        pitchScroller.setOrientation(ScrollerOrientation.HORIZONTAL);
        yawScroller = new HolographicScroller(this.guiLeft + 10, this.guiTop + 55, 12, 180){

            @Override
            public void set(TileAdvancedHolographicDisplay display) {
                currentScroll = (float)(display.getYaw())/360F;
            }

            @Override
            public boolean update(TileAdvancedHolographicDisplay display) {
                double newYaw = (int)(currentScroll*360F);
                if(display.getYaw() != newYaw){
                    display.setRotation(display.getPitch(), newYaw, display.getRoll());
                    return true;
                }
                return false;
            }
        };
        yawScroller.setOrientation(ScrollerOrientation.HORIZONTAL);

        rollScroller = new HolographicScroller(this.guiLeft + 10, this.guiTop + 80, 12, 180){

            @Override
            public void set(TileAdvancedHolographicDisplay display) {
                currentScroll = (float)(display.getRoll())/360F;
            }

            @Override
            public boolean update(TileAdvancedHolographicDisplay display) {
                double newRoll = (int)(currentScroll*360F);
                if(display.getRoll() != newRoll){
                    display.setRotation(display.getPitch(), display.getYaw(), newRoll);
                    return true;
                }
                return false;
            }
        };
        rollScroller.setOrientation(ScrollerOrientation.HORIZONTAL);

        widthScroller = new HolographicScroller(this.guiLeft + 10, this.guiTop + 105, 12, 180){

            @Override
            public void set(TileAdvancedHolographicDisplay display) {
                currentScroll = (float)(display.getWidth())/32F;
            }

            @Override
            public boolean update(TileAdvancedHolographicDisplay display) {
                double newWidth = Math.max(0.5, DisplayElementHelper.toNearestMultiple(currentScroll*32F, 32, 0.25));
                if(display.getWidth() != newWidth){
                    display.setScaling(newWidth, display.getHeight(), display.getDepth());
                    return true;
                }
                return false;
            }
        };
        widthScroller.setOrientation(ScrollerOrientation.HORIZONTAL);

        heightScroller = new HolographicScroller(this.guiLeft + 10, this.guiTop + 130, 12, 180){

            @Override
            public void set(TileAdvancedHolographicDisplay display) {
                currentScroll = (float)(display.getHeight())/32F;
            }

            @Override
            public boolean update(TileAdvancedHolographicDisplay display) {
                double newHeight = Math.max(0.5, DisplayElementHelper.toNearestMultiple(currentScroll*32F, 32, 0.25));
                if(display.getHeight() != newHeight){
                    display.setScaling(display.getWidth(), newHeight, display.getDepth());
                    return true;
                }
                return false;
            }
        };
        heightScroller.setOrientation(ScrollerOrientation.HORIZONTAL);

        xScroller = new HolographicScroller(this.guiLeft + 10, this.guiTop + 155, 12, 180){

            @Override
            public void set(TileAdvancedHolographicDisplay display) {
                currentScroll = (float)(display.getScreenOffset().x+16)/32F;
            }

            @Override
            public boolean update(TileAdvancedHolographicDisplay display) {
                double newX = DisplayElementHelper.toNearestMultiple(currentScroll*32F, 32, 0.25)-16;
                if(display.getScreenOffset().x != newX){
                    display.setScreenOffset(newX, display.getScreenOffset().y, display.getScreenOffset().z);
                    return true;
                }
                return false;
            }
        };
        xScroller.setOrientation(ScrollerOrientation.HORIZONTAL);

        yScroller = new HolographicScroller(this.guiLeft + 10, this.guiTop + 180, 12, 180){

            @Override
            public void set(TileAdvancedHolographicDisplay display) {
                currentScroll = (float)(display.getScreenOffset().y+16)/32F;
            }

            @Override
            public boolean update(TileAdvancedHolographicDisplay display) {
                double newY = DisplayElementHelper.toNearestMultiple(currentScroll*32F, 32, 0.25)-16;
                if(display.getScreenOffset().y != newY){
                    display.setScreenOffset(display.getScreenOffset().x, newY, display.getScreenOffset().z);
                    return true;
                }
                return false;
            }
        };
        yScroller.setOrientation(ScrollerOrientation.HORIZONTAL);

        zScroller = new HolographicScroller(this.guiLeft + 10, this.guiTop + 205, 12, 180){

            @Override
            public void set(TileAdvancedHolographicDisplay display) {
                currentScroll = (float)(display.getScreenOffset().z+16)/32F;
            }

            @Override
            public boolean update(TileAdvancedHolographicDisplay display) {
                double newZ = DisplayElementHelper.toNearestMultiple(currentScroll*32F, 32, 0.25)-16;
                if(display.getScreenOffset().z != newZ){
                    display.setScreenOffset(display.getScreenOffset().x, display.getScreenOffset().y, newZ);
                    return true;
                }
                return false;
            }
        };


        zScroller.setOrientation(ScrollerOrientation.HORIZONTAL);
        scrollers = Lists.newArrayList(pitchScroller, yawScroller, rollScroller, widthScroller, heightScroller, xScroller, yScroller, zScroller);
        setScrollers();
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button instanceof CustomColourButton) {
            FMLCommonHandler.instance().showGuiScreen(IGuiOrigin.withOrigin(new GuiColourSelection(inventorySlots, entity, display.getScreenColour(), i -> display.screenColour.setObject(i)), this));
            return;
        }
        if(button instanceof LogisticsButton){
            switch(button.id){
                case 1:
                    display.screenColour.setObject(TileAbstractHolographicDisplay.DEFAULT_COLOUR);
                    break;
            }
        }

    }
    @Override
    public void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        FontHelper.textCentre("Holographic Display Scaling", xSize, 4, -1);
        FontHelper.text("Pitch: " + display.getPitch() + "°", 12, 20, -1);
        FontHelper.text("Yaw: " + display.getYaw() + "°", 12, 55-10, -1);
        FontHelper.text("Roll: " + display.getRoll() + "°", 12, 80-10, -1);
        FontHelper.text("Screen Width: " + display.getWidth(), 12, 105-10, -1);
        FontHelper.text("Screen Height: " + display.getHeight(), 12, 130-10, -1);
        FontHelper.text("Offset X: " + display.getScreenOffset().x, 12, 155-10, -1);
        FontHelper.text("Offset Y: " + display.getScreenOffset().y, 12, 180-10, -1);
        FontHelper.text("Offset Z: " + display.getScreenOffset().z, 12, 205-10, -1);
    }

    @Override
    public void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
        super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
        scrollers.forEach(s -> renderScroller(s));
    }

    public void drawScreen(int x, int y, float var) {
        super.drawScreen(x, y, var);
        scrollers.forEach(s -> s.drawScreen(x, y, true));
        updateScrollers();
    }

    public void setScrollers(){
        scrollers.forEach(s -> s.set(display));
    }

    public void updateScrollers(){
        Holder<Boolean> shouldUpdate = new Holder(false);
        scrollers.forEach(s -> {
            if(s.update(display)){
                shouldUpdate.value = true;
            }
        });
        if(shouldUpdate.value){
            display.getHolographicEntity().ifPresent(entity -> entity.setSizingFromDisplay(display));
        }
    }

    @Override
    protected void keyTyped(char c, int i) throws IOException {
        if (isCloseKey(i)) {
            display.sendPropertiesToServer();
        }
        super.keyTyped(c, i);
    }
}
