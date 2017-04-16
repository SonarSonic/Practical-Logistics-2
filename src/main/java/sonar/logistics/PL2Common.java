package sonar.logistics;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.integration.multipart.SonarMultipart;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.utils.IGuiItem;
import sonar.core.utils.IGuiTile;
import sonar.logistics.network.PacketAddListener;
import sonar.logistics.network.PacketChannels;
import sonar.logistics.network.PacketClickEventClient;
import sonar.logistics.network.PacketClickEventServer;
import sonar.logistics.network.PacketClientEmitters;
import sonar.logistics.network.PacketConnectedDisplayScreen;
import sonar.logistics.network.PacketEmitterStatement;
import sonar.logistics.network.PacketInfoList;
import sonar.logistics.network.PacketInventoryReader;
import sonar.logistics.network.PacketItemInteractionText;
import sonar.logistics.network.PacketMonitoredList;
import sonar.logistics.network.PacketNodeFilter;
import sonar.logistics.network.PacketViewables;
import sonar.logistics.network.PacketWirelessStorage;

public class PL2Common implements IGuiHandler {

	public static void registerPackets() {
		PL2.network.registerMessage(PacketMonitoredList.Handler.class, PacketMonitoredList.class, 0, Side.CLIENT);
		PL2.network.registerMessage(PacketChannels.Handler.class, PacketChannels.class, 1, Side.CLIENT);
		PL2.network.registerMessage(PacketAddListener.Handler.class, PacketAddListener.class, 2, Side.SERVER);
		PL2.network.registerMessage(PacketInfoList.Handler.class, PacketInfoList.class, 3, Side.CLIENT);
		PL2.network.registerMessage(PacketInventoryReader.Handler.class, PacketInventoryReader.class, 4, Side.SERVER);
		PL2.network.registerMessage(PacketClientEmitters.Handler.class, PacketClientEmitters.class, 5, Side.CLIENT);
		PL2.network.registerMessage(PacketViewables.Handler.class, PacketViewables.class, 6, Side.CLIENT);
		PL2.network.registerMessage(PacketConnectedDisplayScreen.Handler.class, PacketConnectedDisplayScreen.class, 7, Side.CLIENT);
		PL2.network.registerMessage(PacketClickEventServer.Handler.class, PacketClickEventServer.class, 8, Side.SERVER);
		PL2.network.registerMessage(PacketClickEventClient.Handler.class, PacketClickEventClient.class, 9, Side.CLIENT);
		PL2.network.registerMessage(PacketNodeFilter.Handler.class, PacketNodeFilter.class, 10, Side.SERVER);
		PL2.network.registerMessage(PacketEmitterStatement.Handler.class, PacketEmitterStatement.class, 11, Side.SERVER);
		PL2.network.registerMessage(PacketWirelessStorage.Handler.class, PacketWirelessStorage.class, 12, Side.SERVER);
		PL2.network.registerMessage(PacketItemInteractionText.Handler.class, PacketItemInteractionText.class, 13, Side.CLIENT);
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		Object part = SonarMultipartHelper.getPartFromHash(ID, world, new BlockPos(x, y, z));
		if (part == null || !(part instanceof IGuiTile)) {
			part = world.getTileEntity(new BlockPos(x, y, z));
		}
		if (part != null && ID != IGuiItem.ID && part instanceof IGuiTile) {
			if (part instanceof SonarMultipart) {
				((SonarMultipart) part).forceNextSync();
			}
			IGuiTile guiTile = (IGuiTile) part;
			return guiTile.getGuiContainer(player);
		} else if (ID == IGuiItem.ID) {
			ItemStack equipped = player.getHeldItemMainhand();
			if (equipped != null && equipped.getItem() instanceof IGuiItem) {
				return ((IGuiItem) equipped.getItem()).getGuiContainer(player, equipped);
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		Object part = SonarMultipartHelper.getPartFromHash(ID, world, new BlockPos(x, y, z));
		if (part == null || !(part instanceof IGuiTile)) {
			part = world.getTileEntity(new BlockPos(x, y, z));
		}
		if (part != null && part instanceof IGuiTile) {
			if (part instanceof SonarMultipart) {
				((SonarMultipart) part).forceNextSync();
			}
			IGuiTile guiTile = (IGuiTile) part;
			return guiTile.getGuiScreen(player);
		} else {
			ItemStack equipped = player.getHeldItemMainhand();
			if (equipped != null && equipped.getItem() instanceof IGuiItem) {
				return ((IGuiItem) equipped.getItem()).getGuiScreen(player, equipped);
			}
		}
		return null;
	}

	public boolean isUsingOperator() {
		return false;
	}

	public void setUsingOperator(boolean bool) {}

	public void preInit(FMLPreInitializationEvent event) {}

	public void load(FMLInitializationEvent event) {}

	public void postLoad(FMLPostInitializationEvent evt) {}

}
