package sonar.logistics.api.errors;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.text.TextFormatting;
import sonar.core.helpers.ListHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.InfoErrorType;
import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;

@InfoErrorType(id = "destroy", modid = PL2Constants.MODID)
public class DestroyedError extends TileUUIDError {

	public DestroyedError() {
		super();
	}

	public DestroyedError(InfoUUID uuid, INetworkTile tile) {
		super(uuid, tile);
	}

	public DestroyedError(List<InfoUUID> uuids, INetworkTile tile) {
		super(uuids, tile);
	}

	@Override
	public List<String> getDisplayMessage() {
		List<String> message = Lists.newArrayList();
		message.add(TextFormatting.BOLD + "Connection Destroyed");

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
		return error instanceof DestroyedError && ((DestroyedError) error).identity == identity;
	}

	@Override
	public void addError(IInfoError error) {
		DestroyedError dError = (DestroyedError) error;
		ListHelper.addWithCheck(uuids, dError.uuids);
	}

	@Override
	public void removeError(IInfoError error) {
		DestroyedError dError = (DestroyedError) error;
		dError.uuids.forEach(uuid -> uuids.remove(uuid));
	}
}
