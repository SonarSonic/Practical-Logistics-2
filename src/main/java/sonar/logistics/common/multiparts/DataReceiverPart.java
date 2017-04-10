package sonar.logistics.common.multiparts;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.sync.SyncNBTAbstract;
import sonar.core.network.sync.SyncNBTAbstractList;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.wireless.ClientDataEmitter;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.client.gui.GuiDataReceiver;
import sonar.logistics.common.containers.ContainerDataReceiver;
import sonar.logistics.common.multiparts.generic.WirelessPart;
import sonar.logistics.connections.CacheHandler;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.managers.WirelessManager;

public class DataReceiverPart extends WirelessPart implements IDataReceiver, IFlexibleGui, IByteBufTile {

	public SyncNBTAbstractList<ClientDataEmitter> clientEmitters = new SyncNBTAbstractList<ClientDataEmitter>(ClientDataEmitter.class, 2);
	public SyncNBTAbstract<ClientDataEmitter> selectedEmitter = new SyncNBTAbstract<ClientDataEmitter>(ClientDataEmitter.class, 4);
	public List<Integer> networks = Lists.newArrayList();

	{
		syncList.addParts(clientEmitters, selectedEmitter);
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack heldItem, PartMOP hit) {
		if (!LogisticsHelper.isPlayerUsingOperator(player)) {
			if (isServer()) {
				openFlexibleGui(player, 0);
			}
			return true;
		}
		return false;
	}

	public void addEmitterFromClient(ClientDataEmitter emitter) {		
		List<ClientDataEmitter> emitters = Lists.newArrayList(clientEmitters.getObjects());
		Iterator<ClientDataEmitter> iterator = emitters.iterator();
		boolean found = false;
		while (iterator.hasNext()) {
			ClientDataEmitter entry = iterator.next();
			if (entry.equals(emitter)) {// FIXME what's going on here then
				IDataEmitter tile = WirelessManager.getEmitter(entry.getIdentity());
				iterator.remove();
				found = true;
				break;
			}
		}
		if (!found) {
			IDataEmitter tile = WirelessManager.getEmitter(emitter.getIdentity());
			emitters.add(emitter);
		}
		clientEmitters.setObjects(emitters);
		networks = getNetworks();
		WirelessManager.receiverChanged(this);
		sendSyncPacket();
	}

	//// NETWORK \\\\

	@Override
	public List<Integer> getConnectedNetworks() {
		return networks;
	}

	public void refreshConnectedNetworks() {
		networks = getNetworks();
	}

	public List<Integer> getNetworks() {
		List<Integer> networks = Lists.newArrayList();
		List<IDataEmitter> emitters = getEmitters();
		for (IDataEmitter emitter : emitters) {
			if (emitter.getNetworkID() != -1) {
				networks.add(emitter.getNetworkID());
			}
		}
		return networks;
	}

	public List<IDataEmitter> getEmitters() {
		List<IDataEmitter> emitters = Lists.newArrayList();
		for (ClientDataEmitter dataEmitter : clientEmitters.getObjects()) {
			IDataEmitter emitter = WirelessManager.getEmitter(dataEmitter.getIdentity());
			if (emitter != null && emitter.canPlayerConnect(playerUUID.getUUID())) {
				emitters.add(emitter);
			}
		}
		return emitters;
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

	//// GUI \\\\

	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new ContainerDataReceiver(this) : null;
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new GuiDataReceiver(this) : null;
	}

	@Override
	public void onGuiOpened(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			WirelessManager.addViewer(player);
			break;
		}
	}

	@Override
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.DATA_RECEIVER;
	}
}
