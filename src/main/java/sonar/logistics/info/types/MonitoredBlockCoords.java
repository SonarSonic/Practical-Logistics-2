package sonar.logistics.info.types;

import java.util.List;

import net.minecraft.item.ItemStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.BlockCoords;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncNBTAbstract;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.info.IComparableInfo;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.tiles.signaller.ComparableObject;

@LogicInfoType(id = MonitoredBlockCoords.id, modid = PL2Constants.MODID)
public class MonitoredBlockCoords extends BaseInfo<MonitoredBlockCoords> implements INameableInfo<MonitoredBlockCoords>, IComparableInfo<MonitoredBlockCoords> {

	public static final String id = "coords";
	private SyncCoords syncCoords = new SyncCoords(1);
	private final SyncNBTAbstract<StoredItemStack> blockStack = new SyncNBTAbstract<StoredItemStack>(StoredItemStack.class, 2);
	{
		syncList.addParts(syncCoords, blockStack);
	}

	public MonitoredBlockCoords() {}

	public MonitoredBlockCoords(BlockCoords coords, ItemStack blockStack) {
		this.syncCoords.setCoords(coords);
		this.blockStack.setObject(new StoredItemStack(blockStack));
	}

	@Override
	public boolean isIdenticalInfo(MonitoredBlockCoords info) {
		return true;
	}

	@Override
	public boolean isMatchingInfo(MonitoredBlockCoords info) {
		return info.syncCoords.getCoords().equals(syncCoords.getCoords());
	}

	@Override
	public boolean isMatchingType(IInfo info) {
		return info instanceof MonitoredBlockCoords;
	}

	@Override
	public String getClientIdentifier() {		
		return blockStack.getObject().getItemStack().getDisplayName();
	}


	@Override
	public String getClientObject() {		
		return syncCoords.getCoords().toString();
	}

	@Override
	public String getClientType() {
		return "position";
	}

	public boolean equals(Object obj) {
		if (obj instanceof MonitoredBlockCoords) {
			MonitoredBlockCoords monitoredCoords = (MonitoredBlockCoords) obj;
			return monitoredCoords.getCoords().equals(getCoords()) && monitoredCoords.getUnlocalizedName().equals(getUnlocalizedName());
		}
		return false;
	}

	@Override
	public boolean isValid() {
		return getCoords() != null;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public MonitoredBlockCoords copy() {
		return new MonitoredBlockCoords(getCoords(), blockStack.getObject().getItemStack());
	}
	
	public BlockCoords getCoords(){
		return syncCoords.getCoords();
	}
	
	public String getUnlocalizedName(){
		return blockStack.getObject().getItemStack().getUnlocalizedName();
	}

	@Override
	public List<ComparableObject> getComparableObjects(List<ComparableObject> objects) {
		BlockCoords coords = syncCoords.getCoords();
		objects.add(new ComparableObject(this, "x", coords.getX()));
		objects.add(new ComparableObject(this, "y", coords.getY()));
		objects.add(new ComparableObject(this, "z", coords.getZ()));
		return objects;
	}
}