package sonar.logistics.networking.info;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.logistics.PL2Config;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.handlers.IEntityInfoProvider;
import sonar.logistics.api.info.handlers.ITileInfoProvider;
import sonar.logistics.api.lists.types.InfoChangeableList;
import sonar.logistics.api.networks.IEntityMonitorHandler;
import sonar.logistics.api.networks.INetworkListChannels;
import sonar.logistics.api.networks.ITileMonitorHandler;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.api.register.TileHandlerMethod;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.info.LogicInfoRegistry;
import sonar.logistics.networking.common.ListNetworkHandler;

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
		LogicInfoRegistry.INSTANCE.getTileInfo(providedInfo, face, world, state, pos, face, block, tile);

		for (ITileInfoProvider handler : LogicInfoRegistry.INSTANCE.tileProviders) {
			if (handler.canProvide(world, state, pos, face, tile, block)) {
				TileHandlerMethod method = new TileHandlerMethod(handler);
				LogicPath path = new LogicPath();
				path.setStart(method);
				handler.provide(LogicInfoRegistry.INSTANCE, providedInfo, path, null, world, state, pos, face, block, tile);
			}
		}
		providedInfo.forEach(info -> list.add(info));		
		
		return (L) list;
	}

	@Override
	public L updateInfo(InfoNetworkChannels channels, InfoChangeableList list, EntityConnection connection) {
		Entity entity = connection.entity;
		World world = entity.getEntityWorld();
		List<IProvidableInfo> providedInfo = new ArrayList<>();
		LogicInfoRegistry.INSTANCE.getEntityInfo(providedInfo, entity);
		for (IEntityInfoProvider handler : LogicInfoRegistry.INSTANCE.entityProviders) {
			if (handler.canProvide(world, entity)) {
				handler.provide(providedInfo, world, entity);
			}
		}
		providedInfo.forEach(info -> list.add(info));	
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
