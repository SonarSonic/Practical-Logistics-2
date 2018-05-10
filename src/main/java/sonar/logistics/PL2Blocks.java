package sonar.logistics;

import mcmultipart.api.multipart.IMultipart;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import sonar.core.SonarRegister;
import sonar.core.registries.SonarRegistryBlock;
import sonar.core.registries.SonarRegistryMultipart;
import sonar.logistics.core.blocks.BlockSapphireOre;
import sonar.logistics.core.items.ItemConnectableMultipart;
import sonar.logistics.core.tiles.base.BlockLogistics;
import sonar.logistics.core.tiles.connections.data.tiles.BlockDataCable;
import sonar.logistics.core.tiles.connections.redstone.tiles.BlockRedstoneCable;
import sonar.logistics.core.tiles.displays.tiles.connected.BlockLargeDisplay;
import sonar.logistics.core.tiles.displays.tiles.holographic.BlockAdvancedHolographicDisplay;
import sonar.logistics.core.tiles.displays.tiles.holographic.BlockHolographicDisplay;
import sonar.logistics.core.tiles.displays.tiles.small.BlockDisplayScreen;
import sonar.logistics.core.tiles.displays.tiles.small.BlockMiniDisplay;
import sonar.logistics.core.tiles.misc.clock.BlockClock;
import sonar.logistics.core.tiles.misc.hammer.BlockHammer;
import sonar.logistics.core.tiles.misc.hammer.BlockHammerAir;
import sonar.logistics.core.tiles.misc.hammer.TileEntityHammer;
import sonar.logistics.core.tiles.misc.signaller.BlockRedstoneSignaller;
import sonar.logistics.core.tiles.nodes.BlockRedstoneNode;
import sonar.logistics.core.tiles.nodes.array.BlockArray;
import sonar.logistics.core.tiles.nodes.entity.BlockEntityNode;
import sonar.logistics.core.tiles.nodes.node.BlockNode;
import sonar.logistics.core.tiles.nodes.transfer.BlockTransferNode;
import sonar.logistics.core.tiles.readers.base.BlockAbstractReader;
import sonar.logistics.core.tiles.wireless.emitters.BlockDataEmitter;
import sonar.logistics.core.tiles.wireless.emitters.BlockRedstoneEmitter;
import sonar.logistics.core.tiles.wireless.receivers.BlockDataReceiver;
import sonar.logistics.core.tiles.wireless.receivers.BlockRedstoneReceiver;

public class PL2Blocks extends PL2 {

	public static Block sapphire_ore, hammer, hammer_air;
	public static Block info_reader, fluid_reader, energy_reader, inventory_reader, network_reader, data_cable, redstone_cable, node, data_emitter, data_receiver, redstone_emitter, redstone_receiver;
	public static Block array, entity_node, transfer_node, redstone_node, redstone_signaller, clock;
	public static Block display_screen, mini_display, large_display_screen, holographic_display, advanced_holographic_display;

	public static void registerBlocks() {
		hammer = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryBlock(new BlockHammer(), "Hammer", TileEntityHammer.class).setProperties());
		hammer_air = SonarRegister.addBlock(PL2Constants.MODID, new SonarRegistryBlock(new BlockHammerAir(), "Hammer_Air").setProperties().removeCreativeTab());

		sapphire_ore = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new SonarRegistryBlock(new BlockSapphireOre(), "SapphireOre").setProperties(3.0F, 5.0F));

		info_reader = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryConnectableMultipart(new BlockAbstractReader(PL2Multiparts.INFO_READER)));
		inventory_reader = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryConnectableMultipart(new BlockAbstractReader(PL2Multiparts.INVENTORY_READER)));
		fluid_reader = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryConnectableMultipart(new BlockAbstractReader(PL2Multiparts.FLUID_READER)));
		energy_reader = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryConnectableMultipart(new BlockAbstractReader(PL2Multiparts.ENERGY_READER)));
		network_reader = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryConnectableMultipart(new BlockAbstractReader(PL2Multiparts.NETWORK_READER)));

		array = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryConnectableMultipart(new BlockArray()));
		entity_node = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryConnectableMultipart(new BlockEntityNode()));
		transfer_node = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryConnectableMultipart(new BlockTransferNode()));
		redstone_node = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryConnectableMultipart(new BlockRedstoneNode()));

		clock = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryConnectableMultipart(new BlockClock()));
		redstone_signaller = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryConnectableMultipart(new BlockRedstoneSignaller()));

		mini_display = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockMiniDisplay()));
		display_screen = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockDisplayScreen()));
		large_display_screen = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockLargeDisplay()));
		holographic_display = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockHolographicDisplay()));
		advanced_holographic_display = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockAdvancedHolographicDisplay()));
		
		data_cable = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockDataCable()));
		redstone_cable = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryMultipart(new BlockRedstoneCable()));
		node = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryConnectableMultipart(new BlockNode()));
		data_emitter = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryConnectableMultipart(new BlockDataEmitter()));
		data_receiver = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryConnectableMultipart(new BlockDataReceiver()));
		redstone_emitter = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryConnectableMultipart(new BlockRedstoneEmitter()));
		redstone_receiver = SonarRegister.addBlock(PL2Constants.MODID, PL2.creativeTab, new LogisticsRegistryConnectableMultipart(new BlockRedstoneReceiver()));

	}

	public static class LogisticsRegistryConnectableMultipart<T extends BlockLogistics & IMultipart> extends LogisticsRegistryMultipart<T> {

		public LogisticsRegistryConnectableMultipart(T block) {
			super(block);
		}

		public LogisticsRegistryConnectableMultipart(T block, PL2Multiparts multipart) {
			super(block,multipart);
		}

		@Override
		public Item getItemBlock() {
			return new ItemConnectableMultipart(value);
		}
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
		@Override
		public String getTileEntityRegistryName() {
			return "pl2_" + super.getRegistryName();
		}
	}

}
