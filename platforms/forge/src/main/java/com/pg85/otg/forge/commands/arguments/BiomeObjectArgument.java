package com.pg85.otg.forge.commands.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.pg85.otg.OTG;

import net.minecraft.command.ISuggestionProvider;

public class BiomeObjectArgument implements ArgumentType<String>
{
	private static final Function<String, String> filterNamesWithSpaces = (name -> name.contains(" ") ? "\"" + name + "\"" : name);

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException
	{
		return reader.readString();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
	{
		String preset = context.getArgument("preset", String.class);
		List<String> list;
		// Get global objects if global, else fetch based on preset
		if (preset.equalsIgnoreCase("global"))
		{
			list = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getGlobalObjectNames();
		}
		else
		{
			list = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getAllBONamesForPreset(preset);
		}
		if (list == null) list = new ArrayList<>();
		list = list.stream().map(filterNamesWithSpaces).collect(Collectors.toList());
		return ISuggestionProvider.suggest(list, builder);
	}
}