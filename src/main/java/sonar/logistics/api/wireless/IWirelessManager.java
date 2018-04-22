package sonar.logistics.api.wireless;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.PlayerListener;
import sonar.logistics.api.networks.ILogisticsNetwork;

public interface IWirelessManager<E extends IWirelessEmitter, R extends IWirelessReceiver> extends ISonarListenable<PlayerListener> {

	public WirelessConnectionType type();
	
	public E getEmitter(int identity);

	public R getReceiver(int identity);

	public void addViewer(EntityPlayer player);

	public void removeViewer(EntityPlayer player);

	public void onEmitterSecurityChanged(E emitter, WirelessSecurity oldSetting);

	public List<ClientWirelessEmitter> getClientEmitters(EntityPlayer player);
}
