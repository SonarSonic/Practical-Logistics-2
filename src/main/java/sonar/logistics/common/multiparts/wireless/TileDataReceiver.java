package sonar.logistics.common.multiparts.wireless;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.api.states.TileMessage;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.api.wireless.IWirelessEmitter;
import sonar.logistics.api.wireless.IWirelessManager;
import sonar.logistics.client.gui.GuiDataReceiver;
import sonar.logistics.networking.cabling.WirelessDataManager;

public class TileDataReceiver extends TileAbstractReceiver implements IDataReceiver, IFlexibleGui, IByteBufTile {

	public IWirelessManager getWirelessHandler(){
		return WirelessDataManager.instance();
	}
	
	public List<Integer> networks = new ArrayList<>();

	public void updateStates() {
		states.markTileMessage(TileMessage.NO_EMITTERS_CONNECTED, clientEmitters.getObjects().isEmpty());
		states.markTileMessage(TileMessage.EMITTERS_OFFLINE, !clientEmitters.getObjects().isEmpty() && networks.isEmpty());
	}

	//// NETWORK \\\\
	@Override
	public List<Integer> getConnectedNetworks() {
		return networks;
	}

	/** make sure you also notify the network itself of the change, after updating the networks */
	public void refreshConnectedNetworks() {
		networks = getNetworks();
		updateStates();
	}

	public List<Integer> getNetworks() {
		List<Integer> networks = new ArrayList<>();
		List<IWirelessEmitter> emitters = getEmitters();
		for (IWirelessEmitter emitter : emitters) {
			if (emitter.getNetworkID() != -1) {
				networks.add(emitter.getNetworkID());
			}
		}
		return networks;
	}

	//// GUI \\\\

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new GuiDataReceiver(this) : null;
	}
	
	@Override
	public TileMessage[] getValidMessages() {
		return validStates;
	}

	@Override
	public void onEmitterConnected(IWirelessEmitter emitter) {
		refreshConnectedNetworks();
		WirelessDataManager.instance().connectNetworks(getNetwork(), emitter.getNetwork());
	}

	@Override
	public void onEmitterDisconnected(IWirelessEmitter emitter) {
		refreshConnectedNetworks();
		WirelessDataManager.instance().disconnectNetworks(getNetwork(), emitter.getNetwork());
	}

}
