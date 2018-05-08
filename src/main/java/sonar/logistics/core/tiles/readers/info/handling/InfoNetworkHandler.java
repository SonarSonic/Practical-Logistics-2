package sonar.logistics.core.tiles.readers.info.handling;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.logistics.PL2Config;
import sonar.logistics.api.core.tiles.displays.info.IProvidableInfo;
import sonar.logistics.api.core.tiles.displays.info.handlers.IEntityInfoProvider;
import sonar.logistics.api.core.tiles.displays.info.handlers.ITileInfoProvider;
import sonar.logistics.api.core.tiles.displays.info.register.LogicPath;
import sonar.logistics.api.core.tiles.readers.channels.IEntityMonitorHandler;
import sonar.logistics.api.core.tiles.readers.channels.INetworkListChannels;
import sonar.logistics.api.core.tiles.readers.channels.ITileMonitorHandler;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.channels.EntityConnection;
import sonar.logistics.base.channels.handling.ListNetworkHandler;
import sonar.logistics.core.tiles.displays.info.MasterInfoRegistry;
import sonar.logistics.core.tiles.displays.info.paths.TileHandlerMethod;
import sonar.logistics.core.tiles.displays.info.types.general.InfoChangeableList;

import java.util.ArrayList;
import java.util.List;

public class InfoNetworkHandler<I extends IProvidableInfo<I>, L extends InfoChangeableList<I>, C extends InfoNetworkChannels> extends ListNetworkHandler<I,L> implements ITileMonitorHandler<I, L, C>, IEntityMonitorHandler<I, L, C> {
	
	public static InfoNetworkHandler INSTANCE = new InfoNetworkHandler();

	@Override
	public Class<? extends INetworkListChannels> getChannelsType(){
		return InfoNetworkChannels.class;	
	}

	@Override
	public L updateInfo(InfoNetworkChannels channels, InfoChangeableList list, BlockConnection connection) {
		EnumFacing face = connection.face.getOpposite();
		World world = connection.coords.getWorld();
		IBlockState state = connection.coords.getBlockState(world);
		BlockPos pos = connection.coords.getBlockPos();
		Block block = connection.coords.getBlock(world);
		TileEntity tile = connection.coords.getTileEntity(world);
		List<IProvidableInfo> providedInfo = new ArrayList<>();
		MasterInfoRegistry.INSTANCE.getTileInfo(providedInfo, face, world, state, pos, face, block, tile);

		for (ITileInfoProvider handler : MasterInfoRegistry.INSTANCE.tileProviders) {
			if (handler.canProvide(world, state, pos, face, tile, block)) {
				TileHandlerMethod method = new TileHandlerMethod(handler);
				LogicPath path = new LogicPath();
				path.setStart(method);
				handler.provide(MasterInfoRegistry.INSTANCE, providedInfo, path, null, world, state, pos, face, block, tile);
			}
		}
		providedInfo.forEach(list::add);
		
		return (L) list;
	}

	@Override
	public L updateInfo(InfoNetworkChannels channels, InfoChangeableList list, EntityConnection connection) {
		Entity entity = connection.entity;
		World world = entity.getEntityWorld();
		List<IProvidableInfo> providedInfo = new ArrayList<>();
		MasterInfoRegistry.INSTANCE.getEntityInfo(providedInfo, entity);
		for (IEntityInfoProvider handler : MasterInfoRegistry.INSTANCE.entityProviders) {
			if (handler.canProvide(world, entity)) {
				handler.provide(providedInfo, world, entity);
			}
		}
		providedInfo.forEach(list::add);
		return (L) list;
	}

	@Override
	public int updateRate() {
		return PL2Config.infoUpdate;
	}

	@Override
	public L newChangeableList() {
		return (L) new InfoChangeableList();
	}
}
