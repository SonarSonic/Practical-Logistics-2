package sonar.logistics.info.types;

import java.util.ArrayList;

import sonar.core.helpers.FontHelper;
import sonar.core.network.sync.ObjectType;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.core.network.sync.SyncUnidentifiedObject;
import sonar.core.utils.Pair;
import sonar.logistics.Logistics;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.ISuffixable;
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
import sonar.logistics.info.LogicInfoRegistry;

/** default info type, created by the LogicRegistry */
@LogicInfoType(id = LogicInfo.id, modid = Logistics.MODID)
public class LogicInfo extends BaseInfo<LogicInfo> implements IProvidableInfo<LogicInfo>, INameableInfo<LogicInfo>, ISuffixable, IComparableInfo<LogicInfo> {

	public static final String id = "logic";
	public static final LogicMonitorHandler handler = LogicMonitorHandler.instance(InfoMonitorHandler.id);
	private String suffix, prefix;
	public SyncTagType.STRING iden = new SyncTagType.STRING(1);
	public SyncTagType.INT idenNum = (INT) new SyncTagType.INT(2).setDefault(-1);
	public SyncEnum<RegistryType> regType = new SyncEnum(RegistryType.values(), 3);
	public SyncUnidentifiedObject obj = new SyncUnidentifiedObject(4);
	public SyncTagType.BOOLEAN isCategory = new SyncTagType.BOOLEAN(5);

	{
		syncList.addParts(iden, idenNum, regType, obj, isCategory);
	}

	public LogicInfo() {
		super();
	}

	public static LogicInfo buildCategoryInfo(RegistryType type) {
		LogicInfo info = new LogicInfo();
		info.regType.setObject(type);
		info.isCategory.setObject(true);
		return info;
	}

	public static LogicInfo buildDirectInfo(String identifier, RegistryType type, Object obj) {
		return buildDirectInfo(identifier, -1, type, obj);
	}

	public static LogicInfo buildDirectInfo(String identifier, int identifierNum, RegistryType type, Object obj) {
		LogicInfo info = new LogicInfo();
		info.obj.set(obj, ObjectType.getInfoType(obj));
		info.regType.setObject(type);
		info.iden.setObject(identifier);
		info.idenNum.setObject(identifierNum);
		if (info.obj.objType == ObjectType.NONE) {
			Logistics.logger.error(String.format("Invalid Info: %s with object %s", identifier, obj));
			return null;
		}
		return info;
	}

	@Override
	public LogicMonitorHandler<LogicInfo> getHandler() {
		return handler;
	}

	@Override
	public boolean isIdenticalInfo(LogicInfo info) {
		return obj.get().equals(info.obj.get());
	}

	@Override
	public boolean isMatchingInfo(LogicInfo info) {
		if (this.isCategory.getObject()) {
			return info.isCategory.getObject() && regType.getObject().equals(info.regType.getObject());
		}
		return obj.objType != null && obj.objType.equals(info.obj.objType) && iden.getObject().equals(info.iden.getObject()) && regType.getObject().equals(info.regType.getObject()) && idenNum.getObject().equals(info.idenNum.getObject());
	}

	public RegistryType getRegistryType() {
		return regType.getObject();
	}

	@Override
	public LogicInfo setRegistryType(RegistryType type) {
		regType.setObject(type);
		return this;
	}

	public String getClientIdentifier() {
		String newMethod = LogicInfoRegistry.INSTANCE.clientNameAdjustments.get(iden.getObject());
		if (newMethod != null) {
			return FontHelper.translate("pl." + newMethod);
		}
		return FontHelper.translate("pl." + iden) + (idenNum.getObject() != -1 ? " " + idenNum.getObject() : "");
	}

	public Pair<String, String> updateAdjustments(boolean forceUpdate) {
		if (forceUpdate || (prefix == null || suffix == null)) {
			prefix = "";
			suffix = "";
			Pair<String, String> adjustment = LogicInfoRegistry.INSTANCE.infoAdjustments.get(iden.getObject());
			if (adjustment != null) {
				if (!adjustment.a.isEmpty())
					prefix = adjustment.a + " ";
				if (!adjustment.b.isEmpty())
					suffix = " " + adjustment.b;
			}
		}
		return new Pair(prefix, suffix);
	}

	public String getClientObject() {
		if (iden.getObject().equals("Block.getUnlocalizedName")) {
			return FontHelper.translate(obj.get().toString() + ".name");
		}
		updateAdjustments(false);
		return prefix + obj.get().toString() + suffix;
	}

	public String getClientType() {
		return obj.objType.toString().toLowerCase();
	}

	public Object getInfo() {
		return obj.get();
	}

	public ObjectType getInfoType() {
		return obj.objType;
	}

	@Override
	public boolean isHeader() {
		return isCategory.getObject();
	}

	@Override
	public boolean isMatchingType(IMonitorInfo info) {
		return info instanceof LogicInfo;
	}

	@Override
	public boolean isValid() {
		return this.isCategory.getObject() ? regType != null : obj.get() != null && obj.objType != null;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public LogicInfo copy() {
		return buildDirectInfo(iden.getObject(), regType.getObject(), obj.get()).setPath(this.getPath().dupe());
	}

	@Override
	public void renderInfo(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		InfoRenderer.renderNormalInfo(container.display.getDisplayType(), width, height, scale, displayInfo.getFormattedStrings());
	}

	@Override
	public String getSuffix() {
		updateAdjustments(false);
		return suffix;
	}

	@Override
	public String getPrefix() {
		updateAdjustments(false);
		return prefix;
	}

	@Override
	public String getRawData() {
		if (iden.getObject().equals("Block.getUnlocalizedName")) {
			return FontHelper.translate(obj.get().toString() + ".name");
		}
		return obj.get().toString();
	}

	@Override
	public ArrayList<ComparableObject> getComparableObjects(ArrayList<ComparableObject> objects) {
		objects.add(new ComparableObject(this, "raw info", obj.get()));
		objects.add(new ComparableObject(this, "object type", obj.objType));
		objects.add(new ComparableObject(this, "identifier", iden.getObject()));
		return objects;
	}

	public String toString() {
		return this.getClientIdentifier() + ": " + this.getClientObject();
	}

	@Override
	public void setFromReturn(LogicPath path, Object returned) {
		this.obj.obj = returned;
	}

}
