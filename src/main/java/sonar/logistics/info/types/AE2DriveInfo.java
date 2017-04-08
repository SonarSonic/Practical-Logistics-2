package sonar.logistics.info.types;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventoryHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.info.IComparableInfo;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.logistics.ComparableObject;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.api.register.RegistryType;
import sonar.logistics.connections.monitoring.InfoMonitorHandler;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.helpers.InfoRenderer;

@LogicInfoType(id = AE2DriveInfo.id, modid = "appliedenergistics2")
public class AE2DriveInfo extends BaseInfo<AE2DriveInfo> implements IProvidableInfo<AE2DriveInfo>,INameableInfo<AE2DriveInfo>, IComparableInfo<AE2DriveInfo> {

	public static final LogicMonitorHandler handler = LogicMonitorHandler.instance(InfoMonitorHandler.id);
	public static final String id = "ae2-drive-info";
	public SyncTagType.LONG totalBytes = new SyncTagType.LONG(1);
	public SyncTagType.LONG usedBytes = new SyncTagType.LONG(2);
	public SyncTagType.LONG totalTypes = new SyncTagType.LONG(3);
	public SyncTagType.LONG usedTypes = new SyncTagType.LONG(4);
	public SyncTagType.LONG itemCount = new SyncTagType.LONG(5);
	public SyncTagType.INT driveNum = new SyncTagType.INT(5);
	{
		syncList.addParts(totalBytes, usedBytes, totalTypes, usedTypes, itemCount, driveNum);
	}

	public AE2DriveInfo() {}
	
	public AE2DriveInfo(int driveNum){
		this.driveNum.setObject(driveNum);
	}
	
	public AE2DriveInfo(List<IMEInventoryHandler> invHandlers, int driveNum) {
		this(driveNum);
		for (IMEInventoryHandler handler : invHandlers) {
			if (handler instanceof ICellInventoryHandler) {
				ICellInventoryHandler cell = (ICellInventoryHandler) handler;
				ICellInventory cellInventory = cell.getCellInv();
				if (cellInventory != null) {
					totalBytes.current += cellInventory.getTotalBytes();
					usedBytes.current += cellInventory.getUsedBytes();
					totalTypes.current += cellInventory.getTotalItemTypes();
					usedTypes.current += cellInventory.getStoredItemTypes();
					itemCount.current += cellInventory.getStoredItemCount();
				}
			}
		}
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public boolean isIdenticalInfo(AE2DriveInfo info) {
		return info.totalBytes.getObject()==totalBytes.getObject() && info.usedBytes.getObject()==usedBytes.getObject() && info.totalTypes.getObject()==totalTypes.getObject() && info.usedTypes.getObject()==usedTypes.getObject() && info.itemCount.getObject()==itemCount.getObject();
	}

	@Override
	public boolean isMatchingInfo(AE2DriveInfo info) {
		return info.driveNum.getObject()==driveNum.getObject();
	}

	@Override
	public boolean isMatchingType(IMonitorInfo info) {
		return info instanceof AE2DriveInfo;
	}

	@Override
	public LogicMonitorHandler<AE2DriveInfo> getHandler() {
		return handler;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public AE2DriveInfo copy() {
		AE2DriveInfo newInfo = new AE2DriveInfo();
		newInfo.syncList.copyFrom(syncList);
		newInfo.setPath(getPath().dupe());
		return newInfo;
	}

	@Override
	public void renderInfo(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {		
		GL11.glPushMatrix();
		GL11.glPushMatrix();
		GlStateManager.disableLighting();
		GL11.glTranslated(-1, -+0.0625 * 12, +0.004);
		Minecraft.getMinecraft().getTextureManager().bindTexture(InfoContainer.getColour(infoPos));
		InfoRenderer.renderProgressBar(width, height, scale, usedBytes.getObject(), totalBytes.getObject());
		GlStateManager.enableLighting();
		GL11.glTranslated(0, 0, -0.001);
		GL11.glPopMatrix();
		List<String> strings = new ArrayList();
		strings.add("Bytes: " + usedBytes.getObject() + "/" + totalBytes.getObject());
		strings.add("Types: " + usedTypes.getObject() + "/" + totalTypes.getObject());
		InfoRenderer.renderNormalInfo(container.display.getDisplayType(), width, height, scale, strings);
		GL11.glPopMatrix();
	}

	@Override
	public ArrayList<ComparableObject> getComparableObjects(ArrayList<ComparableObject> objects) {
		objects.add(new ComparableObject(this, "total bytes", totalBytes.getObject()));
		objects.add(new ComparableObject(this, "used bytes", usedBytes.getObject()));
		objects.add(new ComparableObject(this, "total types", totalTypes.getObject()));
		objects.add(new ComparableObject(this, "used types", usedTypes.getObject()));
		objects.add(new ComparableObject(this, "item count", itemCount.getObject()));
		objects.add(new ComparableObject(this, "drive number", driveNum.getObject()));
		return null;
	}

	@Override
	public String getClientIdentifier() {
		return driveNum.getObject()==0 ? "ME DRIVE" :"ME CELL " + driveNum.getObject();
	}

	@Override
	public String getClientObject() {
		return "Bytes[" + usedBytes.getObject() +  "/" + totalBytes.getObject() + "]";
	}

	@Override
	public String getClientType() {
		return driveNum.getObject()==0 ?"ME drive" :"ME cell";
	}

	@Override
	public RegistryType getRegistryType() {
		return RegistryType.TILE;
	}

	@Override
	public AE2DriveInfo setRegistryType(RegistryType type) {
		return this;
	}

	@Override
	public void setFromReturn(LogicPath path, Object returned) {
		//custom handlers
		Object obj = returned;
	}
}
