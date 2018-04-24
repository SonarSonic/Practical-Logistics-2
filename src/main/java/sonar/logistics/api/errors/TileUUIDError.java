package sonar.logistics.api.errors;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.api.info.InfoUUID;

public abstract class TileUUIDError extends AbstractUUIDError implements INBTSyncable, IInfoError {

	public int identity;
	public ItemStack displayStack;
	public BlockCoords coords;

	public TileUUIDError() {
		super();
	}

	public TileUUIDError(InfoUUID uuid, INetworkTile tile) {
		this(Lists.newArrayList(uuid), tile);
	}

	public TileUUIDError(List<InfoUUID> uuids, INetworkTile tile) {
		super(uuids);
		identity = tile.getIdentity();
		displayStack = tile.getDisplayStack();
		coords = tile.getCoords();
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		identity = nbt.getInteger("iden");
		displayStack = new ItemStack(nbt);
		coords = BlockCoords.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		nbt.setInteger("iden", identity);
		displayStack.writeToNBT(nbt);
		BlockCoords.writeToNBT(nbt, coords);
		return nbt;
	}

}
