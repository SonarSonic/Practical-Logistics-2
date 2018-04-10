package sonar.logistics.commands;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import sonar.logistics.PL2;
import sonar.logistics.info.LogicInfoRegistry;

public class CommandResetInfoRegistry implements ICommand {

	private final List aliases;

	public CommandResetInfoRegistry() {
		aliases = new ArrayList<>();
		aliases.add("resetRegistry");
		aliases.add("resetReg");
	}

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public String getName() {
		return "/logistics resetRegistry";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/logistics resetRegistry";
	}

	@Override
	public List<String> getAliases() {
		return aliases;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		World world = sender.getEntityWorld();
		if (!world.isRemote) {
			LogicInfoRegistry.INSTANCE.reload();
			PL2.logger.info("Reset Logic Info Registry");
		}
		sender.sendMessage(new TextComponentTranslation("Reset Logic Info Registry"));


	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

}
