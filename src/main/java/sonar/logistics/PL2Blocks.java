package sonar.logistics;

import java.util.List;

import com.google.common.collect.Lists;

import mcmultipart.api.multipart.IMultipart;
import net.minecraft.block.Block;
import sonar.core.SonarCore;
import sonar.core.SonarRegister;
import sonar.core.registries.ISonarRegistryBlock;
import sonar.core.registries.SonarRegistryBlock;
import sonar.core.registries.SonarRegistryMultipart;
import sonar.logistics.common.blocks.BlockHammer;
import sonar.logistics.common.blocks.BlockHammerAir;
import sonar.logistics.common.blocks.BlockSapphireOre;
import sonar.logistics.common.hammer.TileEntityHammer;
import sonar.logistics.common.multiparts2.BlockAbstractReader;
import sonar.logistics.common.multiparts2.BlockLogisticsMultipart;
import sonar.logistics.common.multiparts2.cables.BlockDataCable;
import sonar.logistics.common.multiparts2.nodes.BlockNode;
import sonar.logistics.common.multiparts2.wireless.BlockDataEmitter;
import sonar.logistics.common.multiparts2.wireless.BlockDataReceiver;

public class PL2Blocks extends PL2 {

	public static Block sapphire_ore, hammer, hammer_air;
	public static Block info_reader, data_cable, node, data_emitter, data_receiver;

	public static void registerBlocks() {
		hammer = SonarRegister.addBlock(PL2Constants.MODID, new SonarRegistryBlock(new BlockHammer(), "Hammer", TileEntityHammer.class).setProperties());
		hammer_air = SonarRegister.addBlock(PL2Constants.MODID, new SonarRegistryBlock(new BlockHammerAir(), "Hammer_Air").setProperties().removeCreativeTab());

		sapphire_ore = SonarRegister.addBlock(PL2Constants.MODID, new SonarRegistryBlock(new BlockSapphireOre(), "SapphireOre").setProperties(3.0F, 5.0F));

		info_reader = SonarRegister.addBlock(PL2Constants.MODID, new LogisticsRegistryMultipart(new BlockAbstractReader(PL2Multiparts.INFO_READER)));
		data_cable = SonarRegister.addBlock(PL2Constants.MODID, new LogisticsRegistryMultipart(new BlockDataCable(PL2Multiparts.DATA_CABLE)));
		node = SonarRegister.addBlock(PL2Constants.MODID, new LogisticsRegistryMultipart(new BlockNode(PL2Multiparts.NODE)));
		data_emitter = SonarRegister.addBlock(PL2Constants.MODID, new LogisticsRegistryMultipart(new BlockDataEmitter(PL2Multiparts.DATA_EMITTER)));
		data_receiver = SonarRegister.addBlock(PL2Constants.MODID, new LogisticsRegistryMultipart(new BlockDataReceiver(PL2Multiparts.DATA_RECEIVER)));
		

	}

	public static class LogisticsRegistryMultipart<T extends BlockLogisticsMultipart & IMultipart> extends SonarRegistryMultipart<T> {
		
		public LogisticsRegistryMultipart(T block) {
			super(block, block.multipart.getRegistryName(), block.multipart.getTileClass());
		}

		public LogisticsRegistryMultipart(T block, PL2Multiparts multipart) {
			super(block, multipart.getRegistryName(), multipart.getTileClass());
		}
	}

}
