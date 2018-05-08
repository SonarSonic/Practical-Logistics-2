package sonar.logistics.core.tiles.wireless.receivers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.api.core.tiles.wireless.IWirelessManager;
import sonar.logistics.api.core.tiles.wireless.emitters.IWirelessEmitter;
import sonar.logistics.api.core.tiles.wireless.receivers.IDataReceiver;
import sonar.logistics.base.guidance.errors.ErrorMessage;
import sonar.logistics.core.tiles.wireless.handling.WirelessDataManager;

import java.util.ArrayList;
import java.util.List;

public class TileDataReceiver extends TileAbstractReceiver implements IDataReceiver, IFlexibleGui, IByteBufTile {

	public IWirelessManager getWirelessHandler(){
		return WirelessDataManager.instance();
	}
	
	public List<Integer> networks = new ArrayList<>();

	public void updateStates() {
		states.markTileMessage(ErrorMessage.NO_EMITTERS_CONNECTED, clientEmitters.getObjects().isEmpty());
		states.markTileMessage(ErrorMessage.EMITTERS_OFFLINE, !clientEmitters.getObjects().isEmpty() && networks.isEmpty());
	}

	//// NETWORK \\\\
	@Override
	public List<Integer> getConnectedNetworks() {
		return networks;
	}

	/** make sure you also notify the handling itself of the change, after updating the connections */
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
	public ErrorMessage[] getValidMessages() {
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
