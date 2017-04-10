package sonar.logistics.connections.handlers;

import sonar.logistics.PL2Config;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.NetworkHandler;
import sonar.logistics.api.asm.NetworkHandlerField;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkChannels;

@NetworkHandler(handlerID = TransferNetworkHandler.id, modid = PL2Constants.MODID)
public class TransferNetworkHandler extends DefaultNetworkHandler {
	
	@NetworkHandlerField(handlerID = TransferNetworkHandler.id)
	public static TransferNetworkHandler INSTANCE;
	public static final String id = "transfer";

	@Override
	public String id() {
		return id;
	}

	@Override
	public int updateRate() {
		return PL2Config.transferUpdate;
	}

	@Override
	public INetworkChannels instance(ILogisticsNetwork network) {
		return null;
	}

}
