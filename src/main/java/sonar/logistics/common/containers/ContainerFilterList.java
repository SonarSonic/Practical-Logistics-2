package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.api.filters.IFilteredTile;

public class ContainerFilterList extends ContainerMultipartSync {

	public IFilteredTile tile;

	public ContainerFilterList(EntityPlayer player, IFilteredTile tile) {
		super((TileSonarMultipart) tile);
		this.tile = tile;

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 41 + j * 18, 174 + i * 18));
			}
		}

		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(player.inventory, i, 41 + i * 18, 232));
		}
	}
}