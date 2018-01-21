package sonar.logistics.common.multiparts;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.common.block.SonarMaterials;
import sonar.core.integration.multipart.BlockSonarMultipart;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.helpers.LogisticsHelper;

public abstract class BlockLogistics extends BlockSonarMultipart{

	public PL2Multiparts multipart;

	public BlockLogistics(PL2Multiparts multipart) {
		super(SonarMaterials.machine);
		this.multipart = multipart;
	}

	public PL2Multiparts getMultipart() {
		return multipart;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return getMultipart().createTileEntity();
	}

	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
		return getBoundingBox(state, world, pos);
	}

	public boolean hasStandardGui() {
		return true;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (hasStandardGui() && canOpenGui(player)) {
			TileEntity tile = world.getTileEntity(pos);
			if (!tile.getWorld().isRemote && tile instanceof TileSonarMultipart) {
				((TileSonarMultipart) tile).openFlexibleGui(player, 0);
			}
			return true;
		}
		return false;
	}

	public boolean canOpenGui(EntityPlayer player) {
		if (LogisticsHelper.isPlayerUsingOperator(player)) {
			return false;
		}
		/* TileMessage message = states.canOpenGui(); if (message != null) {
		 * FontHelper.sendMessage(message.message.o(), player.getEntityWorld(),
		 * player); return false; } */
		return true;
	}

	// change it to get the next valid rotation whatever it is;
	/* public Pair<Boolean, EnumFacing> rotatePart(EnumFacing face, EnumFacing
	 * axis) { EnumFacing[] valid = getValidRotations(); if (valid != null) {
	 * int pos = -1; for (int i = 0; i < valid.length; i++) { if (valid[i] ==
	 * face) { pos = i; break; } } if (pos != -1) { int current = pos; boolean
	 * fullCycle = false; while (!fullCycle &&
	 * getContainer().getPartInSlot(PartSlot.getFaceSlot(valid[current])) !=
	 * null) { current++; if (current >= valid.length) { current = 0; } if
	 * (current == pos) { return new Pair(false, face); } } if (current != -1 &&
	 * isServer()) { face = valid[current]; return new Pair(true, face); } } }
	 * return new Pair(false, face); } */
}
