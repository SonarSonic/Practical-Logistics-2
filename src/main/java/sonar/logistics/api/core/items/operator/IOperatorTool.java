package sonar.logistics.api.core.items.operator;

import net.minecraft.item.ItemStack;

/**implemented on an items which can be used as an IOperatorTool, typically this will be the Operator itself*/
public interface IOperatorTool {

	/**the current mode of the operator*/
    OperatorMode getOperatorMode(ItemStack stack);

}
