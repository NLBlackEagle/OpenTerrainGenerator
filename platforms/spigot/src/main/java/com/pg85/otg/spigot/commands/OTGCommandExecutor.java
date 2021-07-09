package com.pg85.otg.spigot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

public class OTGCommandExecutor implements TabCompleter, CommandExecutor
{
	private final Map<String, BaseCommand> commandMap = new TreeMap<>(Comparator.naturalOrder());

	public OTGCommandExecutor()
	{
		commandMap.put("data", new DataCommand());
		commandMap.put("edit", new EditCommand());
		commandMap.put("export", new ExportCommand());
		commandMap.put("flush", new FlushCommand());
		commandMap.put("spawn", new SpawnCommand());
		commandMap.put("structure", new StructureCommand());
		commandMap.put("map", new MapCommand());
		commandMap.put("help", new HelpCommand());
		commandMap.put("biome", new BiomeCommand());
		commandMap.put("locate", new LocateCommand());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] strings)
	{
		String cmd;
		String[] args = strings.length >= 2 ? Arrays.copyOfRange(strings, 1, strings.length) : new String[0];

		if (strings.length >= 1)
			cmd = strings[0].toLowerCase();
		else
			cmd = "help";

		BaseCommand otgCommand = this.commandMap.get(cmd);

		if (otgCommand != null)
		{
			return otgCommand.execute(sender, args);
		}
		return true; // TODO usage message
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args)
	{
		List<String> commands = new ArrayList<>(this.commandMap.keySet());

		if (args.length == 0)
			return commands;
		if (args.length == 1)
			return StringUtil.copyPartialMatches(args[0], commands, new ArrayList<>());

		BaseCommand otgCommand = this.commandMap.get(args[0]);

		if (otgCommand != null)
		{
			return otgCommand.onTabComplete(sender, args);
		}

		return Collections.emptyList();
	}
}
