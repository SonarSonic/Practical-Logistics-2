package sonar.logistics.connections.monitoring;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.utils.SimpleProfiler;
import sonar.logistics.Logistics;
import sonar.logistics.api.asm.EntityMonitorHandler;
import sonar.logistics.api.asm.TileMonitorHandler;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.info.ICustomEntityHandler;
import sonar.logistics.api.info.ICustomTileHandler;
import sonar.logistics.api.info.IEntityMonitorHandler;
import sonar.logistics.api.info.ITileMonitorHandler;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.info.LogicInfoRegistry;
import sonar.logistics.info.LogicInfoRegistry.LogicPath;
import sonar.logistics.info.LogicInfoRegistry.TileHandlerMethod;
import sonar.logistics.info.types.LogicInfo;

@EntityMonitorHandler(handlerID = InfoMonitorHandler.id, modid = Logistics.MODID)
@TileMonitorHandler(handlerID = InfoMonitorHandler.id, modid = Logistics.MODID)
public class InfoMonitorHandler extends LogicMonitorHandler<LogicInfo> implements ITileMonitorHandler<LogicInfo>, IEntityMonitorHandler<LogicInfo> {

	public static final String id = "info";

	@Override
	public String id() {
		return id;
	}

	@Override
	public MonitoredList<LogicInfo> updateInfo(INetworkCache network, MonitoredList<LogicInfo> previousList, NodeConnection connection) {
		MonitoredList<LogicInfo> list = MonitoredList.<LogicInfo>newMonitoredList(network.getNetworkID());
		EnumFacing face = connection.face.getOpposite();
		World world = connection.coords.getWorld();
		IBlockState state = connection.coords.getBlockState(world);
		BlockPos pos = connection.coords.getBlockPos();
		Block block = connection.coords.getBlock(world);
		TileEntity tile = connection.coords.getTileEntity(world);
		LogicInfoRegistry.getTileInfo(list, face, world, state, pos, face, block, tile);

		for (ICustomTileHandler handler : LogicInfoRegistry.customTileHandlers) {
			if (handler.canProvideInfo(world, state, pos, face, tile, block)) {
				TileHandlerMethod method = new TileHandlerMethod(handler);
				LogicPath path = new LogicPath();
				path.setStart(method);
				handler.addInfo(list, path, world, state, pos, face, tile, block);
			}
		}
		return list;
	}

	@Override
	public MonitoredList<LogicInfo> updateInfo(INetworkCache network, MonitoredList<LogicInfo> previousList, Entity entity) {
		MonitoredList<LogicInfo> list = MonitoredList.<LogicInfo>newMonitoredList(network.getNetworkID());
		World world = entity.getEntityWorld();
		LogicInfoRegistry.getEntityInfo(list, entity);
		for (ICustomEntityHandler handler : LogicInfoRegistry.customEntityHandlers) {
			if (handler.canProvideInfo(world, entity)) {
				handler.addInfo(list, world, entity);
			}
		}
		return list;
	}
}
