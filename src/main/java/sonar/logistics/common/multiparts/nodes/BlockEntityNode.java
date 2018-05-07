package sonar.logistics.common.multiparts.nodes;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.common.multiparts.BlockLogisticsSided;

import javax.annotation.Nullable;
import java.util.List;

public class BlockEntityNode extends BlockLogisticsSided {

	public BlockEntityNode() {
		super(PL2Multiparts.ENTITY_NODE);
	}

	public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
		super.addInformation(stack, player, tooltip, advanced);
		tooltip.add(TextFormatting.RED + "THE ENTITY SYSTEM IS CURRENTLY BROKEN");
	}
}
