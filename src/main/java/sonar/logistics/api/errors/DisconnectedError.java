package sonar.logistics.api.errors;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.InfoErrorType;
import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.states.ErrorMessage;

@InfoErrorType(id = "disconnect", modid = PL2Constants.MODID)
public class DisconnectedError extends TileUUIDError {

	public boolean chunkUnload = false;

	public DisconnectedError() {
		super();
	}

	public DisconnectedError(InfoUUID uuid, INetworkTile tile) {
		super(uuid, tile);
	}

	public DisconnectedError(List<InfoUUID> uuids, INetworkTile tile) {
		super(uuids, tile);
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		chunkUnload = nbt.getBoolean("chunk");
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		nbt.setBoolean("chunk", chunkUnload);
		return nbt;
	}

	@Override
	public List<String> getDisplayMessage() {
		List<String> message = Lists.newArrayList();
		message.add(TextFormatting.BOLD + (chunkUnload ? "Connection Not Chunk Loaded" : "Connection Disconnected"));
		if (!this.displayStack.isEmpty()) {
			message.add("NAME: " + displayStack.getDisplayName());
		}
		if (this.coords != null) {
			message.add("POS: " + coords.toString());
		}
		message.add("ID: " + identity);
		return message;
	}

	@Override
	public boolean canDisplayInfo(IInfo info) {
		return false;
	}

	@Override
	public boolean canCombine(IInfoError error) {
		return error instanceof DisconnectedError && ((DisconnectedError) error).identity == identity;
	}

	@Override
	public void addError(IInfoError error) {
		DisconnectedError dError = (DisconnectedError) error;
		ListHelper.addWithCheck(uuids, dError.uuids);
		chunkUnload = dError.chunkUnload || chunkUnload;
	}

	@Override
	public void removeError(IInfoError error) {
		DisconnectedError dError = (DisconnectedError) error;
		dError.uuids.forEach(uuid -> uuids.remove(uuid));
		chunkUnload = dError.chunkUnload;
	}

}
