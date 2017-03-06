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
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.common.multiparts.DataCablePart;
import sonar.logistics.common.multiparts.DataEmitterPart;
import sonar.logistics.common.multiparts.DataReceiverPart;
import sonar.logistics.common.multiparts.DisplayScreenPart;
import sonar.logistics.common.multiparts.InfoReaderPart;
import sonar.logistics.common.multiparts.InventoryReaderPart;
import sonar.logistics.common.multiparts.NodePart;
import sonar.logistics.common.multiparts.RedstoneSignallerPart;
import sonar.logistics.connections.monitoring.MonitoredItemStack;
import sonar.logistics.info.LogicInfoRegistry.RegistryType;
import sonar.logistics.info.types.LogicInfo;
import sonar.logistics.info.types.ProgressInfo;

public class ExampleConfigurations {

	public static class FurnaceProgress extends Logistics3DRenderer {
		public static final FurnaceProgress instance2 = new FurnaceProgress();

		public FurnaceProgress() {
			super(4);
			NodePart node = new NodePart(EnumFacing.DOWN);
			InfoReaderPart reader = new InfoReaderPart(EnumFacing.NORTH);
			DisplayScreenPart screen = new DisplayScreenPart(EnumFacing.NORTH, EnumFacing.NORTH);
			LogicInfo info1 = LogicInfo.buildDirectInfo("TileEntityFurnace.cookTime", RegistryType.TILE, 100, null);
			LogicInfo info2 = LogicInfo.buildDirectInfo("TileEntityFurnace.totalCookTime", RegistryType.TILE, 200, null);
			screen.container.storedInfo.get(0).cachedInfo = new ProgressInfo(info1, info2);

			MultipartStateOverride cable = new MultipartStateOverride(new DataCablePart()) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(DataCablePart.DOWN, CableRenderType.INTERNAL);
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
			NodePart node = new NodePart(EnumFacing.DOWN);
			InventoryReaderPart reader = new InventoryReaderPart(EnumFacing.NORTH);
			DisplayScreenPart screen = new DisplayScreenPart(EnumFacing.NORTH, EnumFacing.NORTH);

			screen.container.storedInfo.get(0).cachedInfo = new MonitoredItemStack(new StoredItemStack(new ItemStack(Blocks.COBBLESTONE), 256), -1);

			MultipartStateOverride cable = new MultipartStateOverride(new DataCablePart()) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(DataCablePart.DOWN, CableRenderType.INTERNAL);
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

			NodePart node = new NodePart(EnumFacing.NORTH);
			MultipartStateOverride cable = new MultipartStateOverride(new DataCablePart()) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(DataCablePart.EAST, CableRenderType.INTERNAL).withProperty(DataCablePart.NORTH, CableRenderType.INTERNAL);
				}
			};
			DataEmitterPart emitter = new DataEmitterPart();
			emitter.face.setObject(EnumFacing.EAST);
			addMultiparts(Lists.newArrayList(node, cable, emitter), new BlockPos(1, 0, 1));
			

			InfoReaderPart reader = new InfoReaderPart(EnumFacing.NORTH);
			DataReceiverPart receiver = new DataReceiverPart();
			receiver.face.setObject(EnumFacing.WEST);
			MultipartStateOverride cable2 = new MultipartStateOverride(new DataCablePart()) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(DataCablePart.WEST, CableRenderType.INTERNAL);
				}
			};
			
			addMultiparts(Lists.newArrayList(reader, cable2, receiver), new BlockPos(-1, 0, 1));	
			MultipartStateOverride signaller = new MultipartStateOverride(new RedstoneSignallerPart(EnumFacing.NORTH)) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(RedstoneSignallerPart.ACTIVE, true);
				}
			};
			
			MultipartStateOverride cable3 = new MultipartStateOverride(new DataCablePart()) {
				public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
					return super.getActualState(state, world, pos).withProperty(DataCablePart.NORTH, CableRenderType.HALF).withProperty(DataCablePart.SOUTH, CableRenderType.INTERNAL);
				}
			};
			addMultiparts(Lists.newArrayList(signaller, cable3), new BlockPos(-1, 0, 0));	
		}

	}

}
