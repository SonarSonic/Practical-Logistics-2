package sonar.logistics.info.types;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.info.IComparableInfo;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.logistics.ComparableObject;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.helpers.InfoRenderer;

@LogicInfoType(id = ClockInfo.id, modid = PL2Constants.MODID)
public class ClockInfo extends BaseInfo<ClockInfo> implements IMonitorInfo<ClockInfo>, INBTSyncable, INameableInfo<ClockInfo>, IComparableInfo<ClockInfo> {

	public static final String id = "clock";
	public int compare;
	public SyncTagType.DOUBLE firstNum = new SyncTagType.DOUBLE(1), secondNum = new SyncTagType.DOUBLE(2);
	public SyncTagType.STRING clockString = new SyncTagType.STRING(3);

	{
		syncList.addParts(firstNum, secondNum, clockString);
	}

	public ClockInfo() {
	}

	public ClockInfo(double firstNum, double secondNum, String clockString) {
		this.firstNum.setObject(firstNum);
		this.secondNum.setObject(secondNum);
		this.clockString.setObject(clockString);
	}

	@Override
	public String getClientIdentifier() {
		return "Time";
	}

	@Override
	public String getClientObject() {
		if (!isValid()) {
			return "ERROR";
		}
		return clockString.getObject();
	}

	@Override
	public String getClientType() {
		return "time";
	}

	@Override
	public boolean isIdenticalInfo(ClockInfo info) {
		return info.firstNum.getObject() == firstNum.getObject() && info.secondNum.getObject() == secondNum.getObject();
	}

	@Override
	public boolean isMatchingInfo(ClockInfo info) {
		return true;
	}

	@Override
	public boolean isMatchingType(IMonitorInfo info) {
		return info instanceof ClockInfo;
	}

	@Override
	public boolean isHeader() {
		return false;
	}

	@Override
	public LogicMonitorHandler<ClockInfo> getHandler() {
		return null;
	}

	@Override
	public boolean isValid() {
		return firstNum.getObject() != null && firstNum.getObject() != null && clockString.getObject() != null;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public ClockInfo copy() {
		return new ClockInfo(firstNum.getObject(), secondNum.getObject(), clockString.getObject());
	}

	@Override
	public void renderInfo(InfoContainer container, IDisplayInfo displayInfo, double displayWidth, double displayHeight, double displayScale, int infoPos) {
		GL11.glPushMatrix();
		GL11.glPushMatrix();
		GlStateManager.disableLighting();
		GL11.glTranslated(-1, -+0.0625 * 12, +0.004);
		Minecraft.getMinecraft().getTextureManager().bindTexture(InfoContainer.getColour(infoPos));
		InfoRenderer.renderProgressBar(displayWidth, displayHeight, displayScale, (compare == 1 ? secondNum.getObject() : firstNum.getObject()), (compare == 1 ? firstNum.getObject() : secondNum.getObject()));
		GlStateManager.enableLighting();
		GL11.glTranslated(0, 0, -0.001);
		GL11.glPopMatrix();
		InfoRenderer.renderNormalInfo(container.display.getDisplayType(), displayWidth, displayHeight, displayScale, displayInfo.getFormattedStrings());
		GL11.glPopMatrix();

	}

	@Override
	public ArrayList<ComparableObject> getComparableObjects(ArrayList<ComparableObject> objects) {
		objects.add(new ComparableObject(this, "isEmitting", firstNum.getObject()==secondNum.getObject()));
		return objects;
	}
}