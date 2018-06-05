package sonar.logistics.base.utils.commands;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import sonar.logistics.PL2;
import sonar.logistics.core.tiles.displays.info.MasterInfoRegistry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CommandResetInfoRegistry implements ICommand {

	private final List aliases;

	public CommandResetInfoRegistry() {
		aliases = new ArrayList<>();
		aliases.add("resetRegistry");
		aliases.add("resetReg");
	}

	@Override
	public int compareTo(@Nonnull ICommand o) {
		return this.getName().compareTo(o.getName());
	}

	@Nonnull
    @Override
	public String getName() {
		return "/logistics resetRegistry";
	}

	@Nonnull
    @Override
	public String getUsage(@Nonnull ICommandSender sender) {
		return "/logistics resetRegistry";
	}

	@Nonnull
    @Override
	public List<String> getAliases() {
		return aliases;
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
		World world = sender.getEntityWorld();
		if (!world.isRemote) {
			MasterInfoRegistry.INSTANCE.reload();
			PL2.logger.info("Reset Logic Info Registry");
		}
		sender.sendMessage(new TextComponentTranslation("Reset Logic Info Registry"));
	}

	@Override
	public boolean checkPermission(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender) {
		return true;
	}

	@Nonnull
    @Override
	public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, BlockPos targetPos) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(@Nonnull String[] args, int index) {
		return false;
	}

}
