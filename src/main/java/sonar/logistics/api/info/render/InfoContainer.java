package sonar.logistics.api.info.render;

import java.util.ArrayList;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.DirtyPart;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.SyncableList;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IAdvancedClickableInfo;
import sonar.logistics.api.info.IBasicClickableInfo;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.render.RenderInfoProperties;
import sonar.logistics.api.tiles.displays.DisplayInteractionEvent;
import sonar.logistics.api.tiles.displays.DisplayLayout;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.utils.InfoUUID;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.common.multiparts.generic.DisplayMultipart;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.InfoError;
import sonar.logistics.info.types.LogicInfoList;
import sonar.logistics.network.PacketClickEventClient;

/** used to store {@link IMonitorInfo} along with their respective {@link DisplayInfo} for rendering on a {@link IDisplay} */
public class InfoContainer extends DirtyPart implements IInfoContainer, ISyncPart {

	public final ArrayList<DisplayInfo> storedInfo = Lists.newArrayList();
	public final IDisplay display;
	public SyncableList syncParts = new SyncableList(this);
	public long lastClickTime;
	public UUID lastClickUUID;
	public boolean hasChanged = true;

	public InfoContainer(IDisplay display) {
		this.display = display;
		this.setListener(display);
		for (int i = 0; i < display.maxInfo(); i++) {
			DisplayInfo syncPart = new DisplayInfo(this, i);
			storedInfo.add(syncPart);
		}
		syncParts.addParts(storedInfo);
		resetRenderProperties();
	}

	public void resetRenderProperties() {
		for (int i = 0; i < storedInfo.size(); i++) {
			DisplayInfo info = storedInfo.get(i);
			double[] scaling = InfoHelper.getScaling(display, display.getLayout(), i);
			double[] translation = InfoHelper.getTranslation(display, display.getLayout(), i);
			info.setRenderInfoProperties(new RenderInfoProperties(this, i, scaling, translation), i);
		}
	}

	public static ResourceLocation getColour(int infoPos) {
		switch (infoPos) {
		case 0:
			return LogisticsColours.colourTex1;
		case 1:
			return LogisticsColours.colourTex2;
		case 2:
			return LogisticsColours.colourTex3;
		case 3:
			return LogisticsColours.colourTex4;
		default:
			return LogisticsColours.colourTex1;
		}
	}

	public boolean monitorsUUID(InfoUUID id) {
		for (int i = 0; i < display.getLayout().maxInfo; i++) {
			InfoUUID infoID = this.getInfoUUID(i);
			if (infoID != null && infoID.equals(id)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public InfoUUID getInfoUUID(int pos) {
		return storedInfo.get(pos).getInfoUUID();
	}

	@Override
	public void setUUID(InfoUUID id, int pos) {
		storedInfo.get(pos).setUUID(id);
		markChanged();
	}

	@Override
	public void renderContainer() {
		if (display.getDisplayType() == DisplayType.LARGE) {
			GL11.glTranslated(0, -0.0625 * 4, 0);
		}
		if (display.getDisplayType() == DisplayType.HOLOGRAPHIC) {
			GL11.glTranslated(0.0625, -0.0625 * 7, 0);
		}
		DisplayLayout layout = display.getLayout();
		DisplayType type = display.getDisplayType();
		for (int pos = 0; pos < layout.maxInfo; pos++) {
			IDisplayInfo info = storedInfo.get(pos);

			GL11.glPushMatrix();
			double[] translation = info.getRenderProperties().translation;
			double[] scaling = info.getRenderProperties().scaling;
			GL11.glTranslated(translation[0], translation[1], translation[2]);

			if (info.getSidedCachedInfo(true) == null && !info.getUnformattedStrings().isEmpty()) {
				InfoRenderer.renderNormalInfo(type, scaling[0], scaling[1], scaling[2], info.getFormattedStrings());
			} else {
				IMonitorInfo toDisplay = info.getSidedCachedInfo(true) == null ? InfoError.noData : info.getSidedCachedInfo(true);
				toDisplay.renderInfo(this, info, scaling[0], scaling[1], scaling[2], pos);
			}
			GL11.glPopMatrix();
		}
	}

	@Override
	public boolean onClicked(DisplayMultipart part, BlockInteractionType type, World world, EntityPlayer player, EnumHand hand, ItemStack stack, PartMOP hit) {
		boolean doubleClick = false;
		if (world.getTotalWorldTime() - lastClickTime < 10 && player.getPersistentID().equals(lastClickUUID)) {
			doubleClick = true;
		}
		lastClickTime = world.getTotalWorldTime();
		lastClickUUID = player.getPersistentID();

		for (int i = 0; i < display.maxInfo(); i++) {
			IDisplayInfo info = storedInfo.get(i);
			IMonitorInfo cachedInfo = info.getSidedCachedInfo(world.isRemote);
			if (cachedInfo instanceof IAdvancedClickableInfo) {
				if (!world.isRemote) {
					IAdvancedClickableInfo clickable = ((IAdvancedClickableInfo) cachedInfo);
					int hashCode = UUID.randomUUID().hashCode();
					DisplayInteractionEvent event = new DisplayInteractionEvent(hashCode, cachedInfo, i, player, type, doubleClick, hand, hit);
					PL2.getServerManager().clickEvents.put(hashCode, event);
					PL2.network.sendTo(new PacketClickEventClient(part.getUUID(), part.getPos(), event), (EntityPlayerMP) player);
				}
			} else if (cachedInfo instanceof IBasicClickableInfo) {
				IBasicClickableInfo clickable = ((IBasicClickableInfo) cachedInfo);
				return clickable.onStandardClick(type, doubleClick, info, player, hand, stack, hit, this);
			}
		}
		return true;
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		NBTTagCompound tag = nbt.getCompoundTag(this.getTagName());
		if (!tag.hasNoTags()) {
			NBTHelper.readSyncParts(tag, type, syncParts);
			resetRenderProperties();
		}
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTTagCompound tag = NBTHelper.writeSyncParts(new NBTTagCompound(), type, syncParts, type == SyncType.SYNC_OVERRIDE);
		if (!tag.hasNoTags()) {
			nbt.setTag(this.getTagName(), tag);
		}
		return nbt;
	}

	@Override
	public int getMaxCapacity() {
		return Math.min(display.maxInfo(), display.getLayout().maxInfo);
	}

	@Override
	public IDisplay getDisplay() {
		return display;
	}

	@Override
	public DisplayInfo getDisplayInfo(int pos) {
		return storedInfo.get(pos);
	}

	public InfoContainer cloneFromContainer(IInfoContainer container) {
		this.readData(container.writeData(new NBTTagCompound(), SyncType.SAVE), SyncType.SAVE);
		return this;
	}

	@Override
	public void writeToBuf(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, this.writeData(new NBTTagCompound(), SyncType.SAVE));
	}

	@Override
	public void readFromBuf(ByteBuf buf) {
		readData(ByteBufUtils.readTag(buf), SyncType.SAVE);
	}

	@Override
	public boolean canSync(SyncType sync) {
		return SyncType.isGivenType(sync, SyncType.DEFAULT_SYNC, SyncType.SAVE);
	}

	@Override
	public String getTagName() {
		return "container";
	}

	@Override
	public void markChanged(IDirtyPart part) {
		syncParts.markSyncPartChanged(part);
		listener.markChanged(this);
	}

	@Override
	public void onMonitoredListChanged(MonitoredList list, InfoUUID uuid) {
		for (int i = 0; i < display.getLayout().maxInfo; i++) {
			DisplayInfo info = this.getDisplayInfo(i);
			if (info != null && uuid.equals(info.getInfoUUID())) {
				if (info.cachedInfo instanceof LogicInfoList) {
					((LogicInfoList) info.cachedInfo).setCachedList((MonitoredList) list.cloneInfo(), uuid);
				}
			}
		}
	}

}
