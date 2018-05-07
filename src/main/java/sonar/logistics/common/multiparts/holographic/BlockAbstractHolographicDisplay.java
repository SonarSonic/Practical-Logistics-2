package sonar.logistics.common.multiparts.holographic;

import mcmultipart.api.container.IPartInfo;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.common.multiparts.displays.BlockAbstractDisplay;

public class BlockAbstractHolographicDisplay extends BlockAbstractDisplay {

    public BlockAbstractHolographicDisplay(PL2Multiparts multipart) {
        super(multipart);
    }

    public void onPartChanged(IPartInfo part, IPartInfo otherPart) {
        if(part.getTile() instanceof TileAbstractHolographicDisplay){
            ((TileAbstractHolographicDisplay) part.getTile()).markDirty();
        }
    }
}
