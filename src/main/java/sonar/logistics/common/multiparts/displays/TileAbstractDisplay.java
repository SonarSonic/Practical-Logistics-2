package sonar.logistics.common.multiparts.displays;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import io.netty.buffer.ByteBuf;
import mcmultipart.RayTraceHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2;
import sonar.logistics.api.states.TileMessage;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.common.multiparts.TileSidedLogistics;
import sonar.logistics.networking.displays.ChunkViewerHandler;

public abstract class TileAbstractDisplay extends TileSidedLogistics implements IByteBufTile, IDisplay {

	public static final TileMessage[] validStates = new TileMessage[] { TileMessage.NO_NETWORK, TileMessage.NO_READER_SELECTED };

	public SyncTagType.BOOLEAN defaultData = new SyncTagType.BOOLEAN(2); // set default info
	{
		syncList.addPart(defaultData);
	}

	public SyncType getUpdateTagType() {
		return SyncType.SAVE;
	}

	public void update() {
		super.update();
		updateDefaultInfo();
		if (this.isServer() && ChunkViewerHandler.instance().hasViewersChanged()) {
			sendInfoContainerPacket();
		}
	}

	public void updateDefaultInfo() {
		/*
		if (isServer() && !defaultData.getObject() && networkID.getObject()!=-1) {
			List<ILogicListenable> providers = DisplayHelper.getLocalProvidersFromDisplay(Lists.newArrayList(), world, pos, this);
			ILogicListenable v;
			if (!providers.isEmpty() && (v = providers.get(0)) instanceof IInfoProvider) {
				IInfoProvider monitor = (IInfoProvider) v;
				if (getGSI() != null && monitor != null && monitor.getIdentity() != -1) {
					for (int i = 0; i < Math.min(monitor.getMaxInfo(), getGSI().getMaxCapacity()); i++) {
						if (!InfoUUID.valid(getGSI().getInfoUUID(i)) && getGSI().getDisplayInfo(i).formatList.getObjects().isEmpty()) {
							LocalProviderHandler.doLocalProviderConnect(this, monitor, new InfoUUID(monitor.getIdentity(), i), i);
						}
					}
					sendInfoContainerPacket();
				}
			}
			defaultData.setObject(true);
		}
		*/
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		// sendInfoContainerPacket();
		return super.getUpdateTag();
	}

	@Override
	public void sendInfoContainerPacket() {
		List<EntityPlayerMP> players = ChunkViewerHandler.instance().getWatchingPlayers(this);
		players.forEach(listener -> SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) listener));
	}
	
	public IDisplay getActualDisplay(){
		return this;
	}

	//// EVENTS \\\\

	public void onFirstTick() {
		super.onFirstTick();
		PL2.getInfoManager(world.isRemote).addDisplay(this);
	}

	public void invalidate() {
		super.invalidate();
		PL2.getInfoManager(world.isRemote).removeDisplay(this);
	}

	//// PACKETS \\\\

	@Override
	public void writePacket(ByteBuf buf, int id) {
		switch (id) {
		default:
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		switch (id) {
		case 2:
			//getGSI().incrementLayout();
			break;
		}
	}

	public RayTraceResult getPartHit(EntityPlayer player) {
		Pair<Vec3d, Vec3d> vectors = RayTraceHelper.getRayTraceVectors(player);
		IBlockState state = world.getBlockState(pos);
		return getBlockType().collisionRayTrace(state, world, pos, vectors.getLeft(), vectors.getRight());
	}

	//// GUI \\\\
/*
	@Override
	public Object getServerElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		//return new ContainerMultipartSync(obj);
		return null;
	}

	@Override
	public Object getClientElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		//return new GuiDisplayScreen(obj, obj.getGSI(), GuiState.values()[id], tag.getInteger("infopos"));
		return null;
	}

	@Override
	public void onGuiOpened(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		if (!world.isRemote) {
			DisplayElementContainer cont = getGSI().addElementContainer(new double[] { 0, 0, 0 }, getGSI().getDisplayScaling(), 0.5);
			//cont.getElements().addElement(new TextDisplayElement(cont, "HELLO SAVE ME!"));
			
			List<String> strings = SonarHelper.convertArray(FontHelper.translate(TextFormatting.BOLD + ""+ TextFormatting.UNDERLINE + "Videotape by Radiohead"+ "-" + "When I'm at the pearly gates-This will be on my videotape, my videotape-Mephistopheles is just beneath-And he's reaching up to grab me- + -This is one for the good days-And I have it all here-In red, blue, green-Red, blue, green- + -You are my center-When I spin away-Out of control on videotape-On videotape-On videotape-On videotape-On videotape-On videotape- + -This is my way of saying goodbye-Because I can't do it face to face-I'm talking to you after it's too late-No matter what happens now-You shouldn't be afraid-Because I know today has been-the most perfect day I've ever seen").split("-"));
			DisplayElementList list = new DisplayElementList();
			for(String t : strings){
				list.getElements().addElement(new TextDisplayElement(t));
			}
			cont.getElements().addElement(list);
		}
		PacketHelper.sendLocalProvidersFromScreen(this, world, pos, player);
		SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
	}
	*/
	@Override
	public TileMessage[] getValidMessages() {
		return validStates;
	}

    @Override
	public boolean maxRender() {
		return true;
	}
}
