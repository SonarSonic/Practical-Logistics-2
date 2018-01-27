package sonar.logistics;

import mcmultipart.api.multipart.IMultipart;
import net.minecraft.block.Block;
import sonar.core.SonarRegister;
import sonar.core.registries.SonarRegistryBlock;
import sonar.core.registries.SonarRegistryMultipart;
import sonar.logistics.common.blocks.BlockHammer;
import sonar.logistics.common.blocks.BlockHammerAir;
import sonar.logistics.common.blocks.BlockSapphireOre;
import sonar.logistics.common.hammer.TileEntityHammer;
import sonar.logistics.common.multiparts.BlockLogistics;
import sonar.logistics.common.multiparts.cables.BlockDataCable;
import sonar.logistics.common.multiparts.cables.BlockRedstoneCable;
import sonar.logistics.common.multiparts.displays.BlockDisplayScreen;
import sonar.logistics.common.multiparts.displays.BlockHolographicDisplay;
import sonar.logistics.common.multiparts.displays.BlockLargeDisplay;
import sonar.logistics.common.multiparts.misc.BlockClock;
import sonar.logistics.common.multiparts.misc.BlockRedstoneSignaller;
import sonar.logistics.common.multiparts.nodes.BlockArray;
import sonar.logistics.common.multiparts.nodes.BlockEntityNode;
import sonar.logistics.common.multiparts.nodes.BlockNode;
import sonar.logistics.common.multiparts.nodes.BlockRedstoneNode;
import sonar.logistics.common.multiparts.nodes.BlockTransferNode;
import sonar.logistics.common.multiparts.readers.BlockAbstractReader;
import sonar.logistics.common.multiparts.wireless.BlockDataEmitter;
import sonar.logistics.common.multiparts.wireless.BlockDataReceiver;
import sonar.logistics.common.multiparts.wireless.BlockRedstoneEmitter;
import sonar.logistics.common.multiparts.wireless.BlockRedstoneReceiver;

public class PL2Blocks extends PL2 {

	public static Block sapphire_ore, hammer, hammer_air;
	public static Block info_reader, fluid_reader, energy_reader, inventory_reader, network_reader, data_cable, redstone_cable, node, data_emitter, data_receiver, redstone_emitter, redstone_receiver;
	public static Block array, entity_node, transfer_node, redstone_node, redstone_signaller, clock;
	public static Block display_screen, large_display_screen, holographic_display;

	public static void registerBlocks() {
		hammer = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryBlock(new BlockHammer(), "Hammer", TileEntityHammer.class).setProperties());
		hammer_air = SonarRegister.addBlock(PL2Constants.MODID, new SonarRegistryBlock(new BlockHammerAir(), "Hammer_Air").setProperties().removeCreativeTab());

		sapphire_ore = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryBlock(new BlockSapphireOre(), "SapphireOre").setProperties(3.0F, 5.0F));

		info_reader = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockAbstractReader(PL2Multiparts.INFO_READER)));
		inventory_reader = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockAbstractReader(PL2Multiparts.INVENTORY_READER)));
		fluid_reader = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockAbstractReader(PL2Multiparts.FLUID_READER)));
		energy_reader = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockAbstractReader(PL2Multiparts.ENERGY_READER)));
		network_reader = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockAbstractReader(PL2Multiparts.NETWORK_READER)));

		array = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockArray()));
		entity_node = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockEntityNode()));
		transfer_node = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockTransferNode()));
		redstone_node = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockRedstoneNode()));

		clock = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockClock()));
		redstone_signaller = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockRedstoneSignaller()));
		
		display_screen = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockDisplayScreen()));
		large_display_screen = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockLargeDisplay()));
		holographic_display = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockHolographicDisplay()));
		
		data_cable = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockDataCable()));
		redstone_cable = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockRedstoneCable()));
		node = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockNode()));
		data_emitter = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockDataEmitter()));
		data_receiver = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockDataReceiver()));
		redstone_emitter = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockRedstoneEmitter()));
		redstone_receiver = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockRedstoneReceiver()));

	}

	public static class LogisticsRegistryMultipart<T extends BlockLogistics & IMultipart> extends SonarRegistryMultipart<T> {

		public LogisticsRegistryMultipart(T block) {
			super(block, block.multipart.getRegistryName(), block.multipart.getTileClass());
			setProperties(0.5F, 25F);
		}

		public LogisticsRegistryMultipart(T block, PL2Multiparts multipart) {
			super(block, multipart.getRegistryName(), multipart.getTileClass());
			setProperties(0.5F, 25F);
		}
	}

}
