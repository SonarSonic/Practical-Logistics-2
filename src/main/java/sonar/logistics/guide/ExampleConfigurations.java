package sonar.logistics.guide;

import java.util.List;

import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.common.block.properties.SonarProperties;
import sonar.logistics.PL2Blocks;
import sonar.logistics.PL2Items;
import sonar.logistics.api.PL2Properties;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.ItemChangeableList;
import sonar.logistics.api.register.RegistryType;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayConnections;
import sonar.logistics.common.multiparts.cables.TileDataCable;
import sonar.logistics.common.multiparts.displays.BlockLargeDisplay;
import sonar.logistics.common.multiparts.displays.TileDisplayScreen;
import sonar.logistics.common.multiparts.displays.TileLargeDisplayScreen;
import sonar.logistics.common.multiparts.misc.TileRedstoneSignaller;
import sonar.logistics.common.multiparts.nodes.TileNode;
import sonar.logistics.common.multiparts.readers.TileInfoReader;
import sonar.logistics.common.multiparts.readers.TileInventoryReader;
import sonar.logistics.common.multiparts.wireless.TileDataEmitter;
import sonar.logistics.common.multiparts.wireless.TileDataReceiver;
import sonar.logistics.info.types.LogicInfo;
import sonar.logistics.info.types.LogicInfoList;
import sonar.logistics.info.types.MonitoredItemStack;
import sonar.logistics.info.types.ProgressInfo;

public class ExampleConfigurations {

	public static class FurnaceProgress extends Logistics3DRenderer {
		public static final FurnaceProgress instance2 = new FurnaceProgress();

		public FurnaceProgress() {
			super(4);
			addBlock(new BlockPos(0, 0, 0), PL2Blocks.node.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.DOWN), new TileNode());
			addBlock(new BlockPos(0, 0, 0), PL2Blocks.info_reader.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.NORTH), new TileInfoReader());
			addBlock(new BlockPos(0, 0, 0), PL2Blocks.data_cable.getDefaultState().withProperty(PL2Properties.DOWN, CableRenderType.INTERNAL), new TileDataCable());
			TileDisplayScreen screen = new TileDisplayScreen();
			LogicInfo info1 = LogicInfo.buildDirectInfo("TileEntityFurnace.cookTime", RegistryType.TILE, 100);
			LogicInfo info2 = LogicInfo.buildDirectInfo("TileEntityFurnace.totalCookTime", RegistryType.TILE, 200);
			screen.container().storedInfo.get(0).cachedInfo = new ProgressInfo(info1, info2);
			addBlock(new BlockPos(0, 0, 0), PL2Blocks.display_screen.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.NORTH).withProperty(SonarProperties.ROTATION, EnumFacing.NORTH), screen);
			addBlock(new BlockPos(0, -1, 0), Blocks.LIT_FURNACE.getDefaultState());
		}
	}

	public static class InventoryExample extends Logistics3DRenderer {

		public static final InventoryExample instance = new InventoryExample();

		public InventoryExample() {
			super(4);
			addBlock(new BlockPos(0, 0, 0), PL2Blocks.node.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.DOWN), new TileNode());
			addBlock(new BlockPos(0, 0, 0), PL2Blocks.inventory_reader.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.NORTH), new TileInventoryReader());
			addBlock(new BlockPos(0, 0, 0), PL2Blocks.data_cable.getDefaultState().withProperty(PL2Properties.DOWN, CableRenderType.INTERNAL), new TileDataCable());

			TileDisplayScreen screen = new TileDisplayScreen();
			screen.container().storedInfo.get(0).cachedInfo = new MonitoredItemStack(new StoredItemStack(new ItemStack(Blocks.COBBLESTONE), 256), -1);
			addBlock(new BlockPos(0, 0, 0), PL2Blocks.display_screen.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.NORTH), screen);
			addBlock(new BlockPos(0, -1, 0), Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.SOUTH), new TileEntityChest());
		}
	}

	public static class WirelessRedstone extends Logistics3DRenderer {

		public WirelessRedstone() {
			super(16);
			addBlock(new BlockPos(1, 0, 0), Blocks.REDSTONE_WIRE.getDefaultState().withProperty(BlockRedstoneWire.POWER, 15));
			addBlock(new BlockPos(-1, 0, -1), Blocks.REDSTONE_WIRE.getDefaultState().withProperty(BlockRedstoneWire.POWER, 15));

			addBlock(new BlockPos(1, 0, -1), Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, BlockLever.EnumOrientation.UP_Z).withProperty(BlockLever.POWERED, true));

			addBlock(new BlockPos(1, 0, 1), PL2Blocks.node.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.NORTH), new TileNode());
			addBlock(new BlockPos(1, 0, 1), PL2Blocks.data_cable.getDefaultState().withProperty(PL2Properties.EAST, CableRenderType.INTERNAL).withProperty(PL2Properties.NORTH, CableRenderType.INTERNAL), new TileDataCable());
			addBlock(new BlockPos(1, 0, 1), PL2Blocks.data_emitter.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.EAST), new TileDataEmitter());

			addBlock(new BlockPos(-1, 0, 1), PL2Blocks.info_reader.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.NORTH).withProperty(PL2Properties.HASDISPLAY, false), new TileInfoReader());
			addBlock(new BlockPos(-1, 0, 1), PL2Blocks.data_receiver.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.WEST), new TileDataReceiver());
			addBlock(new BlockPos(-1, 0, 1), PL2Blocks.data_cable.getDefaultState().withProperty(PL2Properties.WEST, CableRenderType.INTERNAL), new TileDataCable());

			addBlock(new BlockPos(-1, 0, 0), PL2Blocks.redstone_signaller.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.NORTH), new TileRedstoneSignaller());
			addBlock(new BlockPos(-1, 0, 0), PL2Blocks.data_cable.getDefaultState().withProperty(PL2Properties.SOUTH, CableRenderType.CABLE), new TileDataCable());

		}

	}

	public static class MultipleInventory extends Logistics3DRenderer {

		public MultipleInventory() {
			super(16);
			ConnectedDisplay fullDisplay = new ConnectedDisplay(-1);
			fullDisplay.width.setObject(2);
			fullDisplay.height.setObject(0);
			TileLargeDisplayScreen screen1 = new TileLargeDisplayScreen();

			screen1.overrideDisplay = fullDisplay;
			screen1.shouldRender.setObject(true);
			fullDisplay.topLeftScreen = screen1;
			fullDisplay.container().resetRenderProperties();

			ItemChangeableList list = new ItemChangeableList();
			list.add(new StoredItemStack(new ItemStack(PL2Items.sapphire), 512));
			list.add(new StoredItemStack(new ItemStack(PL2Items.stone_plate), 256));
			list.add(new StoredItemStack(new ItemStack(Blocks.COBBLESTONE), 256));
			list.add(new StoredItemStack(new ItemStack(Blocks.SAND), 256));
			list.add(new StoredItemStack(new ItemStack(Blocks.LOG, 1, 0), 128));
			list.add(new StoredItemStack(new ItemStack(Blocks.LOG, 1, 1), 128));
			list.add(new StoredItemStack(new ItemStack(Blocks.LOG, 1, 2), 128));
			list.add(new StoredItemStack(new ItemStack(Blocks.LOG, 1, 3), 128));
			list.add(new StoredItemStack(new ItemStack(Blocks.PLANKS, 1, 0), 64));
			list.add(new StoredItemStack(new ItemStack(Blocks.PLANKS, 1, 1), 64));
			list.add(new StoredItemStack(new ItemStack(Blocks.PLANKS, 1, 2), 64));
			list.add(new StoredItemStack(new ItemStack(Blocks.PLANKS, 1, 3), 64));
			list.add(new StoredItemStack(new ItemStack(Blocks.DIRT), 64));
			list.add(new StoredItemStack(new ItemStack(Blocks.GLOWSTONE), 64));
			list.add(new StoredItemStack(new ItemStack(Blocks.IRON_ORE), 64));
			list.add(new StoredItemStack(new ItemStack(PL2Blocks.data_cable), 16));
			list.add(new StoredItemStack(new ItemStack(PL2Blocks.info_reader), 4));
			list.add(new StoredItemStack(new ItemStack(PL2Items.operator), 1));

			fullDisplay.container().storedInfo.get(0).cachedInfo = new LogicInfoList() {
				{
					infoID.setObject(MonitoredItemStack.id);
					listChanged = false;
				}

				public List<MonitoredItemStack> getCachedList(InfoUUID id) {
					return list.createSaveableList();
				}
			};

			addBlock(new BlockPos(1, 0, 0), PL2Blocks.node.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.DOWN), new TileNode());
			addBlock(new BlockPos(1, 0, 0), PL2Blocks.large_display_screen.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.NORTH).withProperty(BlockLargeDisplay.TYPE, DisplayConnections.ONE_E), screen1);
			addBlock(new BlockPos(1, 0, 0), PL2Blocks.data_cable.getDefaultState().withProperty(PL2Properties.DOWN, CableRenderType.INTERNAL).withProperty(PL2Properties.WEST, CableRenderType.CABLE).withProperty(PL2Properties.NORTH, CableRenderType.INTERNAL), new TileDataCable());

			addBlock(new BlockPos(-1, 0, 0), PL2Blocks.node.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.DOWN), new TileNode());
			addBlock(new BlockPos(-1, 0, 0), PL2Blocks.large_display_screen.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.NORTH).withProperty(BlockLargeDisplay.TYPE, DisplayConnections.ONE_W), new TileLargeDisplayScreen());
			addBlock(new BlockPos(-1, 0, 0), PL2Blocks.data_cable.getDefaultState().withProperty(PL2Properties.DOWN, CableRenderType.INTERNAL).withProperty(PL2Properties.EAST, CableRenderType.CABLE).withProperty(PL2Properties.NORTH, CableRenderType.INTERNAL), new TileDataCable());

			addBlock(new BlockPos(0, 0, 0), PL2Blocks.inventory_reader.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.NORTH), new TileInventoryReader());
			addBlock(new BlockPos(0, 0, 0), PL2Blocks.node.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.DOWN), new TileNode());
			addBlock(new BlockPos(0, 0, 0), PL2Blocks.large_display_screen.getDefaultState().withProperty(SonarProperties.ORIENTATION, EnumFacing.NORTH).withProperty(BlockLargeDisplay.TYPE, DisplayConnections.OPPOSITE_2), new TileLargeDisplayScreen());
			addBlock(new BlockPos(0, 0, 0), PL2Blocks.data_cable.getDefaultState().withProperty(PL2Properties.DOWN, CableRenderType.INTERNAL).withProperty(PL2Properties.EAST, CableRenderType.CABLE).withProperty(PL2Properties.WEST, CableRenderType.CABLE).withProperty(PL2Properties.NORTH, CableRenderType.INTERNAL), new TileDataCable());

			IBlockState chestState = Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.SOUTH);
			addBlock(new BlockPos(0, -1, 0), chestState, new TileEntityChest());
			addBlock(new BlockPos(1, -1, 0), chestState, new TileEntityChest());
			addBlock(new BlockPos(-1, -1, 0), chestState, new TileEntityChest());

		}

	}

}
