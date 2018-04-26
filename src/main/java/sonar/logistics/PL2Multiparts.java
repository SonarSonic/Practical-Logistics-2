package sonar.logistics;

import net.minecraft.tileentity.TileEntity;
import sonar.core.translate.Localisation;
import sonar.logistics.common.multiparts.cables.TileDataCable;
import sonar.logistics.common.multiparts.cables.TileRedstoneCable;
import sonar.logistics.common.multiparts.displays.TileDisplayScreen;
import sonar.logistics.common.multiparts.displays.TileHolographicDisplay;
import sonar.logistics.common.multiparts.displays.TileLargeDisplayScreen;
import sonar.logistics.common.multiparts.displays.TileMiniDisplay;
import sonar.logistics.common.multiparts.misc.TileClock;
import sonar.logistics.common.multiparts.misc.TileRedstoneSignaller;
import sonar.logistics.common.multiparts.nodes.TileArray;
import sonar.logistics.common.multiparts.nodes.TileEntityNode;
import sonar.logistics.common.multiparts.nodes.TileNode;
import sonar.logistics.common.multiparts.nodes.TileRedstoneNode;
import sonar.logistics.common.multiparts.nodes.TileTransferNode;
import sonar.logistics.common.multiparts.readers.TileEnergyReader;
import sonar.logistics.common.multiparts.readers.TileFluidReader;
import sonar.logistics.common.multiparts.readers.TileInfoReader;
import sonar.logistics.common.multiparts.readers.TileInventoryReader;
import sonar.logistics.common.multiparts.readers.TileNetworkReader;
import sonar.logistics.common.multiparts.wireless.TileDataEmitter;
import sonar.logistics.common.multiparts.wireless.TileDataReceiver;
import sonar.logistics.common.multiparts.wireless.TileRedstoneEmitter;
import sonar.logistics.common.multiparts.wireless.TileRedstoneReceiver;

public enum PL2Multiparts {
	NODE(0.875, 0, 0.0625, "Node", TileNode.class, PL2Translate.NODE), //
	ARRAY(0.625, 0.0625 * 1, 0.0625 * 4, "Array", TileArray.class, PL2Translate.ARRAY), //
	ENTITY_NODE(5 * 0.0625, 0.0625 * 1, 0.0625 * 4, "EntityNode", TileEntityNode.class, PL2Translate.ENTITY_NODE), //
	DATA_CABLE(0, 0, 0, "DataCable", TileDataCable.class, PL2Translate.CABLE), //
	REDSTONE_CABLE(0, 0, 0, "RedstoneCable", TileRedstoneCable.class, PL2Translate.REDSTONE_CABLE), //
	INFO_READER(6 * 0.0625, 0.0625 * 1, 0.0625 * 5, "InfoReader", TileInfoReader.class, PL2Translate.INFO_READER), //
	FLUID_READER(6 * 0.0625, 0.0625 * 1, 0.0625 * 5, "FluidReader", TileFluidReader.class, PL2Translate.FLUID_READER), //
	ENERGY_READER(6 * 0.0625, 0.0625 * 1, 0.0625 * 5, "EnergyReader", TileEnergyReader.class, PL2Translate.ENERGY_READER), //
	INVENTORY_READER(6 * 0.0625, 0.0625 * 1, 0.0625 * 5, "InventoryReader", TileInventoryReader.class, PL2Translate.INVENTORY_READER), //
	NETWORK_READER(6 * 0.0625, 0.0625 * 1, 0.0625 * 5, "NetworkReader", TileNetworkReader.class, PL2Translate.NETWORK_READER), //
	DATA_EMITTER(0.0625 * 5, 0.0625 / 2, 0.0625 * 4, "DataEmitter", TileDataEmitter.class, PL2Translate.DATA_EMITTER), //
	DATA_RECEIVER(0.0625 * 5, 0.0625 / 2, 0.0625 * 4, "DataReceiver", TileDataReceiver.class, PL2Translate.DATA_RECEIVER), //
	REDSTONE_EMITTER(0.0625 * 5, 0.0625 / 2, 0.0625 * 4, "RedstoneEmitter", TileRedstoneEmitter.class, PL2Translate.REDSTONE_EMITTER), //
	REDSTONE_RECEIVER(0.0625 * 5, 0.0625 / 2, 0.0625 * 4, "RedstoneReceiver", TileRedstoneReceiver.class, PL2Translate.REDSTONE_RECEIVER), //
	CLOCK(3 * 0.0625, 0.0625 * 1, 0.0625 * 3, "Clock", TileClock.class, PL2Translate.CLOCK), //
	REDSTONE_SIGNALLER(3 * 0.0625, 0.0625 * 1, 0.0625 * 6, "RedstoneSignaller", TileRedstoneSignaller.class, PL2Translate.REDSTONE_SIGNALLER), //
	TRANSFER_NODE(0.0625 * 8, 0, 0.0625 * 2, "TransferNode", TileTransferNode.class, PL2Translate.TRANSFER_NODE), //
	REDSTONE_NODE(0.0625 * 8, 0, 0.0625 * 2, "RedstoneNode", TileRedstoneNode.class, PL2Translate.REDSTONE_NODE), //

	DISPLAY_SCREEN(0, 0, 0, "DisplayScreen", TileDisplayScreen.class, PL2Translate.DISPLAY_SCREEN), //
	MINI_DISPLAY(0, 0, 0, "MiniDisplay", TileMiniDisplay.class, PL2Translate.MINI_DISPLAY), //
	HOLOGRAPHIC_DISPLAY(0, 0, 0, "HolographicDisplay", TileHolographicDisplay.class, PL2Translate.HOLOGRAPHIC_DISPLAY), //
	LARGE_DISPLAY_SCREEN(0, 0, 0, "LargeDisplayScreen", TileLargeDisplayScreen.class, PL2Translate.LARGE_DISPLAY_SCREEN); // //
	public double width, heightMin, heightMax;
	public Class<? extends TileEntity> tile;
	public String registryName;
	private Localisation localisation;

	PL2Multiparts(double width, double heightMin, double heightMax, String registryName, Class<? extends TileEntity> tile, Localisation localisation) {
		this.width = width;
		this.heightMin = heightMin;
		this.heightMax = heightMax;
		this.tile = tile;
		this.registryName = registryName;
		this.localisation = localisation;
	}

	public Class<? extends TileEntity> getTileClass() {
		return tile;
	}

	public TileEntity createTileEntity() {
		try {
			return tile.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getRegistryName() {
		return registryName;
	}

	public String getUnlocalisedName() {
		return localisation.o();
	}

	public String getDisplayName() {
		return localisation.t();
	}
}
