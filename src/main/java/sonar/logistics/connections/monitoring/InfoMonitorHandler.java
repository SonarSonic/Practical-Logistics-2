package sonar.logistics.connections.monitoring;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.logistics.PL2;
import sonar.logistics.api.asm.EntityMonitorHandler;
import sonar.logistics.api.asm.TileMonitorHandler;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.info.ICustomEntityHandler;
import sonar.logistics.api.info.ICustomTileHandler;
import sonar.logistics.api.info.IEntityMonitorHandler;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.ITileMonitorHandler;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.api.nodes.EntityConnection;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.api.register.TileHandlerMethod;
import sonar.logistics.info.LogicInfoRegistry;

@EntityMonitorHandler(handlerID = InfoMonitorHandler.id, modid = PL2.MODID)
@TileMonitorHandler(handlerID = InfoMonitorHandler.id, modid = PL2.MODID)
public class InfoMonitorHandler extends LogicMonitorHandler<IProvidableInfo> implements ITileMonitorHandler<IProvidableInfo>, IEntityMonitorHandler<IProvidableInfo> {

	public static final String id = "info";

	@Override
	public String id() {
		return id;
	}

	@Override
	public MonitoredList<IProvidableInfo> updateInfo(INetworkCache network, MonitoredList<IProvidableInfo> previousList, BlockConnection connection) {
		MonitoredList<IProvidableInfo> list = MonitoredList.<IProvidableInfo>newMonitoredList(network.getNetworkID());
		EnumFacing face = connection.face.getOpposite();
		World world = connection.coords.getWorld();
		IBlockState state = connection.coords.getBlockState(world);
		BlockPos pos = connection.coords.getBlockPos();
		Block block = connection.coords.getBlock(world);
		TileEntity tile = connection.coords.getTileEntity(world);
		LogicInfoRegistry.INSTANCE.getTileInfo(list, face, world, state, pos, face, block, tile);

		for (ICustomTileHandler handler : LogicInfoRegistry.INSTANCE.customTileHandlers) {
			if (handler.canProvideInfo(world, state, pos, face, tile, block)) {
				TileHandlerMethod method = new TileHandlerMethod(handler);
				LogicPath path = new LogicPath();
				path.setStart(method);
				handler.addInfo(LogicInfoRegistry.INSTANCE, list, path, null, world, state, pos, face, block, tile);
			}
		}
		return list;
	}

	@Override
	public MonitoredList<IProvidableInfo> updateInfo(INetworkCache network, MonitoredList<IProvidableInfo> previousList, EntityConnection connection) {
		MonitoredList<IProvidableInfo> list = MonitoredList.<IProvidableInfo>newMonitoredList(network.getNetworkID());
		Entity entity = connection.entity;
		World world = entity.getEntityWorld();
		LogicInfoRegistry.INSTANCE.getEntityInfo(list, entity);
		for (ICustomEntityHandler handler : LogicInfoRegistry.INSTANCE.customEntityHandlers) {
			if (handler.canProvideInfo(world, entity)) {
				handler.addInfo(list, world, entity);
			}
		}
		return list;
	}
}
