package sonar.logistics.core.tiles.displays.info.types;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMInfo;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.INameableInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.readers.ILogicListSorter;
import sonar.logistics.core.tiles.displays.info.elements.NetworkEnergyListElement;
import sonar.logistics.core.tiles.displays.info.elements.base.IDisplayElement;
import sonar.logistics.core.tiles.displays.info.elements.base.IElementStorageHolder;
import sonar.logistics.core.tiles.displays.info.types.energy.MonitoredEnergyStack;
import sonar.logistics.core.tiles.displays.info.types.fluids.ElementNetworkFluidGrid;
import sonar.logistics.core.tiles.displays.info.types.fluids.InfoNetworkFluid;
import sonar.logistics.core.tiles.displays.info.types.items.MonitoredItemStack;
import sonar.logistics.core.tiles.displays.info.types.items.NetworkItemGridElement;
import sonar.logistics.core.tiles.readers.SortingHelper;

import java.util.List;

@ASMInfo(id = LogicInfoList.id, modid = PL2Constants.MODID)
public class LogicInfoList extends BaseInfo<LogicInfoList> implements INameableInfo<LogicInfoList> {

	public static final String id = "logiclist";
	public final SyncTagType.INT networkID = (INT) new SyncTagType.INT(2).setDefault(-1);
	public SyncTagType.INT identity = new SyncTagType.INT(0);
	public SyncTagType.STRING infoID = new SyncTagType.STRING(1);
	public ILogicListSorter listSorter = null;
	public boolean listChanged = true, wasRefreshed = false;
	{
		syncList.addParts(identity, infoID, networkID);
	}

	public LogicInfoList() {}

	public LogicInfoList(int identity, String infoID, int networkID) {
		this.identity.setObject(identity);
		this.infoID.setObject(infoID);
		this.networkID.setObject(networkID);
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public boolean isIdenticalInfo(LogicInfoList info) {
		return identity.getObject().equals(info.identity.getObject());
	}

	@Override
	public boolean isMatchingInfo(LogicInfoList info) {
		return infoID.getObject().equals(info.infoID.getObject()) && networkID.getObject().equals(info.networkID.getObject()) ;
	}

	@Override
	public boolean isMatchingType(IInfo info) {
		return info instanceof LogicInfoList;
	}

	@Override
	public boolean isValid() {
		return identity.getObject() != -1;
	}

	@Override
	public LogicInfoList copy() {
		return new LogicInfoList(identity.getObject(), infoID.getObject(), networkID.getObject());
	}

	@Override
	public String getClientIdentifier() {
		return "List: " + infoID.getObject().toLowerCase();
	}

	@Override
	public String getClientObject() {
		return "LIST";
	}

	@Override
	public String getClientType() {
		return "list";
	}
	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		if (nbt.hasKey("sorter")) {
			listSorter = SortingHelper.loadListSorter(nbt.getCompoundTag("sorter"));
		}
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		if (listSorter != null) {
			nbt.setTag("sorter", SortingHelper.saveListSorter(new NBTTagCompound(), listSorter, SyncType.SAVE));
		}
		return nbt;
	}
	@Override
	public void createDefaultElements(List<IDisplayElement> toAdd, IElementStorageHolder h, InfoUUID uuid) {
		switch (this.infoID.getObject()) {
		case MonitoredItemStack.id:
			toAdd.add(new NetworkItemGridElement(uuid));
			break;
		case InfoNetworkFluid.id:
			toAdd.add(new ElementNetworkFluidGrid(uuid));
			break;
		case MonitoredEnergyStack.id:
			toAdd.add(new NetworkEnergyListElement(uuid));
			break;
		}
	}
}