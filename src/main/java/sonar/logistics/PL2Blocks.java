package sonar.logistics;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import sonar.core.SonarCore;
import sonar.core.SonarRegister;
import sonar.core.registries.ISonarRegistryBlock;
import sonar.core.registries.SonarRegistryBlock;
import sonar.logistics.common.blocks.BlockHammer;
import sonar.logistics.common.blocks.BlockHammerAir;
import sonar.logistics.common.blocks.BlockSapphireOre;
import sonar.logistics.common.hammer.TileEntityHammer;

public class PL2Blocks extends PL2 {

	public static List<ISonarRegistryBlock> registeredBlocks = Lists.newArrayList();

	public static Block sapphire_ore, hammer, hammer_air;

	public static void registerBlocks() {
		hammer = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryBlock(new BlockHammer(), "Hammer", TileEntityHammer.class).setProperties());
		hammer_air = SonarRegister.addBlock(PL2Constants.MODID, new SonarRegistryBlock(new BlockHammerAir(), "Hammer_Air").setProperties().removeCreativeTab());

		sapphire_ore = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryBlock(new BlockSapphireOre(), "SapphireOre").setProperties(3.0F, 5.0F));
	}

}
