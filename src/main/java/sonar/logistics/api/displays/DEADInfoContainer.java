package sonar.logistics.api.displays;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.DirtyPart;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.core.network.sync.SyncableList;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.render.RenderInfoProperties;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayLayout;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.api.tiles.displays.DisplayScreenLook;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.IScaleableDisplay;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gsi.GSIOverlays;
import sonar.logistics.client.gsi.IGSI;
import sonar.logistics.client.gsi.IGSIListViewer;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.InteractionHelper;
import sonar.logistics.helpers.LogisticsHelper;

/** used to store {@link IInfo} along with their respective {@link DisplayInfo} for rendering on a {@link IDisplay} */
@Deprecated
public class DEADInfoContainer extends DirtyPart implements IInfoContainer, ISyncPart {

	private SyncTagType.INT identity = (INT) new SyncTagType.INT("identity").setDefault((int) -1);
	public SyncEnum<DisplayLayout> layout = new SyncEnum(DisplayLayout.values(), -2);
	public final ArrayList<DisplayInfo> storedInfo = Lists.newArrayList(); // stored info occupies sync ids 0-16
	public IDisplay display;
	public SyncableList syncParts = new SyncableList(this);
	public long lastClickTime;
	public UUID lastClickUUID;
	public boolean hasChanged = true;

	// SHOULD BE SYNCED??
	public int currentMaxInfo;
	public double width, height, scale; // inherited from the display type size, but then can be altered accordingly.

	public InfoContainer(IDisplay display, int id) {
		updateRenderingFromDisplay(display);
		this.setListener(display);
		for (int i = 0; i < Math.min(4, display.getDisplayType().getInfoMax()); i++) { // fix to enable changeable DisplayInfo size
			DisplayInfo syncPart = new DisplayInfo(this, i);
			storedInfo.add(syncPart);
		}
		identity.setObject(id);

		syncParts.addParts(storedInfo);
		syncParts.addParts(identity);
		syncParts.addParts(layout);
		resetRenderProperties();
	}

	public void resetRenderProperties() {
		updateRenderingFromDisplay(display);
		for (int i = 0; i < storedInfo.size(); i++) {
			DisplayInfo info = storedInfo.get(i);
			double[] scaling = InteractionHelper.getScaling(this, i);
			double[] translation = InteractionHelper.getTranslation(this, i);
			info.setRenderInfoProperties(new RenderInfoProperties(this, i, scaling, translation), i);
			info.updateGSI();
		}
	}

	public void updateRenderingFromDisplay(IDisplay display) {
		this.display = display;
		if (display instanceof IScaleableDisplay) {
			double[] scaling = ((IScaleableDisplay) display).getScaling();
			this.width = scaling[0];
			this.height = scaling[1];
			this.scale = scaling[2];
		} else {
			this.width = display.getDisplayType().width;
			this.height = display.getDisplayType().height;
			this.scale = display.getDisplayType().scale;
		}
	}

	public double[] getDisplayScaling() {
		return new double[] { width, height, scale };
	}

	public DisplayLayout getLayout() {
		return layout.getObject();
	}

	public void incrementLayout() {
		layout.incrementEnum();
		while (!(layout.getObject().maxInfo <= getMaxCapacity())) {
			layout.incrementEnum();
		}
		// sendInfoContainerPacketToWatchers();
	}

	public void sendInfoContainerPacketToWatchers() {
		display.sendInfoContainerPacket();
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

	public boolean isDisplayingUUID(InfoUUID id) {
		return getDisplayMonitoringUUID(id) != null;
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
		/*
		if (display.getDisplayType() == DisplayType.LARGE) {
			GL11.glTranslated(0, -0.0625 * 4, 0);
		}
		if (display.getDisplayType() == DisplayType.HOLOGRAPHIC) {
			GL11.glTranslated(0.0625, -0.0625 * 7, 0);
		}
		*/
		
		DisplayScreenLook look = GSIOverlays.getCurrentLook(this);

		for (int pos = 0; pos < Math.min(getLayout().maxInfo, getMaxCapacity()); pos++) {
			IDisplayInfo info = storedInfo.get(pos);
			InfoUUID uuid = info.getInfoUUID();
			//if (InfoUUID.shouldRender(uuid)) { FIXME// prevents no data from flashing before client receives data
				GL11.glPushMatrix();
				double[] translation = info.getRenderProperties().translation;
				double[] scaling = info.getRenderProperties().scaling;
				GL11.glTranslated(translation[0], translation[1], translation[2]);
				info.getGSI().renderGSIBackground(info.getSidedCachedInfo(true), this, info, scaling[0], scaling[1], scaling[2], pos);
				info.getGSI().renderGSIForeground(info.getSidedCachedInfo(true), this, info, scaling[0], scaling[1], scaling[2], pos);
				if (look != null) {
					info.getGSI().renderLookOverlays(info.getSidedCachedInfo(true), this, info, scaling[0], scaling[1], scaling[2], pos, look);
				}
				GL11.glPopMatrix();
			//}
		}
	}

	@Override
	public boolean onClicked(TileAbstractDisplay part, BlockInteractionType type, World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (LogisticsHelper.isPlayerUsingOperator(player)) {
			if (!world.isRemote) {
				incrementLayout();
				FontHelper.sendMessage("Screen Layout: " + layout.getObject(), getWorld(), player);
			}
			return true;
		}

		if (display instanceof ConnectedDisplay) {
			if (!((ConnectedDisplay) display).canBeRendered.getObject()) {
				if (world.isRemote) //left click is client only.
					player.sendMessage(new TextComponentTranslation("THE DISPLAY IS INCOMPLETE"));
				return true;
			}
		}

		if (world.isRemote) { // only clicks on client side, like a GUI, positioning may not be the same on server
			boolean doubleClick = false;
			if (world.getTotalWorldTime() - lastClickTime < 10 && player.getPersistentID().equals(lastClickUUID)) {
				doubleClick = true;
			}
			lastClickTime = world.getTotalWorldTime();
			lastClickUUID = player.getPersistentID();
			DisplayScreenClick click = InteractionHelper.getClickPosition(this, pos, type, facing, hitX, hitY, hitZ);
			click.setDoubleClick(doubleClick);

			for (int i = 0; i < getMaxCapacity(); i++) {
				DisplayInfo renderInfo = storedInfo.get(i);
				IInfo cachedInfo = renderInfo.getSidedCachedInfo(world.isRemote);
				IGSI gsi = renderInfo.getGSI();
				if (gsi.canInteractWith(cachedInfo, click, hand)) {
					gsi.onGSIClicked(cachedInfo, click, hand);
					return true;
				}
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
		return Math.min(4, display.getDisplayType().getInfoMax());
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

	public void forEachValidUUID(Consumer<InfoUUID> action) {
		for (int i = 0; i < display.getGSI().getMaxCapacity(); i++) {
			InfoUUID uuid = display.getGSI().getInfoUUID(i);
			if (InfoUUID.valid(uuid)) {
				action.accept(uuid);
			}
		}
	}

	public @Nullable DisplayInfo getDisplayMonitoringUUID(InfoUUID uuid) {
		for (int i = 0; i < getMaxCapacity(); i++) {
			DisplayInfo info = this.getDisplayInfo(i);
			if (info != null && uuid.equals(info.getInfoUUID())) {
				return info;
			}
		}
		return null;
	}

	@Override
	public void onMonitoredListChanged(InfoUUID uuid, AbstractChangeableList list) {
		DisplayInfo displayInfo = getDisplayMonitoringUUID(uuid);
		if (displayInfo != null && displayInfo.getGSI() instanceof IGSIListViewer) {
			((IGSIListViewer) displayInfo.getGSI()).setCachedList(list, uuid);
		}
	}

	@Override
	public void onInfoChanged(InfoUUID uuid, IInfo info) {
		DisplayInfo displayInfo = getDisplayMonitoringUUID(uuid);
		if (info != null) {
			displayInfo.setCachedInfo(info);
		}
	}

	public World getWorld() {
		return display.getCoords() != null ? display.getCoords().getWorld() : null;
	}

	@Override
	public EnumFacing getFacing() {
		return display.getCableFace();
	}

	@Override
	public EnumFacing getRotation() {
		return EnumFacing.NORTH; // FIXME - when it's placed set the rotation;
	}

	@Override
	public int getContainerIdentity() {
		return identity.getObject();
	}

}
