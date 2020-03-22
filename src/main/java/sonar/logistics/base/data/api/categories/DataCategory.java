package sonar.logistics.base.data.api.categories;

import net.minecraft.util.ResourceLocation;

public class DataCategory implements IDataCategory {

    public final String id;
    public final ResourceLocation icon;

    public DataCategory(String id, ResourceLocation icon){
        this.id = id;
        this.icon = icon;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public ResourceLocation getIconLocation() {
        return icon;
    }
}
