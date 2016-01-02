package sonar.logistics.info.providers.tile;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import sonar.calculator.mod.api.IFlux;
import sonar.calculator.mod.api.IFluxPoint;
import sonar.calculator.mod.api.ITeleport;
import sonar.calculator.mod.utils.FluxRegistry;
import sonar.logistics.api.Info;
import sonar.logistics.api.StandardInfo;
import sonar.logistics.api.data.TileProvider;
import cpw.mods.fml.common.Loader;

public class CalculatorProvider extends TileProvider {

	public static String name = "Calculator-Provider";
	public String[] categories = new String[] { "Calculator" };
	public String[] subcategories = new String[] { "Flux Network ID", "Flux Network Name", "Flux Network Owner", "Max Transfer", "Priority", "Teleporter ID", "Teleporter Name" };

	@Override
	public String helperName() {
		return name;
	}

	@Override
	public boolean canProvideInfo(World world, int x, int y, int z, ForgeDirection dir) {
		TileEntity target = world.getTileEntity(x, y, z);
		if (target != null) {
			if (target instanceof IFlux) {
				return true;
			}
			if (target instanceof ITeleport) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void getHelperInfo(List<Info> infoList, World world, int x, int y, int z, ForgeDirection dir) {
		byte id = this.getID();
		TileEntity target = world.getTileEntity(x, y, z);
		if (target != null) {
			if (target instanceof IFlux) {
				IFlux flux = (IFlux) target;
				infoList.add(new StandardInfo(id, 0, 1, flux.networkID()));
				infoList.add(new StandardInfo(id, 0, 2, FluxRegistry.getNetwork(flux.networkID())));
				infoList.add(new StandardInfo(id, 0, 3, flux.masterName()));
				if (target instanceof IFluxPoint) {
					IFluxPoint plug = (IFluxPoint) target;
					infoList.add(new StandardInfo(id, 0, 4, plug.maxTransfer()));
					infoList.add(new StandardInfo(id, 0, 5, plug.priority()));
				}
			}
			if (target instanceof ITeleport) {
				ITeleport teleporter = (ITeleport) target;
				infoList.add(new StandardInfo(id, 0, 6, teleporter.teleporterID()));
				infoList.add(new StandardInfo(id, 0, 7, teleporter.name()));
			}
		}

	}

	public boolean isLoadable() {
		return Loader.isModLoaded("Calculator");
	}

	@Override
	public String getCategory(byte id) {
		return categories[id];
	}

	@Override
	public String getSubCategory(byte id) {
		return subcategories[id];
	}
}
