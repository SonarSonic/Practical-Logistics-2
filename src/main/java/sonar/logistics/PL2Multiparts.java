package sonar.logistics;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import sonar.core.translate.Localisation;
import sonar.logistics.common.multiparts2.*;
import sonar.logistics.common.multiparts2.cables.TileDataCable;
import sonar.logistics.common.multiparts2.nodes.TileNode;
import sonar.logistics.common.multiparts2.wireless.TileDataEmitter;
import sonar.logistics.common.multiparts2.wireless.TileDataReceiver;

public enum PL2Multiparts {
	NODE(0.875, 0, 0.0625, "Node", TileNode.class, PL2Translate.NODE), //
	DATA_CABLE(0, 0, 0, "DataCable", TileDataCable.class, PL2Translate.CABLE), //
	INFO_READER(6 * 0.0625, 0.0625 * 1, 0.0625 * 5, "InfoReader", TileInfoReader.class, PL2Translate.INFO_READER),//
	DATA_EMITTER(0.0625 * 5, 0.0625 / 2, 0.0625 * 4, "DataEmitter", TileDataEmitter.class, PL2Translate.DATA_EMITTER), //
	DATA_RECEIVER(0.0625 * 5, 0.0625 / 2, 0.0625 * 4, "DataReceiver", TileDataReceiver.class, PL2Translate.DATA_RECEIVER); //
	/*
	ARRAY(0.625, 0.0625 * 1, 0.0625 * 4, PL2Translate.ARRAY), //
	CLOCK(3 * 0.0625, 0.0625 * 1, 0.0625 * 3, PL2Translate.CLOCK), //
	DISPLAY_SCREEN(0, 0, 0, PL2Translate.DISPLAY_SCREEN), //
	ENERGY_READER(6 * 0.0625, 0.0625 * 1, 0.0625 * 5, PL2Translate.ENERGY_READER), //
	ENTITY_NODE(5 * 0.0625, 0.0625 * 1, 0.0625 * 4, PL2Translate.ENTITY_NODE), //
	FLUID_READER(6 * 0.0625, 0.0625 * 1, 0.0625 * 5, PL2Translate.FLUID_READER), //
	INVENTORY_READER(6 * 0.0625, 0.0625 * 1, 0.0625 * 5, PL2Translate.INVENTORY_READER), //
	LARGE_DISPLAY_SCREEN(0, 0, 0, PL2Translate.LARGE_DISPLAY_SCREEN), //
	
	
	REDSTONE_SIGNALLER(3 * 0.0625, 0.0625 * 1, 0.0625 * 6, PL2Translate.REDSTONE_SIGNALLER), //
	TRANSFER_NODE(0.0625 * 8, 0, 0.0625 * 2, PL2Translate.TRANSFER_NODE);//
	*/
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
