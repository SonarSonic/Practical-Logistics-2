package sonar.logistics.core.tiles.displays.gsi.render;

import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import sonar.logistics.api.core.tiles.displays.tiles.IDisplay;
import sonar.logistics.base.utils.slots.EnumDisplayFaceSlot;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenLook;
import sonar.logistics.core.tiles.displays.tiles.DisplayVectorHelper;

import javax.annotation.Nullable;
import java.util.Optional;

public class GSIOverlays {

	public static DisplayScreenLook currentLook = null;
	public static BlockPos lastPos = null;

	@Nullable
	public static DisplayScreenLook getCurrentLook(DisplayGSI container) {
		if (currentLook != null && currentLook.identity == container.getDisplayGSIIdentity()) {
			return currentLook;
		}
		return null;
	}

	@Nullable
	public static DisplayScreenLook getCurrentLook(int identity) {
		if (currentLook != null && currentLook.identity == identity) {
			return currentLook;
		}
		return null;
	}

	public static void tick(DrawBlockHighlightEvent evt) {
		EnumFacing face = evt.getTarget().sideHit;
		if (face != null) {
			BlockPos clickPos = evt.getTarget().getBlockPos();
			IPartSlot slot = EnumDisplayFaceSlot.fromFace(face);
			Optional<IMultipartTile> tile = MultipartHelper.getPartTile(evt.getPlayer().getEntityWorld(), clickPos, slot);
			if (tile.isPresent() && tile.get() instanceof IDisplay) {
				IDisplay display = (IDisplay) tile.get();
				if(display.getGSI() != null) {
					currentLook = DisplayVectorHelper.createLook(evt.getPlayer(), display);
					return;
				}
			}

		}
		currentLook = null;

	}

}
