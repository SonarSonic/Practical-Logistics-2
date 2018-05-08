package sonar.logistics.core.tiles.wireless.emitters;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.helpers.NBTHelper;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.STRING;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.api.core.tiles.wireless.EnumWirelessConnectionState;
import sonar.logistics.api.core.tiles.wireless.EnumWirelessSecurity;
import sonar.logistics.api.core.tiles.wireless.IWirelessManager;
import sonar.logistics.api.core.tiles.wireless.emitters.IWirelessEmitter;
import sonar.logistics.api.core.tiles.wireless.receivers.IWirelessReceiver;
import sonar.logistics.core.tiles.wireless.base.TileAbstractWireless;

import java.util.UUID;

public abstract class TileAbstractEmitter<E extends IWirelessEmitter, R extends IWirelessReceiver> extends TileAbstractWireless implements IWirelessEmitter, IFlexibleGui, IByteBufTile {

	public static final String UNNAMED = "Unnamed Emitter";
	public SyncTagType.STRING emitterName = (STRING) new SyncTagType.STRING(2).setDefault(UNNAMED);
	public SyncEnum<EnumWirelessSecurity> security = new SyncEnum(EnumWirelessSecurity.values(), 5);

	{
		syncList.addParts(emitterName, security);
	}

	public abstract IWirelessManager<E, R> getWirelessHandler();

	//// IWirelessEmitter \\\\
	@Override
	public EnumWirelessConnectionState canPlayerConnect(UUID uuid) {
		return EnumWirelessConnectionState.fromBoolean(playerUUID.getUUID().equals(uuid));
	}

	@Override
	public String getEmitterName() {
		return emitterName.getObject();
	}

	@Override
	public EnumWirelessSecurity getSecurity() {
		return security.getObject();
	}

	//// GUI \\\\

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new GuiWirelessEmitter(this) : null;
	}
	
	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new ContainerMultipartSync(this) : null;
	}

	//// PACKETS \\\\
	@Override
	public void writePacket(ByteBuf buf, int id) {
		ISyncPart part = NBTHelper.getSyncPartByID(syncList.getStandardSyncParts(), id);
		if (part != null)
			part.writeToBuf(buf);
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		EnumWirelessSecurity oldSetting = getSecurity();
		ISyncPart part = NBTHelper.getSyncPartByID(syncList.getStandardSyncParts(), id);
		if (part != null)
			part.readFromBuf(buf);
		if (id == 5) {
			getWirelessHandler().onEmitterSecurityChanged((E) this, oldSetting);
		}
	}

	//// GUI \\\\
	@Override
	public void onGuiOpened(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			break;
		}
	}

}
