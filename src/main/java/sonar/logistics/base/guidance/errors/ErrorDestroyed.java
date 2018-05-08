package sonar.logistics.base.guidance.errors;

import com.google.common.collect.Lists;
import net.minecraft.util.text.TextFormatting;
import sonar.core.helpers.ListHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMInfoError;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.base.tiles.INetworkTile;

import java.util.List;

@ASMInfoError(id = "destroy", modid = PL2Constants.MODID)
public class ErrorDestroyed extends ErrorTileUUID {

	public ErrorDestroyed() {
		super();
	}

	public ErrorDestroyed(InfoUUID uuid, INetworkTile tile) {
		super(uuid, tile);
	}

	public ErrorDestroyed(List<InfoUUID> uuids, INetworkTile tile) {
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
		return error instanceof ErrorDestroyed && ((ErrorDestroyed) error).identity == identity;
	}

	@Override
	public void addError(IInfoError error) {
		ErrorDestroyed dError = (ErrorDestroyed) error;
		ListHelper.addWithCheck(uuids, dError.uuids);
	}

	@Override
	public void removeError(IInfoError error) {
		ErrorDestroyed dError = (ErrorDestroyed) error;
		dError.uuids.forEach(uuid -> uuids.remove(uuid));
	}
}
