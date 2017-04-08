package sonar.logistics.api.viewers;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ISonarListener;
import sonar.core.listener.ListenerList;
import sonar.core.utils.IUUIDIdentity;
import sonar.logistics.api.cabling.ILogicTile;

public interface ILogicViewable<L extends ISonarListener> extends ILogicTile, ISonarListenable<L> {
				
}
