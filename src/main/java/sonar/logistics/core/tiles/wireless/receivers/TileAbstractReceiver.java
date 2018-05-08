package sonar.logistics.core.tiles.wireless.receivers;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.sync.SyncNBTAbstract;
import sonar.core.network.sync.SyncNBTAbstractList;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.api.core.tiles.wireless.EnumWirelessConnectionState;
import sonar.logistics.api.core.tiles.wireless.EnumWirelessSecurity;
import sonar.logistics.api.core.tiles.wireless.IWirelessManager;
import sonar.logistics.api.core.tiles.wireless.emitters.ClientWirelessEmitter;
import sonar.logistics.api.core.tiles.wireless.emitters.IWirelessEmitter;
import sonar.logistics.api.core.tiles.wireless.receivers.IWirelessReceiver;
import sonar.logistics.base.guidance.errors.ErrorMessage;
import sonar.logistics.core.tiles.wireless.base.TileAbstractWireless;

import java.util.ArrayList;
import java.util.List;

public abstract class TileAbstractReceiver<E extends IWirelessEmitter, R extends IWirelessReceiver> extends TileAbstractWireless implements IWirelessReceiver<E>, IByteBufTile, IFlexibleGui {

	public static final ErrorMessage[] validStates = new ErrorMessage[] { ErrorMessage.NO_NETWORK, ErrorMessage.NO_EMITTERS_CONNECTED, ErrorMessage.EMITTERS_OFFLINE };

	public SyncNBTAbstractList<ClientWirelessEmitter> clientEmitters = new SyncNBTAbstractList<>(ClientWirelessEmitter.class, 2);
	public SyncNBTAbstract<ClientWirelessEmitter> selectedEmitter = new SyncNBTAbstract<>(ClientWirelessEmitter.class, 4);

	{
		syncList.addParts(clientEmitters, selectedEmitter);
	}
		
	public abstract IWirelessManager<E,R> getWirelessHandler();

	public void addEmitterFromClient(ClientWirelessEmitter emitter) {
		E tile = getWirelessHandler().getEmitter(emitter.getIdentity());
		ClientWirelessEmitter cachedEmitter = getCachedEmitter(emitter.getIdentity());
		boolean found = cachedEmitter != null;
		if (!found) {
			clientEmitters.addObject(emitter);
			onEmitterConnected(tile);
		} else {
			clientEmitters.removeObject(cachedEmitter);
			onEmitterDisconnected(tile);
		}
		sendSyncPacket();
	}

	public List<E> getEmitters() {
		List<E> emitters = new ArrayList<>();
		for (ClientWirelessEmitter dataEmitter : clientEmitters.getObjects()) {
			E emitter = getWirelessHandler().getEmitter(dataEmitter.getIdentity());
			if (emitter != null && emitter.canPlayerConnect(playerUUID.getUUID()).isConnected()) {
				emitters.add(emitter);
			}
		}
		return emitters;
	}
	
	public ClientWirelessEmitter getCachedEmitter(int identity) {
		for (ClientWirelessEmitter entry : clientEmitters.getObjects()) {
			if (entry.getIdentity() == identity) {
				return entry;
			}
		}
		return null;
	}

	@Override
	public void onEmitterSecurityChanged(E emitter, EnumWirelessSecurity oldSetting) {
		if (canEmitterAccessReceiver(emitter).isConnected()) {
			EnumWirelessConnectionState wasConnected = canReceiverAccessEmitter(emitter, oldSetting);
			EnumWirelessConnectionState canConnect = canReceiverAccessEmitter(emitter, emitter.getSecurity());
			if (!wasConnected.isMatching(canConnect)) {
				if(canConnect.isConnected()){
					onEmitterConnected(emitter);
				}else{
					onEmitterDisconnected(emitter);					
				}		
			}
		}
	}

	@Override
	public EnumWirelessConnectionState canEmitterAccessReceiver(IWirelessEmitter emitter) {
		return EnumWirelessConnectionState.fromBoolean(getCachedEmitter(emitter.getIdentity()) != null);
	}

	//// GUI \\\\
	
	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new ContainerAbstractReceiver(this) : null;
	}

	@Override
	public void onGuiOpened(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			getWirelessHandler().addViewer(player);
			break;
		}
	}
	
	//// PACKETS \\\\

	@Override
	public void writePacket(ByteBuf buf, int id) {
		switch (id) {
		case 0:
			selectedEmitter.writeToBuf(buf);
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		switch (id) {
		case 0:
			selectedEmitter.readFromBuf(buf);
			addEmitterFromClient(selectedEmitter.getObject().copy());
			break;
		}
	}

}
