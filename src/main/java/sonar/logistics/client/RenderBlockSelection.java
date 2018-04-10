package sonar.logistics.client;

import static net.minecraft.client.renderer.GlStateManager.disableAlpha;
import static net.minecraft.client.renderer.GlStateManager.enableAlpha;
import static net.minecraft.client.renderer.GlStateManager.popMatrix;
import static net.minecraft.client.renderer.GlStateManager.pushMatrix;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.RenderHelper;
import sonar.core.utils.Pair;

public class RenderBlockSelection {

	public static Map<BlockCoords, Pair<Long, Boolean>> positions = new HashMap<>();
	public static int displayTime = 8000;

	public static void tick(RenderWorldLastEvent evt) {
		if (positions.isEmpty()) {
			return;
		}
		Iterator<Entry<BlockCoords, Pair<Long, Boolean>>> iterator = positions.entrySet().iterator();
		iterator.forEachRemaining(position -> {
			long time = System.currentTimeMillis();
			if (time > position.getValue().a + displayTime) {
				iterator.remove();
			} else {
				pushMatrix();
				disableAlpha();
				if (position.getValue().b) {
				} else {
					RenderHelper.drawBoundingBox(Block.FULL_BLOCK_AABB, position.getKey().getBlockPos(), evt.getPartialTicks(), 120.0F, 100.0F, 20.0F, 0.5F);
				}
				enableAlpha();
				popMatrix();
			}
		});
	}

	public static boolean isPositionRenderered(BlockCoords coords) {
		for (Entry<BlockCoords, Pair<Long, Boolean>> position : positions.entrySet()) {
			if (position.getKey().equals(coords)) {
				return true;
			}
		}
		return false;
	}

	public static void addPosition(BlockCoords position, boolean exactBounds) {
		if (!positions.containsKey(position)) {
			positions.put(position, new Pair(System.currentTimeMillis(), exactBounds));
		}
	}

}
