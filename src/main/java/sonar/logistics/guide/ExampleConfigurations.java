package sonar.logistics.guide;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.client.gui.MultipartStateOverride;
import sonar.logistics.PL2Items;
import sonar.logistics.api.register.RegistryType;
import sonar.logistics.api.tiles.cable.PL2Properties;
import sonar.logistics.api.tiles.cable.CableRenderType;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayConnections;
import sonar.logistics.api.utils.InfoUUID;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.common.multiparts.DataCablePart;
import sonar.logistics.common.multiparts.DataEmitterPart;
import sonar.logistics.common.multiparts.DataReceiverPart;
import sonar.logistics.common.multiparts.DisplayScreenPart;
import sonar.logistics.common.multiparts.InfoReaderPart;
import sonar.logistics.common.multiparts.InventoryReaderPart;
import sonar.logistics.common.multiparts.LargeDisplayScreenPart;
import sonar.logistics.common.multiparts.NodePart;
import sonar.logistics.common.multiparts.RedstoneSignallerPart;
import sonar.logistics.info.types.LogicInfo;
import sonar.logistics.info.types.LogicInfoList;
import sonar.logistics.info.types.MonitoredItemStack;
import sonar.logistics.info.types.ProgressInfo;

public class ExampleConfigurations {

	public static class FurnaceProgress extends Logistics3DRenderer {
		public static final FurnaceProgress instance2 = new FurnaceProgress();

		public FurnaceProgress() {
			super(4);
			NodePart node = new NodePart();
			node.setCableFace(EnumFacing.DOWN);
			InfoReaderPart reader = new InfoReaderPart();
			reader.setCableFace(EnumFacing.DOWN);
			DisplayScreenPart screen = new DisplayScreenPart(EnumFacing.NORTH, EnumFacing.NORTH);
			LogicInfo info1 = LogicInfo.buildDirectInfo("TileEntityFurnace.cookTime", RegistryType.TILE, 100);
			LogicInfo info2 = LogicInfo.buildDirectInfo("TileEntityFurnace.totalCookTime", RegistryType.TILE, 200);
			screen.container.storedInfo.get(0).cachedInfo = new ProgressInfo(info1, info2);

			MultipartStateOverride cable = new MultipartStateOverride(new DataCablePart()) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(PL2Properties.DOWN, CableRenderType.INTERNAL);
				}
			};
			addMultiparts(Lists.newArrayList(node, reader, screen, cable), new BlockPos(0, 0, 0));
			addBlock(Blocks.LIT_FURNACE.getDefaultState(), new BlockPos(0, -1, 0));
		}
	}

	public static class InventoryExample extends Logistics3DRenderer {

		public static final InventoryExample instance = new InventoryExample();

		public InventoryExample() {
			super(4);
			NodePart node = new NodePart();
			node.setCableFace(EnumFacing.DOWN);
			InventoryReaderPart reader = new InventoryReaderPart();
			reader.setCableFace(EnumFacing.NORTH);
			DisplayScreenPart screen = new DisplayScreenPart(EnumFacing.NORTH, EnumFacing.NORTH);

			screen.container.storedInfo.get(0).cachedInfo = new MonitoredItemStack(new StoredItemStack(new ItemStack(Blocks.COBBLESTONE), 256), -1);

			MultipartStateOverride cable = new MultipartStateOverride(new DataCablePart()) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(PL2Properties.DOWN, CableRenderType.INTERNAL);
				}
			};
			addMultiparts(Lists.newArrayList(node, reader, screen, cable), new BlockPos(0, 0, 0));
			addBlock(Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.SOUTH), new BlockPos(0, -1, 0));
			addTileEntity(new TileEntityChest(), new BlockPos(0, -1, 0));
		}
	}

	public static class WirelessRedstone extends Logistics3DRenderer {

		public WirelessRedstone() {
			super(16);
			addBlock(Blocks.REDSTONE_WIRE.getDefaultState().withProperty(BlockRedstoneWire.POWER, 15), new BlockPos(1, 0, 0));
			addBlock(Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, BlockLever.EnumOrientation.UP_Z).withProperty(BlockLever.POWERED, true), new BlockPos(1, 0, -1));

			NodePart node = new NodePart();
			node.setCableFace(EnumFacing.NORTH);
			MultipartStateOverride cable = new MultipartStateOverride(new DataCablePart()) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(PL2Properties.EAST, CableRenderType.INTERNAL).withProperty(PL2Properties.NORTH, CableRenderType.INTERNAL);
				}
			};
			DataEmitterPart emitter = new DataEmitterPart();
			emitter.setCableFace(EnumFacing.EAST);
			addMultiparts(Lists.newArrayList(node, cable, emitter), new BlockPos(1, 0, 1));

			InfoReaderPart reader = new InfoReaderPart();
			reader.setCableFace(EnumFacing.NORTH);
			DataReceiverPart receiver = new DataReceiverPart();
			receiver.setCableFace(EnumFacing.NORTH);
			receiver.face.setObject(EnumFacing.WEST);
			MultipartStateOverride cable2 = new MultipartStateOverride(new DataCablePart()) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(PL2Properties.WEST, CableRenderType.INTERNAL);
				}
			};

			addMultiparts(Lists.newArrayList(reader, cable2, receiver), new BlockPos(-1, 0, 1));
			RedstoneSignallerPart signallerPart =  new RedstoneSignallerPart();
			signallerPart.setCableFace(EnumFacing.NORTH);
			MultipartStateOverride signaller = new MultipartStateOverride(signallerPart) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(RedstoneSignallerPart.ACTIVE, true);
				}
			};

			MultipartStateOverride cable3 = new MultipartStateOverride(new DataCablePart()) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(PL2Properties.NORTH, CableRenderType.HALF).withProperty(PL2Properties.SOUTH, CableRenderType.INTERNAL);
				}
			};
			addMultiparts(Lists.newArrayList(signaller, cable3), new BlockPos(-1, 0, 0));
		}

	}

	public static class MultipleInventory extends Logistics3DRenderer {

		public MultipleInventory() {
			super(16);

			NodePart node = new NodePart();
			node.setCableFace(EnumFacing.DOWN);
			InventoryReaderPart reader = new InventoryReaderPart();
			reader.setCableFace(EnumFacing.NORTH);
			ConnectedDisplay fullDisplay = new ConnectedDisplay(-1);
			fullDisplay.width.setObject(2);
			fullDisplay.height.setObject(0);
			LargeDisplayScreenPart screen1 = new LargeDisplayScreenPart(EnumFacing.NORTH, EnumFacing.NORTH);

			screen1.overrideDisplay = fullDisplay;
			screen1.shouldRender.setObject(true);
			fullDisplay.topLeftScreen = screen1;
			fullDisplay.container.resetRenderProperties();

			MonitoredList<MonitoredItemStack> list = MonitoredList.<MonitoredItemStack>newMonitoredList(-1);
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(PL2Items.sapphire), 512)));
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(PL2Items.stone_plate), 256)));
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(Blocks.COBBLESTONE), 256)));
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(Blocks.SAND), 256)));
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(Blocks.LOG, 1, 0), 128)));
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(Blocks.LOG, 1, 1), 128)));
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(Blocks.LOG, 1, 2), 128)));
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(Blocks.LOG, 1, 3), 128)));
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(Blocks.PLANKS, 1, 0), 64)));
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(Blocks.PLANKS, 1, 1), 64)));
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(Blocks.PLANKS, 1, 2), 64)));
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(Blocks.PLANKS, 1, 3), 64)));
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(Blocks.DIRT), 64)));
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(Blocks.GLOWSTONE), 64)));
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(Blocks.IRON_ORE), 64)));
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(PL2Items.cable), 16)));
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(PL2Items.info_reader), 4)));
			list.add(new MonitoredItemStack(new StoredItemStack(new ItemStack(PL2Items.operator), 1)));

			fullDisplay.container.storedInfo.get(0).cachedInfo = new LogicInfoList() {
				{
					infoID.setObject(MonitoredItemStack.id);
				}

				public MonitoredList<?> getCachedList(InfoUUID id) {
					return list;
				}
			};

			LargeDisplayScreenPart screen2 = new LargeDisplayScreenPart(EnumFacing.NORTH, EnumFacing.NORTH);
			LargeDisplayScreenPart screen3 = new LargeDisplayScreenPart(EnumFacing.NORTH, EnumFacing.NORTH);

			MultipartStateOverride screenState1 = new MultipartStateOverride(screen1) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(LargeDisplayScreenPart.TYPE, DisplayConnections.ONE_E);
				}

			};

			MultipartStateOverride screenState2 = new MultipartStateOverride(screen2) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(LargeDisplayScreenPart.TYPE, DisplayConnections.OPPOSITE_2);
				}

			};

			MultipartStateOverride screenState3 = new MultipartStateOverride(screen3) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(LargeDisplayScreenPart.TYPE, DisplayConnections.ONE_W);
				}

			};

			MultipartStateOverride cable1 = new MultipartStateOverride(new DataCablePart()) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(PL2Properties.DOWN, CableRenderType.INTERNAL).withProperty(PL2Properties.WEST, CableRenderType.CABLE).withProperty(PL2Properties.NORTH, CableRenderType.INTERNAL);
				}
			};

			MultipartStateOverride cable2 = new MultipartStateOverride(new DataCablePart()) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(PL2Properties.DOWN, CableRenderType.INTERNAL).withProperty(PL2Properties.EAST, CableRenderType.CABLE).withProperty(PL2Properties.WEST, CableRenderType.CABLE);
				}
			};

			MultipartStateOverride cable3 = new MultipartStateOverride(new DataCablePart()) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(PL2Properties.DOWN, CableRenderType.INTERNAL).withProperty(PL2Properties.EAST, CableRenderType.CABLE).withProperty(PL2Properties.NORTH, CableRenderType.INTERNAL);
				}
			};

			addMultiparts(Lists.newArrayList(node, screenState1, cable1), new BlockPos(1, 0, 0));
			addMultiparts(Lists.newArrayList(node, screenState3, cable3), new BlockPos(-1, 0, 0));
			addMultiparts(Lists.newArrayList(node, reader, screenState2, cable2), new BlockPos(0, 0, 0));
			IBlockState chestState = Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.SOUTH);
			addBlock(chestState, new BlockPos(0, -1, 0));
			addBlock(chestState, new BlockPos(1, -1, 0));
			addBlock(chestState, new BlockPos(-1, -1, 0));
			addTileEntity(new TileEntityChest(), new BlockPos(0, -1, 0));
			addTileEntity(new TileEntityChest(), new BlockPos(1, -1, 0));
			addTileEntity(new TileEntityChest(), new BlockPos(-1, -1, 0));

		}

	}

}
