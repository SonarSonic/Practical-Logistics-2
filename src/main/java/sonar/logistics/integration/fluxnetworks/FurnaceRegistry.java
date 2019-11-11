package sonar.logistics.integration.fluxnetworks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;

public class FurnaceRegistry {

    public boolean handles(TileEntity tile){
        return tile instanceof TileEntityFurnace;
    }

    public String[] getMethods(){
        return new String[]{"getBurnTime"};
    }

    public Object[] invokeMethods(TileEntityFurnace furnace, String method, Object[] args){
        switch(method){
            case "getBurnTime":
                return new Object[]{furnace.getField(0)};


        }
        return new Object[0];
    }

}
